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
        val currentReciter: Reciter,
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
    private val storage = AudioStorage(audioRoot)

    var suras by mutableStateOf<List<SuraItem>>(emptyList())
        private set
    var form by mutableStateOf(ListenInput.Form())
        private set
    var phase by mutableStateOf<ListenPhase>(ListenPhase.Idle)
        private set

    /** Downloaded audio per reciter, shown in the "downloaded audio" card. */
    var downloads by mutableStateOf<List<ReciterDownloads>>(emptyList())
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

    private var currentQueue: List<Pair<AyahItem, Reciter>> = emptyList()

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publishPlaying()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) finishPlayback()
        }

        override fun onPlayerError(error: PlaybackException) {
            val index = controller?.currentMediaItemIndex ?: -1
            val pair = currentQueue.getOrNull(index)
            pair?.let { (ayah, reciter) -> reciter.cacheFile(audioRoot, ayah.ayahIndex).delete() }
            message = R.string.error
            stop()
        }
    }

    init {
        viewModelScope.launch {
            suras = withContext(Dispatchers.IO) { repository.suras }
            publishPlaying()
        }
        refreshDownloads()
        controllerFuture.addListener(
            {
                controller = runCatching { controllerFuture.get() }.getOrNull()?.also {
                    it.addListener(playerListener)
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
        form = if (newForm.startSura != previous.startSura && newForm.endSura < newForm.startSura) {
            newForm.copy(endSura = newForm.startSura)
        } else {
            newForm
        }
    }

    fun toggleReciter(reciter: Reciter) {
        val current = form.selectedReciters.toMutableSet()
        if (current.contains(reciter)) {
            if (current.size > 1) current.remove(reciter)
        } else {
            current.add(reciter)
        }
        form = form.copy(selectedReciters = current)
    }

    fun consumeMessage() {
        message = null
    }

    fun start() {
        val (range, _) = validation()
        if (range == null || suras.isEmpty()) return
        stop()
        workJob = viewModelScope.launch {
            val first = ListenInput.globalIndex(::ayahCount, range.startSura, range.startAyah)
            val last = ListenInput.globalIndex(::ayahCount, range.endSura, range.endAyah)
            val ayahs = withContext(Dispatchers.IO) { repository.getAyahSInRange(first, last) }

            val missing = mutableListOf<Pair<Reciter, AyahItem>>()
            for (reciter in range.selectedReciters) {
                for (ayah in ayahs) {
                    if (!reciter.cacheFile(audioRoot, ayah.ayahIndex).exists()) {
                        missing.add(reciter to ayah)
                    }
                }
            }

            phase = ListenPhase.Downloading(0, missing.size)
            missing.forEachIndexed { index, (reciter, ayah) ->
                if (!isActive) return@launch
                val ok = withContext(Dispatchers.IO) { downloader.download(reciter, ayah.ayahIndex) }
                if (!ok) {
                    phase = ListenPhase.Idle
                    message = R.string.listen_download_error
                    return@launch
                }
                phase = ListenPhase.Downloading(index + 1, missing.size)
            }
            phase = ListenPhase.Idle
            refreshDownloads()

            val sessionQueue = ListenInput.sessionPlaylist(
                ayahs,
                range.selectedReciters,
                range.sessionMode,
                range.chunkSize,
            )
            val finalQueue = ListenInput.playlist(sessionQueue, range.repeatEach, range.repeatAll)
            play(finalQueue)
        }
    }

    fun refreshDownloads() {
        viewModelScope.launch {
            downloads = withContext(Dispatchers.IO) { storage.usage() }
        }
    }

    fun deleteDownloads(target: Reciter?) {
        if ((controller?.mediaItemCount ?: 0) > 0) stop()
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                if (target == null) storage.deleteAll() else storage.delete(target)
            }
            message = if (ok) R.string.downloads_deleted else R.string.error
            refreshDownloads()
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
        currentQueue = emptyList()
        phase = ListenPhase.Idle
    }

    private fun play(queue: List<Pair<AyahItem, Reciter>>) {
        val c = controller
        if (c == null || queue.isEmpty()) {
            if (queue.isEmpty()) finishPlayback() else message = R.string.error
            return
        }
        currentQueue = queue
        val items = queue.mapIndexed { index, (ayah, reciter) ->
            val suraName = suraName(ayah)
            val artist = getApplication<Application>().getString(reciter.nameRes)
            MediaItem.Builder()
                .setMediaId("$index:${ayah.ayahIndex}:${reciter.id}")
                .setUri(Uri.fromFile(reciter.cacheFile(audioRoot, ayah.ayahIndex)))
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
        val pair = currentQueue.getOrNull(index) ?: return
        val (ayah, reciter) = pair
        if (phase is ListenPhase.Downloading) return
        phase = ListenPhase.Playing(
            position = index,
            total = c.mediaItemCount,
            ayah = ayah,
            suraName = suraName(ayah),
            currentReciter = reciter,
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
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
    }

    companion object {
        private const val TICK_MS = 250L
    }
}
