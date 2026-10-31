package education.mahmoud.quranyapp.feature.bookmark_fragment.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import education.mahmoud.quranyapp.datalayer.local.room.AyahItem
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranFont
import education.mahmoud.quranyapp.utils.Data
import education.mahmoud.quranyapp.utils.NumberHelper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.BookmarkItem
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Compose replacement for the legacy Bookmark fragment + RecyclerView adapter.
 * Streams bookmarks from Room via RxJava and mirrors them into Compose state.
 */
@Composable
fun BookmarkScreen(
    repository: QuranRepository,
    onBookmarkClick: (BookmarkItem) -> Unit,
    modifier: Modifier = Modifier,
    /** extra bottom space so the last items scroll above the navigation bar (edge-to-edge) */
    bottomInset: Dp = 0.dp,
    /** opens the reader on a highlighted ayah */
    onHighlightClick: (AyahItem) -> Unit = {},
) {
    var bookmarks by remember { mutableStateOf<List<BookmarkItem>?>(null) }
    var highlights by remember { mutableStateOf<List<AyahItem>?>(null) }

    // highlights live in SharedPreferences; reload whenever the screen comes back from the reader
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, repository) {
        val load = { highlights = repository.getHighlightedAyahItems() }
        load()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(repository) {
        val disposable = repository.bookmarks
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list -> bookmarks = list }, { bookmarks = emptyList() })
        onDispose { disposable.dispose() }
    }

    val list = bookmarks
    val marked = highlights

    Box(modifier = modifier.fillMaxSize()) {
        when {
            list == null || marked == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            list.isEmpty() && marked.isEmpty() -> EmptyBookmarks(Modifier.align(Alignment.Center))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 12.dp + bottomInset),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (marked.isNotEmpty()) {
                    item(key = "highlights_header") { SectionHeader(stringResource(R.string.highlights_title)) }
                    items(marked, key = { "h" + it.ayahIndex }) { ayah ->
                        HighlightCard(
                            ayah = ayah,
                            onClick = { onHighlightClick(ayah) },
                            onDelete = {
                                repository.setAyahHighlighted(ayah.ayahIndex, false)
                                highlights = repository.getHighlightedAyahItems()
                            },
                        )
                    }
                    if (list.isNotEmpty()) {
                        item(key = "bookmarks_header") { SectionHeader(stringResource(R.string.bookmarked_pages_title)) }
                    }
                }
                items(list, key = { "b" + it.id }) { item ->
                    BookmarkCard(
                        item = item,
                        onClick = { onBookmarkClick(item) },
                        onDelete = { repository.deleteBookmark(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 6.dp, end = 4.dp),
        color = MaterialTheme.colorScheme.primary,
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
    )
}

/** A highlighted ayah: amber edge, "sura - ayah N" in the system font, the ayah in the Quran font. */
@Composable
private fun HighlightCard(
    ayah: AyahItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(HighlightAmber),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 12.dp, bottom = 12.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.ayah_reference,
                        Data.SURA_NAMES.getOrNull(ayah.surahIndex - 1).orEmpty(),
                        NumberHelper.getArabicNumber(ayah.ayahInSurahIndex),
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                Text(
                    text = ayah.text,
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    fontFamily = QuranFont,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = stringResource(R.string.ayah_action_remove_highlight),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private val HighlightAmber = Color(0xFFFFC107)

@Composable
private fun EmptyBookmarks(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Filled.Bookmark,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.no_bookmarks),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.highlight_hint),
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            fontFamily = FontFamily.Default,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BookmarkCard(
    item: BookmarkItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.suraName.orEmpty(),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.page_num) + " " + item.pageNum,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = stringResource(R.string.deleted),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
