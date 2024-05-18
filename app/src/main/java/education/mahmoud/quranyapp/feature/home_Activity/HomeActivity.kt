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
import com.google.android.material.tabs.TabLayoutMediator
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.databinding.ActivityHomeBinding
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.feature.ayahs_search.ShowSearchResults
import education.mahmoud.quranyapp.feature.download.DownloadActivity
import education.mahmoud.quranyapp.feature.feedback_activity.FeedbackActivity
import education.mahmoud.quranyapp.feature.gotoscreen.GoToSurah
import education.mahmoud.quranyapp.feature.listening_activity.ListenFragment
import education.mahmoud.quranyapp.feature.read_log.ReadLogFragment
import education.mahmoud.quranyapp.feature.scores.ScoreActivity
import education.mahmoud.quranyapp.feature.setting.SettingActivity
import education.mahmoud.quranyapp.feature.showSuraAyas.ShowAyahsActivity
import education.mahmoud.quranyapp.feature.show_tafseer.TafseerDetails
import education.mahmoud.quranyapp.feature.splash.Splash
import education.mahmoud.quranyapp.feature.test_quran.TestFragment
import education.mahmoud.quranyapp.utils.Constants
import education.mahmoud.quranyapp.utils.viewBinding
import org.koin.android.ext.android.inject
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class HomeActivity : AppCompatActivity() {

    private var isPermissionAllowed: Boolean = false
    private val quranRepository: QuranRepository by inject()
    var ahays = 0

    private var currentID = 0

    private val tafseerFragment by lazy { TafseerDetails() }
    private val listenFragment by lazy { ListenFragment() }
    private val readLogFragment by lazy { ReadLogFragment() }
    private val testFragment by lazy { TestFragment() }

    private val binding by viewBinding(ActivityHomeBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        goToSplash()
        ahays = quranRepository.totlaAyahs

        //  toolbar?.let { setSupportActionBar(it) }

        setupVP()
    }

    private fun setupVP() {
        with(binding) {
            val adapter = HomeVPAdapter(this@HomeActivity)
            binding.vpHome.adapter = adapter
            val titles = resources.getStringArray(R.array.home_tabs)
            TabLayoutMediator(tabHome, binding.vpHome) { tab, pos ->
                tab.text = titles[pos]
                Log.i("TestTest", "HomeActivity setupVP ${titles[pos]}")
            }.attach()
        }
    }

    fun afterSplash() {
        supportFragmentManager.popBackStackImmediate()
        openRead()
        // checkLastReadAndDisplayDialoge()
    }

    private fun determineToOpenOrNotSplash() {
        Log.d(TAG, "determineToOpenOrNotSplash:  n $ahays")
        if (ahays == 0) {
            Log.d(TAG, "determineToOpenOrNotSplash: ok  $ahays")
            goToSplash()
        }
    }

    private fun checkLastReadAndDisplayDialoge() {
        val last = quranRepository.latestRead
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

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openRead() {
        binding.vpHome.currentItem = 0
    }

    private fun openTafseer() {
        val a = supportFragmentManager.beginTransaction()
        a.replace(binding.homeContainer.id, tafseerFragment).commit()
    }

    private fun openListen() {
        val a = supportFragmentManager.beginTransaction()
        a.replace(binding.homeContainer.id, listenFragment).commit()
    }

    private fun openTest() {
        val a = supportFragmentManager.beginTransaction()
        a.replace(binding.homeContainer.id, testFragment).commit()
    }

    private fun openBookmark() {
        binding.vpHome.currentItem = 1
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
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
            R.id.actionBookmark -> openBookmark()
        }
        return super.onOptionsItemSelected(item)
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
        transaction.replace(binding.homeContainer.id, logFragment).commit()
    }

    companion object {
        private const val RC_STORAGE = 1001
        private const val TAG = "HomeActivity"
    }
}
