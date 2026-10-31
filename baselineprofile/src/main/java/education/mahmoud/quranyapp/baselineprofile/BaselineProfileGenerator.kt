package education.mahmoud.quranyapp.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates [baseline-prof.txt] and [startup-prof.txt].
 *
 * Startup rules (includeInStartupProfile) are only the cold start to first frame:
 * Application, Koin, splash, and the Compose home shell. R8 uses those to pack
 * the primary DEX. Scrolling and opening a surah stay in the baseline profile
 * so ART compiles them, without pushing them into the startup DEX.
 *
 * Run on a rooted emulator or device (API 28+):
 * ./gradlew :app:generateReleaseBaselineProfile
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = PACKAGE,
        includeInStartupProfile = true,
        maxIterations = 5,
        stableIterations = 3,
    ) {
        pressHome()
        startActivityAndWait()
    }

    @Test
    fun scrollSurahList() = rule.collect(
        packageName = PACKAGE,
        includeInStartupProfile = false,
        maxIterations = 5,
        stableIterations = 3,
    ) {
        pressHome()
        startActivityAndWait()
        waitForSurahList()
        val list = device.findObject(By.scrollable(true))
        list?.setGestureMargin(device.displayWidth / 8)
        list?.fling(Direction.DOWN)
        device.waitForIdle()
        list?.fling(Direction.UP)
        device.waitForIdle()
    }

    @Test
    fun openSurahReader() = rule.collect(
        packageName = PACKAGE,
        includeInStartupProfile = false,
        maxIterations = 5,
        stableIterations = 3,
    ) {
        pressHome()
        startActivityAndWait()
        waitForSurahList()
        device.findObject(By.text(FATIHA))?.click()
        val reader = device.wait(Until.findObject(By.res(PACKAGE, READER_LIST)), 30_000)
        // loadFullQuran() builds all 604 pages off the main thread after this screen opens.
        device.waitForIdle()
        Thread.sleep(3_000)
        reader?.swipe(Direction.LEFT, 0.7f)
        device.waitForIdle()
    }

    private fun MacrobenchmarkScope.waitForSurahList() {
        // Cold first launch imports the Quran JSON into Room before the splash leaves.
        val shown = device.wait(Until.hasObject(By.text(FATIHA)), 90_000)
        check(shown == true) { "Surah list never showed — splash or the first-run import did not finish." }
    }

    private companion object {
        const val PACKAGE = "education.mahmoud.quranyapp"
        const val FATIHA = "الفاتحة"
        const val READER_LIST = "rvAyahsPages"
    }
}
