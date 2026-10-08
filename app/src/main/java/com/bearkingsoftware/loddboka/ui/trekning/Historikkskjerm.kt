package com.bearkingsoftware.loddboka.ui.trekning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bearkingsoftware.loddboka.LocalLocaleManager
import com.bearkingsoftware.loddboka.LocaleManager
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.data.Farge
import com.bearkingsoftware.loddboka.ui.color
import com.bearkingsoftware.loddboka.ui.getContrastingTextColor
import com.bearkingsoftware.loddboka.ui.toStringResource
import com.bearkingsoftware.loddboka.viewmodel.Vinner
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Historikkskjerm(
    vinnere: List<Vinner>,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val localeManager = LocalLocaleManager.current
    val scope = rememberCoroutineScope()
    val sort by localeManager.winnersSortFlow.collectAsState(initial = LocaleManager.WINNERS_SORT_NEWEST)
    val showTime by localeManager.winnersShowTimeFlow.collectAsState(initial = false)
    val displayed = if (sort == LocaleManager.WINNERS_SORT_DRAW) vinnere.asReversed() else vinnere
    val pdfSubtitle = stringResource(
        if (sort == LocaleManager.WINNERS_SORT_DRAW) {
            R.string.winners_pdf_note_draw
        } else {
            R.string.winners_pdf_note_newest
        }
    )
    val timeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
    val labels = WinnerExportLabels(
        pdfTitle = stringResource(R.string.winners_pdf_title),
        book = stringResource(R.string.export_column_book),
        ticket = stringResource(R.string.export_column_ticket),
        drawn = stringResource(R.string.export_column_drawn),
        chooserTitle = stringResource(R.string.share_results),
        colorName = { farge ->
            when (farge) {
                Farge.RØD -> context.getString(R.string.color_red)
                Farge.BLÅ -> context.getString(R.string.color_blue)
                Farge.GRØNN -> context.getString(R.string.color_green)
                Farge.GUL -> context.getString(R.string.color_yellow)
                Farge.ORANSJE -> context.getString(R.string.color_orange)
                Farge.ROSA -> context.getString(R.string.color_pink)
                Farge.LILLA -> context.getString(R.string.color_purple)
                Farge.HVIT -> context.getString(R.string.color_white)
            }
        },
    )
    val sortOptions = listOf(
        LocaleManager.WINNERS_SORT_NEWEST to R.string.winners_sort_newest,
        LocaleManager.WINNERS_SORT_DRAW to R.string.winners_sort_draw,
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.historikk_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.historikk_close))
                    }
                },
                actions = {
                    if (vinnere.isNotEmpty()) {
                        TextButton(
                            onClick = { shareWinnerResults(context, displayed, labels, pdfSubtitle) }
                        ) {
                            Text(stringResource(R.string.share_results))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                sortOptions.forEachIndexed { index, (value, labelRes) ->
                    SegmentedButton(
                        selected = sort == value,
                        onClick = { scope.launch { localeManager.setWinnersSort(value) } },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = sortOptions.size),
                        label = { Text(stringResource(labelRes)) },
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = showTime,
                    onCheckedChange = { checked ->
                        scope.launch { localeManager.setWinnersShowTime(checked) }
                    }
                )
                Text(
                    text = stringResource(R.string.winners_show_time),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(displayed) { index, vinner ->
                    HistorikkRad(
                        vinner = vinner,
                        index = index + 1,
                        drawnTime = if (showTime) timeFormat.format(Date(vinner.trukketMillis)) else null,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun HistorikkRad(vinner: Vinner, index: Int, drawnTime: String?) {
    val backgroundColor = vinner.loddbok.farge.color
    val textColor = getContrastingTextColor(backgroundColor)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.width(32.dp)
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${vinner.loddbok.farge.toStringResource()} ${vinner.loddbok.bokstav} - ${vinner.loddNummer}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = textColor,
            )
            if (drawnTime != null) {
                Text(
                    text = drawnTime,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = textColor,
                )
            }
        }
    }
}
