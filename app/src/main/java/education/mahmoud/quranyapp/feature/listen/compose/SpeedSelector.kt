package education.mahmoud.quranyapp.feature.listen.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
        // numbers read left-to-right (slow → fast) even in the Arabic layout
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PlaybackSpeed.OPTIONS.forEach { option ->
                    val label = PlaybackSpeed.label(option)
                    val description = stringResource(R.string.listen_speed_option, label)
                    FilterChip(
                        selected = PlaybackSpeed.normalize(speed) == option,
                        onClick = { onSpeed(option) },
                        label = {
                            Text(label, style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Default))
                        },
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .semantics { contentDescription = description },
                    )
                }
            }
        }
    }
}
