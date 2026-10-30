package education.mahmoud.quranyapp.feature.listen

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.IOException

/** What the Listen screen is doing right now. */
sealed class ListenPhase {
    object Idle : ListenPhase()
    data class Downloading(val done: Int, val total: Int) : ListenPhase()
    data class Playing(
        val position: Int,
        val total: Int,
        val ayah: AyahItem,
        val suraName: String,
        val isPlaying: Boolean,
        val progressMs: Int,
        val durationMs: Int,
    ) : ListenPhase()
}

/**
 * Downloads the selected ayahs into app-specific storage (getExternalFilesDir, no runtime
 * permission needed on any API level) and plays them in order with [MediaPlayer].
 */
@Suppress("TooManyFunctions") // one small function per player control
class ListenViewModel(app: Application) : AndroidViewModel(app), KoinComponent {

    private val repository: QuranRepository by inject()
    private val http: OkHttpClient by inject()
    private val audioRoot: File = app.getExternalFilesDir("audio") ?: File(app.filesDir, "audio")
    private val downloader by lazy { AudioDownloader(http, audioRoot) }

    var suras by mutableStateOf<List<SuraItem>>(emptyList())
        private set
    var form by mutableStateOf(ListenInput.Form())
        private set
    var reciter by mutableStateOf(Reciter.fromId(repository.listenReciter))
        private set
    var phase by mutableStateOf<ListenPhase>(ListenPhase.Idle)
        private set

    /** String resource of a one-shot message for the snackbar. */
    var message by mutableStateOf<Int?>(null)
        private set

    private var player: MediaPlayer? = null
    private var playlist: List<AyahItem> = emptyList()
    private var position = 0
    private var workJob: Job? = null
    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            suras = withContext(Dispatchers.IO) { repository.suras }
        }
    }

    fun ayahCount(sura: Int): Int = suras.firstOrNull { it.index == sura }?.numOfAyahs ?: 0

    fun validation() = ListenInput.validate(form, ::ayahCount)

    fun updateForm(newForm: ListenInput.Form) {
        val previous = form
        // keep the end sura in step when the start sura moves past it
        form = if (newForm.startSura != previous.startSura && newForm.endSura < newForm.startSura) {
            newForm.copy(endSura = newForm.startSura)
        } else {
            newForm
        }
    }

    fun selectReciter(value: Reciter) {
        reciter = value
        repository.listenReciter = value.id
    }

    fun consumeMessage() {
        message = null
    }

    fun start() {
        val (range, _) = validation()
        if (range == null || suras.isEmpty()) return
        stop()
        val currentReciter = reciter
        workJob = viewModelScope.launch {
            val first = ListenInput.globalIndex(::ayahCount, range.startSura, range.startAyah)
            val last = ListenInput.globalIndex(::ayahCount, range.endSura, range.endAyah)
            val ayahs = withContext(Dispatchers.IO) { repository.getAyahSInRange(first, last) }
            val missing = ayahs.filterNot { currentReciter.cacheFile(audioRoot, it.ayahIndex).exists() }
            phase = ListenPhase.Downloading(0, missing.size)
            missing.forEachIndexed { index, ayah ->
                if (!isActive) return@launch
                val ok = withContext(Dispatchers.IO) { downloader.download(currentReciter, ayah.ayahIndex) }
                if (!ok) {
                    phase = ListenPhase.Idle
                    message = R.string.listen_download_error
                    return@launch
                }
                phase = ListenPhase.Downloading(index + 1, missing.size)
            }
            playlist = ListenInput.playlist(ayahs, range.repeatEach, range.repeatAll)
            playAt(0, currentReciter)
        }
    }

    fun togglePlayPause() {
        val mp = player ?: return
        if (mp.isPlaying) mp.pause() else mp.start()
        publishPlaying()
    }

    fun next() {
        if (position + 1 < playlist.size) playAt(position + 1, reciter)
    }

    fun previous() {
        if (playlist.isNotEmpty()) playAt((position - 1).coerceAtLeast(0), reciter)
    }

    fun seekTo(ms: Int) {
        player?.seekTo(ms)
        publishPlaying()
    }

    fun stop() {
        workJob?.cancel()
        tickerJob?.cancel()
        releasePlayer()
        playlist = emptyList()
        phase = ListenPhase.Idle
    }

    private fun playAt(index: Int, playReciter: Reciter) {
        releasePlayer()
        position = index
        val ayah = playlist.getOrNull(index) ?: return finishPlayback()
        val file = playReciter.cacheFile(audioRoot, ayah.ayahIndex)
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                setDataSource(file.absolutePath)
                setOnCompletionListener { playAt(position + 1, playReciter) }
                prepare()
                start()
            }
        } catch (e: IOException) {
            // corrupt / partial file: drop it so the next attempt downloads it again
            Log.w(TAG, "cannot play ${file.name}", e)
            file.delete()
            message = R.string.error
            stop()
            return
        }
        publishPlaying()
        startTicker()
    }

    private fun finishPlayback() {
        stop()
        message = R.string.listen_finished
    }

    private fun publishPlaying() {
        val mp = player ?: return
        val ayah = playlist.getOrNull(position) ?: return
        phase = ListenPhase.Playing(
            position = position,
            total = playlist.size,
            ayah = ayah,
            suraName = suras.firstOrNull { it.index == ayah.surahIndex }?.name.orEmpty(),
            isPlaying = mp.isPlaying,
            progressMs = mp.currentPosition,
            durationMs = mp.duration.coerceAtLeast(0),
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                publishPlaying()
                delay(TICK_MS)
            }
        }
    }

    private fun releasePlayer() {
        player?.setOnCompletionListener(null)
        player?.release()
        player = null
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        releasePlayer()
    }

    companion object {
        private const val TAG = "ListenViewModel"
        private const val TICK_MS = 250L
    }
}
