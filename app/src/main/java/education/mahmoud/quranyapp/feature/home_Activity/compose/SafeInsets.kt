package education.mahmoud.quranyapp.feature.home_Activity.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection

/**
 * Window insets for the edge-to-edge Compose screens (Compose foundation 1.4 APIs only).
 * System bars + display cutout, so landscape / notched phones keep content out of the cutout.
 */
object SafeInsets {
    /** Scaffold content: status bar, navigation / gesture bar and cutouts. */
    val content: WindowInsets
        @Composable get() = WindowInsets.systemBars.union(WindowInsets.displayCutout)

    /** Top app bar: its background extends behind the status bar, its content is pushed below it. */
    val topBar: WindowInsets
        @Composable get() = content.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
}

/**
 * Scaffold padding without the bottom part. Use it outside a scrolling container and add
 * [PaddingValues.calculateBottomPadding] inside it, so content scrolls behind the
 * navigation bar but its end can still be scrolled above it.
 */
@Composable
fun Modifier.paddingExceptBottom(padding: PaddingValues): Modifier {
    val dir = LocalLayoutDirection.current
    return this.padding(
        start = padding.calculateStartPadding(dir),
        top = padding.calculateTopPadding(),
        end = padding.calculateEndPadding(dir),
    )
}
