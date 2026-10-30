package education.mahmoud.quranyapp.feature.listen.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.feature.listen.ListenActivity
import org.koin.android.ext.android.inject

/**
 * Plays the Listen screen's ayah queue in the background.
 *
 * Media3 shows the media-style notification (play/pause, previous/next) and keeps the
 * service in the foreground while playing. ExoPlayer handles audio focus (pauses on calls
 * or other apps) and pauses when headphones are unplugged.
 */
class QuranPlaybackService : MediaSessionService() {

    private var session: MediaSession? = null
    private val repository: QuranRepository by inject()

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        // the saved speed applies to every queue, including ones resumed from the notification
        player.setPlaybackSpeed(repository.playbackSpeed)
        player.addListener(object : Player.Listener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                repository.playbackSpeed = playbackParameters.speed
            }
        })
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, ListenActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        session = MediaSession.Builder(this, SpeedLabelPlayer(player))
            .setSessionActivity(openApp)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        // swiping the app away while paused should not leave a dead notification behind
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }
}
