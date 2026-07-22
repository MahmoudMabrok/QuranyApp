package education.mahmoud.quranyapp.feature.show_sura_list.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import education.mahmoud.quranyapp.R
import education.mahmoud.quranyapp.datalayer.QuranRepository
import education.mahmoud.quranyapp.datalayer.local.room.SuraItem
import education.mahmoud.quranyapp.feature.home_Activity.compose.EvenRowColor
import education.mahmoud.quranyapp.feature.home_Activity.compose.QuranyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Compose replacement for the legacy Surah-list fragment + RecyclerView adapter.
 * Loads the Surah list off the main thread and renders it as a [LazyColumn].
 */
@Composable
fun SuraListScreen(
    repository: QuranRepository,
    onSuraClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val surasState by produceState<List<SuraItem>?>(initialValue = null, repository) {
        value = withContext(Dispatchers.IO) { repository.suras }
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
                SuraHeaderRow()
                Divider()
                LazyColumn(Modifier.fillMaxSize()) {
                    items(suras, key = { it.index }) { sura ->
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
private fun SuraHeaderRow() {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderCell(stringResource(R.string.sura_num), weight = 1f)
            HeaderCell(stringResource(R.string.sura_name), weight = 3f, align = TextAlign.End)
            HeaderCell(stringResource(R.string.start_page), weight = 1f)
            HeaderCell(stringResource(R.string.ayahs_count), weight = 1f)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(
    text: String,
    weight: Float,
    align: TextAlign = TextAlign.Center,
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        textAlign = align,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
private fun SuraRow(
    sura: SuraItem,
    even: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (even) EvenRowColor else androidx.compose.ui.graphics.Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = sura.index.toString(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = sura.name.orEmpty(),
            modifier = Modifier.weight(3f),
            textAlign = TextAlign.End,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Icon(
            painter = painterResource(
                if (sura.revelationType == "Meccan") R.drawable.ic_makka else R.drawable.ic_madinah,
            ),
            contentDescription = sura.revelationType,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
        )
        Text(
            text = sura.startIndex.toString(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = sura.numOfAyahs.toString(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
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
