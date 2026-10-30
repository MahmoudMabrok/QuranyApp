package education.mahmoud.quranyapp.feature.home_Activity.compose

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import education.mahmoud.quranyapp.R

// Brand palette mirrored from res/values/colors.xml
private val Primary = Color(0xFF00A388)
private val PrimaryVariant = Color(0xFF79BD8F)
private val Accent = Color(0xFFBEEB9F)
private val SurfaceLight = Color(0xFFF6F8F5)
private val EvenRow = Color(0x1A79BD8F)

val QuranFont = FontFamily(Font(R.font.me_quran))

private val QuranyTypography = Typography().let { base ->
    Typography(
        titleLarge = base.titleLarge.copy(fontFamily = QuranFont),
        titleMedium = base.titleMedium.copy(fontFamily = QuranFont),
        bodyLarge = base.bodyLarge.copy(fontFamily = QuranFont),
        bodyMedium = base.bodyMedium.copy(fontFamily = QuranFont),
        labelLarge = base.labelLarge.copy(fontFamily = QuranFont),
    )
}

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Accent,
    onPrimaryContainer = Color(0xFF14312A),
    secondary = PrimaryVariant,
    onSecondary = Color.White,
    surface = Color.White,
    surfaceVariant = EvenRow,
    background = SurfaceLight,
    onBackground = Color(0xFF1B1B1B),
    onSurface = Color(0xFF1B1B1B),
)

private val DarkColors = darkColorScheme(
    primary = PrimaryVariant,
    onPrimary = Color(0xFF08281F),
    primaryContainer = Color(0xFF0F5245),
    onPrimaryContainer = Accent,
    secondary = Accent,
    surface = Color(0xFF1C1F1D),
    surfaceVariant = Color(0xFF23302B),
    background = Color(0xFF121412),
    onBackground = Color(0xFFEDEDED),
    onSurface = Color(0xFFEDEDED),
)

val EvenRowColor: Color get() = EvenRow

@Composable
fun QuranyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        // edge-to-edge: the bars are transparent and the top app bars draw behind the status bar.
        // Light theme: teal app bar -> white status icons, light background -> dark nav icons.
        // Dark theme: light green app bar -> dark status icons, dark background -> light nav icons.
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = QuranyTypography,
        content = content,
    )
}
