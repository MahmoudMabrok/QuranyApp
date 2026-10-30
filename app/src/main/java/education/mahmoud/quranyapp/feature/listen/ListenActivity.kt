package education.mahmoud.quranyapp.feature.listen

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import education.mahmoud.quranyapp.feature.listen.compose.ListenScreen
import education.mahmoud.quranyapp.utils.EdgeToEdge

/** Listen to a range of ayahs with a chosen reciter (Compose, follows [QuranyTheme]). */
class ListenActivity : AppCompatActivity() {

    private val viewModel: ListenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                QuranyTheme {
                    ListenScreen(viewModel = viewModel, onBack = ::finish)
                }
            }
        }
    }
}
