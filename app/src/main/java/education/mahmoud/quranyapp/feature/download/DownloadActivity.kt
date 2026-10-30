package education.mahmoud.quranyapp.feature.download

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import education.mahmoud.quranyapp.feature.download.compose.DataScreen
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import education.mahmoud.quranyapp.feature.listen.ListenActivity

/**
 * "تحميل البيانات" screen. The old Java/ButterKnife version crashed on open (ButterKnife's
 * annotation processor no longer runs, so every view was null) and downloaded audio from a
 * dead URL into shared storage. It now shows what is available offline and manages the
 * downloaded recitations; new recitations are downloaded from the Listen screen.
 */
class DownloadActivity : AppCompatActivity() {

    private val viewModel: DataViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                QuranyTheme {
                    DataScreen(
                        viewModel = viewModel,
                        onBack = ::finish,
                        onOpenListen = { startActivity(Intent(this, ListenActivity::class.java)) },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}
