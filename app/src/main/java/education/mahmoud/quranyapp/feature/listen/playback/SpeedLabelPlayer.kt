package education.mahmoud.quranyapp.feature.listen.playback

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Wraps the service's player so the media notification shows the current speed next to the
 * reciter name (e.g. "Mishary Alafasy · 1.5x") whenever it is not the normal 1x.
 *
 * Media3 only refreshes the notification text on metadata changes, so a speed change is
 * re-announced to the session as a metadata change.
 */
class SpeedLabelPlayer(player: Player) : ForwardingPlayer(player) {

    private val sessionListeners = CopyOnWriteArrayList<Player.Listener>()

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                val metadata = mediaMetadata
                sessionListeners.forEach { it.onMediaMetadataChanged(metadata) }
            }
        })
    }

    override fun addListener(listener: Player.Listener) {
        sessionListeners += listener
        super.addListener(listener)
    }

    override fun removeListener(listener: Player.Listener) {
        sessionListeners -= listener
        super.removeListener(listener)
    }

    override fun getMediaMetadata(): MediaMetadata {
        val metadata = super.getMediaMetadata()
        val speed = playbackParameters.speed
        if (PlaybackSpeed.isDefault(speed)) return metadata
        val label = PlaybackSpeed.label(speed)
        val artist = metadata.artist?.let { "$it \u00B7 $label" } ?: label
        return metadata.buildUpon().setArtist(artist).build()
    }
}
