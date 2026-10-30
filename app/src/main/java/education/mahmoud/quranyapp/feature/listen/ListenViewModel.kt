package education.mahmoud.quranyapp.feature.listen

import android.app.Application
import android.content.ComponentName
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.listen.playback.PlaybackQueue
import education.mahmoud.quranyapp.feature.listen.playback.QuranPlaybackService
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
 * permission needed on any API level) and hands them to [QuranPlaybackService], which keeps
 * playing in the background with notification controls. This view model only talks to the
 * service through a media3 [MediaController].
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

    private val controllerFuture: ListenableFuture<MediaController> = MediaController.Builder(
        app,
        SessionToken(app, ComponentName(app, QuranPlaybackService::class.java)),
    ).buildAsync()
    private var controller: MediaController? = null
    private var workJob: Job? = null
    private var tickerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publishPlaying()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) finishPlayback()
        }

        override fun onPlayerError(error: PlaybackException) {
            // a corrupt / partial file: drop it so the next attempt downloads it again
            val ayah = PlaybackQueue.ayahs.getOrNull(controller?.currentMediaItemIndex ?: -1)
            ayah?.let { PlaybackQueue.reciter.cacheFile(audioRoot, it.ayahIndex).delete() }
            message = R.string.error
            stop()
        }
    }

    init {
        viewModelScope.launch {
            suras = withContext(Dispatchers.IO) { repository.suras }
            publishPlaying()
        }
        controllerFuture.addListener(
            {
                controller = runCatching { controllerFuture.get() }.getOrNull()?.also {
                    it.addListener(playerListener)
                    // playback may still be running from an earlier visit to this screen
                    if (it.mediaItemCount > 0) startTicker()
                    publishPlaying()
                }
            },
            ContextCompat.getMainExecutor(app),
        )
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
            phase = ListenPhase.Idle
            val queue = ListenInput.playlist(ayahs, range.repeatEach, range.repeatAll)
            play(queue, currentReciter)
        }
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun next() {
        controller?.takeIf { it.hasNextMediaItem() }?.seekToNextMediaItem()
    }

    fun previous() {
        val c = controller ?: return
        if (c.hasPreviousMediaItem()) c.seekToPreviousMediaItem() else c.seekTo(0)
    }

    fun seekTo(ms: Int) {
        controller?.seekTo(ms.toLong())
    }

    fun stop() {
        workJob?.cancel()
        tickerJob?.cancel()
        controller?.let {
            it.stop()
            it.clearMediaItems()
        }
        PlaybackQueue.ayahs = emptyList()
        phase = ListenPhase.Idle
    }

    private fun play(queue: List<AyahItem>, playReciter: Reciter) {
        val c = controller
        if (c == null || queue.isEmpty()) {
            if (queue.isEmpty()) finishPlayback() else message = R.string.error
            return
        }
        PlaybackQueue.ayahs = queue
        PlaybackQueue.reciter = playReciter
        val artist = getApplication<Application>().getString(playReciter.nameRes)
        val items = queue.mapIndexed { index, ayah ->
            val suraName = suraName(ayah)
            MediaItem.Builder()
                .setMediaId("$index:${ayah.ayahIndex}")
                .setUri(Uri.fromFile(playReciter.cacheFile(audioRoot, ayah.ayahIndex)))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("$suraName \u00B7 ${ayah.ayahInSurahIndex}")
                        .setArtist(artist)
                        .setAlbumTitle(suraName)
                        .build(),
                )
                .build()
        }
        c.setMediaItems(items, 0, 0L)
        c.prepare()
        c.play()
        startTicker()
    }

    private fun suraName(ayah: AyahItem): String =
        suras.firstOrNull { it.index == ayah.surahIndex }?.name.orEmpty()

    private fun finishPlayback() {
        stop()
        message = R.string.listen_finished
    }

    private fun publishPlaying() {
        val c = controller ?: return
        if (c.mediaItemCount == 0) return
        val index = c.currentMediaItemIndex
        val ayah = PlaybackQueue.ayahs.getOrNull(index) ?: return
        if (phase is ListenPhase.Downloading) return
        reciter = PlaybackQueue.reciter
        phase = ListenPhase.Playing(
            position = index,
            total = c.mediaItemCount,
            ayah = ayah,
            suraName = suraName(ayah),
            isPlaying = c.playWhenReady,
            progressMs = c.currentPosition.toInt(),
            durationMs = c.duration.takeIf { it > 0 }?.toInt() ?: 0,
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

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        // only disconnect: the service keeps playing in the background
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
    }

    companion object {
        private const val TICK_MS = 250L
    }
}
