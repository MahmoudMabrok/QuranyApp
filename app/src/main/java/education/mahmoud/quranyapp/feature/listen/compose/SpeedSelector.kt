package education.mahmoud.quranyapp.feature.listen.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.CompositionLocalProvider
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.feature.listen.playback.PlaybackSpeed

/**
 * Playback speed chips (0.5x … 2x). Used on the Listen form and in the player card; the
 * chosen speed is saved and applied to the background player immediately.
 */
@Composable
internal fun SpeedSelector(
    speed: Float,
    onSpeed: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Speed,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                stringResource(R.string.listen_speed_current, PlaybackSpeed.label(speed)),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        // numbers read left-to-right (slow → fast) even in the Arabic layout; one segmented
        // row so all six speeds fit on a phone screen without wrapping
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50)),
            ) {
                PlaybackSpeed.OPTIONS.forEachIndexed { index, option ->
                    val selected = PlaybackSpeed.normalize(speed) == option
                    val description = stringResource(R.string.listen_speed_option, PlaybackSpeed.label(option))
                    if (index > 0) {
                        Box(
                            Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(MaterialTheme.colorScheme.outline),
                        )
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .selectable(selected = selected, role = Role.RadioButton) { onSpeed(option) }
                            .semantics { contentDescription = description },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            PlaybackSpeed.label(option),
                            style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Default),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
