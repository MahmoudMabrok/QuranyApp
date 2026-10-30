package education.mahmoud.quranyapp.feature.home_Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.databinding.ActivityHomeBinding
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.BookmarkItem
import education.mahmoud.quranyapp.feature.ayahs_search.ShowSearchResults
import education.mahmoud.quranyapp.feature.download.DownloadActivity
import education.mahmoud.quranyapp.feature.gotoscreen.compose.GoToActions
import education.mahmoud.quranyapp.feature.gotoscreen.compose.GoToDialog
import education.mahmoud.quranyapp.feature.home_Activity.compose.HomeActions
import education.mahmoud.quranyapp.feature.home_Activity.compose.HomeScreen
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import education.mahmoud.quranyapp.feature.listen.ListenActivity
import education.mahmoud.quranyapp.feature.read_log.ReadLogFragment
import education.mahmoud.quranyapp.feature.scores.ScoreActivity
import education.mahmoud.quranyapp.feature.setting.SettingActivity
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.feature.splash.Splash
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.viewBinding
import org.koin.android.ext.android.inject

/**
 * Home screen host. The two main working screens (Surah list + Bookmarks) are
 * now rendered with Jetpack Compose inside [binding.composeHome]. The [mainContainer]
 * overlay is retained for the splash, search and read-log fragments.
 */
class HomeActivity : AppCompatActivity() {

    private val quranRepository: QuranRepository by inject()
    var ahays = 0

    private var selectedTab by mutableStateOf(0)
    private var showGoTo by mutableStateOf(false)

    private val binding by viewBinding(ActivityHomeBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        goToSplash()
        ahays = quranRepository.totlaAyahs

        setupCompose()
    }

    private fun setupCompose() {
        binding.composeHome.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                QuranyTheme {
                    HomeScreen(
                        repository = quranRepository,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        actions = HomeActions(
                            onSuraClick = ::openSura,
                            onBookmarkClick = ::openBookmarkItem,
                            onSearch = ::openSearch,
                            onJump = ::openGoToSura,
                            onLastRead = ::gotoLastRead,
                            onListen = ::openListen,
                            onReadLog = ::goToReadLog,
                            onScore = ::gotoScore,
                            onDownload = ::gotoDownload,
                            onSettings = ::openSetting,
                        ),
                    )
                    if (showGoTo) {
                        GoToDialog(
                            repository = quranRepository,
                            onDismiss = { showGoTo = false },
                            actions = GoToActions(
                                onSuraAyah = { sura, ayah ->
                                    openAyahs(Constants.SURAH_GO_INDEX to sura, Constants.AYAH_GO_INDEX to ayah)
                                },
                                onJuz = { openAyahs(Constants.JUZ_INDEX to it) },
                                onPage = { openAyahs(Constants.PAGE_INDEX to it) },
                            ),
                        )
                    }
                }
            }
        }
    }

    fun afterSplash() {
        supportFragmentManager.popBackStackImmediate()
        openRead()
    }

    private fun openRead() {
        selectedTab = 0
    }

    fun goToSplash() {
        Log.d(TAG, "goToSplash:")

        supportFragmentManager.beginTransaction()
            .add(R.id.mainContainer, Splash())
            .addToBackStack(null)
            .commit()
    }

    private fun openSearch() {
        supportFragmentManager.beginTransaction()
            .add(R.id.mainContainer, ShowSearchResults())
            .addToBackStack(null)
            .commit()
    }

    private fun gotoLastRead() {
        val index = quranRepository.latestRead
        if (index == -1) {
            Toast.makeText(this, "You Have no saved recitation", Toast.LENGTH_SHORT).show()
            return
        }
        val openAcivity = Intent(this, ShowAyahsActivity::class.java)
        openAcivity.putExtra(Constants.LAST_INDEX, index)
        startActivity(openAcivity)
    }

    private fun openSura(index: Int) {
        val openAcivity = Intent(this, ShowAyahsActivity::class.java)
        openAcivity.putExtra(Constants.SURAH_INDEX, index)
        startActivity(openAcivity)
    }

    private fun openBookmarkItem(item: BookmarkItem) {
        val openAcivity = Intent(this, ShowAyahsActivity::class.java)
        openAcivity.putExtra(Constants.PAGE_INDEX, item.pageNum)
        startActivity(openAcivity)
    }

    private fun openGoToSura() {
        showGoTo = true
    }

    private fun openAyahs(vararg extras: Pair<String, Int>) {
        showGoTo = false
        val intent = Intent(this, ShowAyahsActivity::class.java)
        extras.forEach { (key, value) -> intent.putExtra(key, value) }
        startActivity(intent)
    }

    private fun openSetting() {
        startActivity(Intent(this, SettingActivity::class.java))
    }

    private fun gotoScore() {
        startActivity(Intent(this, ScoreActivity::class.java))
    }

    private fun gotoDownload() {
        startActivity(Intent(this, DownloadActivity::class.java))
    }

    private fun openListen() {
        startActivity(Intent(this, ListenActivity::class.java))
    }

    private fun goToReadLog() {
        supportFragmentManager.beginTransaction()
            .add(R.id.mainContainer, ReadLogFragment())
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}
