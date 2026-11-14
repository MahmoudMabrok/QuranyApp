package education.mahmoud.quranyapp.feature.show_sura_list.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.home_Activity.compose.EvenRowColor
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Compose replacement for the legacy Surah-list fragment + RecyclerView adapter.
 * Observes the Surah list from Room off the main thread and renders it as a two-column grid.
 */
@Composable
fun SuraListScreen(
    repository: QuranRepository,
    onSuraClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    /** extra bottom space so the last rows scroll above the navigation bar (edge-to-edge) */
    bottomInset: Dp = 0.dp,
) {
    // Observe Room so the list fills in as soon as the first-launch import finishes,
    // instead of showing "no data" until the app is restarted.
    var surasState by remember { mutableStateOf<List<SuraItem>?>(null) }
    DisposableEffect(repository) {
        val disposable = repository.surasStream
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list -> surasState = list }, { surasState = emptyList() })
        onDispose { disposable.dispose() }
    }
    val suras = surasState

    Box(modifier = modifier.fillMaxSize()) {
        when {
            suras == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            suras.isEmpty() -> Text(
                text = stringResource(R.string.tv_no_quran_data),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
            else -> Column(Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        top = 12.dp,
                        end = 12.dp,
                        bottom = bottomInset + 12.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = suras,
                        key = { it.index },
                        contentType = { "sura" },
                    ) { sura ->
                        SuraRow(
                            sura = sura,
                            even = sura.index % 2 == 0,
                            onClick = { onSuraClick(sura.index) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuraRow(
    sura: SuraItem,
    even: Boolean,
    onClick: () -> Unit,
) {
    val iconRes = remember(sura.revelationType) {
        if (sura.revelationType == "Meccan") R.drawable.ic_makka else R.drawable.ic_madinah
    }
    val icon = painterResource(iconRes)

    val displayName = sura.name.orEmpty().removePrefix("سورة").trimStart()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (even) EvenRowColor else MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = displayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Icon(
                        painter = icon,
                        contentDescription = sura.revelationType,
                        tint = Color.Unspecified,
                        modifier = Modifier.width(28.dp).padding(horizontal = 3.dp),
                    )
                    Text(
                        text = sura.index.toString(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuraRowPreview() {
    QuranyTheme {
        SuraRow(
            sura = SuraItem(1, 7, "الفاتحة", "Al-Faatiha", "The Opening", "Meccan"),
            even = true,
            onClick = {},
        )
    }
}
