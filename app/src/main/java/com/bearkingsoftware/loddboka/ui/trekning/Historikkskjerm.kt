package com.bearkingsoftware.loddboka.ui.trekning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.ui.getContrastingTextColor
import com.bearkingsoftware.loddboka.ui.toStringResource
import com.bearkingsoftware.loddboka.viewmodel.Vinner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Historikkskjerm(
    vinnere: List<Vinner>,
    onClose: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.historikk_title)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.historikk_close))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            itemsIndexed(vinnere) { index, vinner ->
                HistorikkRad(vinner = vinner, index = index + 1)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun HistorikkRad(vinner: Vinner, index: Int) {
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
        Text(
            text = "${vinner.loddbok.farge.toStringResource()} ${vinner.loddbok.bokstav} - ${vinner.loddNummer}",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
    }
}