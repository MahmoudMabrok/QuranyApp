package education.mahmoud.quranyapp.feature.listen.playback

import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.feature.listen.Reciter

/**
 * The ayahs currently queued in [QuranPlaybackService], kept process-wide so that the
 * Listen screen can show the playing ayah again after it was closed and reopened while
 * playback continued in the background.
 */
object PlaybackQueue {
    @Volatile
    var ayahs: List<AyahItem> = emptyList()

    @Volatile
    var reciter: Reciter = Reciter.ALAFASY
}
