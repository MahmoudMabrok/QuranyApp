package education.mahmoud.quranyapp.feature.home_Activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import education.mahmoud.quranyapp.feature.gotoscreen.GoToSurah
import education.mahmoud.quranyapp.feature.home_Activity.compose.HomeActions
import education.mahmoud.quranyapp.feature.home_Activity.compose.HomeScreen
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import education.mahmoud.quranyapp.feature.listening_activity.ListenFragment
import education.mahmoud.quranyapp.feature.read_log.ReadLogFragment
import education.mahmoud.quranyapp.feature.scores.ScoreActivity
import education.mahmoud.quranyapp.feature.setting.SettingActivity
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.feature.splash.Splash
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.viewBinding
import org.koin.android.ext.android.inject
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

/**
 * Home screen host. The two main working screens (Surah list + Bookmarks) are
 * now rendered with Jetpack Compose inside [binding.composeHome]. The [mainContainer]
 * overlay is retained for the splash, search and read-log fragments.
 */
class HomeActivity : AppCompatActivity() {

    private var isPermissionAllowed: Boolean = false
    private val quranRepository: QuranRepository by inject()
    var ahays = 0

    private val listenFragment by lazy { ListenFragment() }

    private var selectedTab by mutableStateOf(0)

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
                            onReadLog = ::goToReadLog,
                            onScore = ::gotoScore,
                            onDownload = ::gotoDownload,
                            onSettings = ::openSetting,
                        ),
                    )
                }
            }
        }
    }

    fun afterSplash() {
        supportFragmentManager.popBackStackImmediate()
        openRead()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openRead() {
        selectedTab = 0
    }

    fun acquirePermission() {
        val perms = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        EasyPermissions.requestPermissions(
            PermissionRequest.Builder(this, RC_STORAGE, *perms).build()
        )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isPermissionAllowed = true
            quranRepository.permissionState = true
            listenFragment.downloadAyahs()
        } else if (requestCode == RC_STORAGE) {
            showMessage(getString(R.string.down_permission))
            quranRepository.permissionState = false
            // user refuse to take permission
            openRead()
        }
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
        val a = supportFragmentManager.beginTransaction()
        val goToSurah = GoToSurah()
        goToSurah.show(a, null)
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

    private fun goToReadLog() {
        supportFragmentManager.beginTransaction()
            .add(R.id.mainContainer, ReadLogFragment())
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val RC_STORAGE = 1001
        private const val TAG = "HomeActivity"
    }
}
