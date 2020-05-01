package education.mahmoud.quranyapp.feature.home_Activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tjeannin.apprate.AppRate
import education.mahmoud.quranyapp.App
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.data_layer.Repository
import education.mahmoud.quranyapp.data_layer.local.room.AyahItem
import education.mahmoud.quranyapp.data_layer.local.room.SuraItem
import education.mahmoud.quranyapp.data_layer.model.full_quran.Surah
import education.mahmoud.quranyapp.feature.ayahs_search.ShowSearchResults
import education.mahmoud.quranyapp.feature.bookmark_fragment.BookmarkFragment
import education.mahmoud.quranyapp.feature.download.DownloadActivity
import education.mahmoud.quranyapp.feature.feedback_activity.FeedbackActivity
import education.mahmoud.quranyapp.feature.home_Activity.HomeActivity
import education.mahmoud.quranyapp.feature.listening_activity.ListenFragment
import education.mahmoud.quranyapp.feature.read_log.ReadLogFragment
import education.mahmoud.quranyapp.feature.scores.ScoreActivity
import education.mahmoud.quranyapp.feature.setting.SettingActivity
import education.mahmoud.quranyapp.feature.show_sura_ayas.ShowAyahsActivity
import education.mahmoud.quranyapp.feature.show_sura_list.GoToSurah
import education.mahmoud.quranyapp.feature.show_sura_list.SuraListFragment
import education.mahmoud.quranyapp.feature.show_tafseer.TafseerDetails
import education.mahmoud.quranyapp.feature.splash.Splash
import education.mahmoud.quranyapp.feature.test_quran.TestFragment
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.Util
import kotlinx.android.synthetic.main.activity_home2.*
import org.koin.java.KoinJavaComponent
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class HomeActivity : AppCompatActivity() {

    private var isPermissionAllowed: Boolean = false
    private var repository = KoinJavaComponent.get(Repository::class.java)
    var ahays = 0
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_read -> {
                openRead()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_tafseer -> {
                openTafseer()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigationListen -> {
                openListen()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_test -> {
                openTest()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_bookmarks -> {
                openBookmark()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)
        ButterKnife.bind(this)
        Log.d(TAG, "onCreate: start app")
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        repository = (application as App).repository
        AppRate(this).setMinLaunchesUntilPrompt(5)
                .init()
        //    Stetho.initializeWithDefaults(getApplication());
        Log.d(TAG, "onCreate: n $ahays")
        ahays = repository.totlaAyahs
        Log.d(TAG, "onCreate: nn after  $ahays")
        openRead()
        checkLastReadAndDisplayDialoge()
        determineToOpenOrNotSplash()
    }

    private fun determineToOpenOrNotSplash() {
        Log.d(TAG, "determineToOpenOrNotSplash:  n $ahays")
        if (ahays == 0) {
            Log.d(TAG, "determineToOpenOrNotSplash: ok  $ahays")
            goToSplash()
        }
    }

    private fun checkLastReadAndDisplayDialoge() {
        val last = repository.latestRead
        Log.d(TAG, "checkLastReadAndDisplayDialoge: $last")
        if (last >= 0) {
            displayDialoge(last)
            Log.d(TAG, "checkLastReadAndDisplayDialoge: @@ ")
        }
    }

    private fun displayDialoge(last: Int) {
        Log.d(TAG, "displayDialoge: ")
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.last_read_dialoge, null)
        val button = view.findViewById<Button>(R.id.btnOpenPage)
        button.setOnClickListener {
            openPage(last)
            dialog.dismiss()
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun openPage(last: Int) {
        gotoSuraa(last)
    }

    override fun onResume() {
        super.onResume()
        // used to update UI
        val id: Int = navigation.getSelectedItemId()
        navigation.setSelectedItemId(id)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openRead() {
        val fragment = SuraListFragment()
        val a = supportFragmentManager.beginTransaction()
        a.replace(homeContainer.getId(), fragment).commit()
    }

    private fun openTafseer() {
        val fragment = TafseerDetails()
        val a = supportFragmentManager.beginTransaction()
        a.replace(homeContainer.getId(), fragment).commit()
    }

    private fun openListen() {
        val fragment = ListenFragment()
        val a = supportFragmentManager.beginTransaction()
        a.replace(homeContainer.getId(), fragment).commit()
    }

    private fun openTest() {
        val fragment = TestFragment()
        //  TestQuranSound fragment = new TestQuranSound();
        val a = supportFragmentManager.beginTransaction()
        a.replace(homeContainer.getId(), fragment).commit()
    }

    private fun openBookmark() {
        val fragment = BookmarkFragment()
        val a = supportFragmentManager.beginTransaction()
        a.replace(homeContainer.getId(), fragment).commit()
    }

    fun acquirePermission() {
        val perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        EasyPermissions.requestPermissions(PermissionRequest.Builder(this, RC_STORAGE, *perms).build())
    }

    private fun goToSplash() {
        Log.d(TAG, "goToSplash:")
        val openAcivity = Intent(this@HomeActivity, Splash::class.java)
        startActivity(openAcivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isPermissionAllowed = true
            repository.permissionState = true
        } else if (requestCode == RC_STORAGE) {
            showMessage(getString(R.string.down_permission))
            repository.permissionState = false
            openListen()
        }
    }

    private fun openSearch() {
        val openAcivity = Intent(this, ShowSearchResults::class.java)
        startActivity(openAcivity)
    }

    private fun gotoLastRead() {
        val index = repository.latestRead
        if (index == -1) {
            Toast.makeText(this, "You Have no saved recitation", Toast.LENGTH_SHORT).show()
            return
        }
        gotoSuraa(index)
    }

    private fun gotoSuraa(index: Int) {
        val openAcivity = Intent(this, ShowAyahsActivity::class.java)
        openAcivity.putExtra(Constants.LAST_INDEX, index)
        startActivity(openAcivity)
    }

    private fun openGoToSura() {
        val a = supportFragmentManager.beginTransaction()
        val goToSurah = GoToSurah()
        goToSurah.show(a, null)
    }

    private fun openSetting() {
        val openAcivity = Intent(this, SettingActivity::class.java)
        startActivity(openAcivity)
    }

    private fun gotoFeedback() {
        val openAcivity = Intent(this, FeedbackActivity::class.java)
        startActivity(openAcivity)
    }

    private fun gotoScore() {
        val openAcivity = Intent(this, ScoreActivity::class.java)
        startActivity(openAcivity)
    }

    private fun gotoDownload() {
        val openAcivity = Intent(this, DownloadActivity::class.java)
        startActivity(openAcivity)
    }

    private fun goToReadLog() {
        val transaction = supportFragmentManager.beginTransaction()
        val logFragment = ReadLogFragment()
        transaction.replace(homeContainer.getId(), logFragment).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.actionJump -> openGoToSura()
            R.id.actionSearch -> openSearch()
            R.id.actionSetting -> openSetting()
            R.id.actionGoToLastRead -> gotoLastRead()
            R.id.actionReadLog -> goToReadLog()
            R.id.actionScore -> gotoScore()
            R.id.actionDownload -> gotoDownload()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        // // TODO: 6/27/2019 message of exiting - n pages - . m
    }

    companion object {
        private const val RC_STORAGE = 1001
        private const val TAG = "HomeActivity"
    }
}