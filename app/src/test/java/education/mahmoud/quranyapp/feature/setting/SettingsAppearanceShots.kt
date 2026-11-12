package education.mahmoud.quranyapp.feature.setting

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import io.github.sherifshabans.mushaf.MushafDecoration
import io.github.sherifshabans.mushaf.MushafPager
import io.github.sherifshabans.mushaf.MushafSpread
import io.github.sherifshabans.mushaf.PageTurn
import io.github.sherifshabans.mushaf.rememberMushafPagerState
import androidx.compose.ui.unit.LayoutDirection
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.datalayer.local.room.BookmarkItem
import education.mahmoud.quranyapp.feature.home_Activity.compose.HomeActions
import education.mahmoud.quranyapp.feature.home_Activity.compose.HomeTopBar
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import education.mahmoud.quranyapp.feature.setting.compose.SettingsScreen
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafLook
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafPageTurn
import education.mahmoud.quranyapp.feature.showSuraAyas.mushaftext.MushafReaderSettings
import education.mahmoud.quranyapp.utils.Constants
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Draws the home bar and the settings mushaf preview to PNG with real Skia,
 * the same way mushaf-text renders its decoration harness. No emulator.
 *
 *     ./gradlew :app:testDebugUnitTest --tests '*SettingsAppearanceShots*'
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h1400dp-420dpi")
class SettingsAppearanceShots {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun homeTopBarShowsTheSettingsGear() {
        show { HomeTopBar(idleActions()) }
        save("home-top-bar.png")
    }

    @Test
    fun settingsPreviewOrnate() {
        show { SettingsPreview(MushafReaderSettings()) }
        save("settings-preview-ornate.png")
    }

    @Test
    fun settingsPreviewCairo() {
        show { SettingsPreview(MushafReaderSettings(look = MushafLook.CAIRO)) }
        save("settings-preview-cairo.png")
    }

    @Test
    fun settingsPickerShowsTheLibraryTurns() {
        show { SettingsPreview(MushafReaderSettings(pageTurn = MushafPageTurn.FLIP)) }
        rule.onNodeWithTag("page_turn").performScrollTo()
        rule.waitForIdle()
        save("settings-page-turn-picker.png")
    }

    @Test
    fun readerCurlMidTurn() {
        showCurl(MushafSpread.Single)
        holdFingerHalfway()
        save("reader-curl-mid.png")
    }

    @Config(qualifiers = "land-w1000dp-h640dp-420dpi")
    @Test
    fun readerCurlMidTurnSpread() {
        showCurl(MushafSpread.Auto)
        holdFingerHalfway()
        save("reader-curl-mid-spread.png")
    }

    private fun showCurl(spread: MushafSpread) {
        show {
            MushafPager(
                modifier = Modifier.fillMaxSize(),
                state = rememberMushafPagerState(initialPage = 2),
                decoration = MushafDecoration.Classic,
                pageTurn = PageTurn.Curl,
                spread = spread,
                pagePadding = PaddingValues(0.dp),
            )
        }
        rule.waitForIdle()
    }

    /** Drag half the page width and leave the finger down, the way a held curl looks. */
    private fun holdFingerHalfway() {
        rule.onRoot().performTouchInput {
            down(percentOffset(0.15f, 0.55f))
            moveTo(percentOffset(0.65f, 0.55f))
        }
        rule.waitForIdle()
    }

    @Test
    fun readerFlipMidTurn() {
        show {
            MushafPager(
                modifier = Modifier.fillMaxSize(),
                state = rememberMushafPagerState(initialPage = 2),
                decoration = MushafDecoration.Classic,
                pageTurn = PageTurn.Flip,
                pagePadding = PaddingValues(0.dp),
            )
        }
        rule.waitForIdle()
        rule.onRoot().performTouchInput {
            down(center)
            moveBy(percentOffset(0.35f, 0f))
        }
        rule.waitForIdle()
        save("reader-flip-mid.png")
    }

    private fun show(content: @Composable () -> Unit) {
        rule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                QuranyTheme { content() }
            }
        }
        rule.waitForIdle()
    }

    private fun save(name: String) {
        rule.waitForIdle()
        val view = rule.activity.window.decorView
        val width = view.width.coerceAtLeast(1)
        val height = view.height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { view.draw(Canvas(bitmap)) }
        val dir = File("build/appearance-shots").apply { mkdirs() }
        val file = File(dir, name)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("[appearance] ${file.absolutePath} ${bitmap.width}x${bitmap.height}")
    }
}

@Composable
private fun SettingsPreview(reader: MushafReaderSettings) {
    SettingsScreen(
        nightMode = false,
        backgroundColor = Constants.WHITE,
        onNightModeChange = {},
        onBackgroundColorChange = {},
        onBack = {},
        reader = reader,
        versionName = "3.0.5",
    )
}

private fun idleActions() = HomeActions(
    onSuraClick = { _: Int -> },
    onBookmarkClick = { _: BookmarkItem -> },
    onSearch = {},
    onJump = {},
    onLastRead = {},
    onListen = {},
    onReadLog = {},
    onMemorizationTest = {},
    onScore = {},
    onDownload = {},
    onSettings = {},
    onHighlightClick = { _: AyahItem -> },
)
