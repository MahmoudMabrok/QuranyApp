package education.mahmoud.quranyapp.feature.home_Activity.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.BookmarkItem
import education.mahmoud.quranyapp.feature.bookmark_fragment.compose.BookmarkScreen
import education.mahmoud.quranyapp.feature.show_sura_list.compose.SuraListScreen

/**
 * Actions delegated back to the hosting Activity so the Compose UI stays free of
 * navigation concerns while reusing the existing Activity/Fragment destinations.
 */
data class HomeActions(
    val onSuraClick: (Int) -> Unit,
    val onBookmarkClick: (BookmarkItem) -> Unit,
    val onSearch: () -> Unit,
    val onJump: () -> Unit,
    val onLastRead: () -> Unit,
    val onReadLog: () -> Unit,
    val onScore: () -> Unit,
    val onDownload: () -> Unit,
    val onSettings: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: QuranRepository,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    actions: HomeActions,
) {
    val titles = stringArrayResource(R.array.home_tabs)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = actions.onSearch) {
                        Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                    }
                    HomeOverflowMenu(actions)
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                titles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = { Text(title) },
                    )
                }
            }
            Crossfade(targetState = selectedTab) { tab ->
                when (tab) {
                    0 -> SuraListScreen(repository = repository, onSuraClick = actions.onSuraClick)
                    else -> BookmarkScreen(repository = repository, onBookmarkClick = actions.onBookmarkClick)
                }
            }
        }
    }
}

@Composable
private fun HomeOverflowMenu(actions: HomeActions) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Filled.MoreVert, contentDescription = null)
    }
    val dismiss = { expanded = false }
    DropdownMenu(expanded = expanded, onDismissRequest = dismiss) {
        MenuRow(stringResource(R.string.jump), Icons.Filled.Navigation, dismiss, actions.onJump)
        MenuRow(stringResource(R.string.go_to_latest_read), Icons.Filled.MenuBook, dismiss, actions.onLastRead)
        MenuRow(stringResource(R.string.readlog), Icons.Filled.History, dismiss, actions.onReadLog)
        MenuRow(stringResource(R.string.score), Icons.Filled.EmojiEvents, dismiss, actions.onScore)
        MenuRow(stringResource(R.string.download), Icons.Filled.Download, dismiss, actions.onDownload)
        MenuRow(stringResource(R.string.setting), Icons.Filled.Settings, dismiss, actions.onSettings)
    }
}

@Composable
private fun MenuRow(
    text: String,
    icon: ImageVector,
    dismiss: () -> Unit,
    action: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = { dismiss(); action() },
    )
}
