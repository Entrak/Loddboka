package com.bearkingsoftware.loddboka.ui.hovedskjerm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.data.Loddbok
import com.bearkingsoftware.loddboka.ui.getContrastingTextColor
import com.bearkingsoftware.loddboka.ui.toStringResource

@Composable
fun LoddbokListe(
    loddboker: List<Loddbok>,
    modifier: Modifier = Modifier,
    onDeleteClick: (Loddbok) -> Unit,
    onEditClick: (Loddbok) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(loddboker, key = { it.id }) { loddbok ->
            val textColor = getContrastingTextColor(loddbok.farge.color)
            val darkTheme = isSystemInDarkTheme()
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(2.dp, Color.Black),
                colors = CardDefaults.cardColors(
                    containerColor = if (darkTheme) loddbok.farge.color else loddbok.farge.color.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.loddbokliste_farge)}: ${loddbok.farge.toStringResource()}   ${stringResource(R.string.loddbokliste_bokstav)}: ${loddbok.bokstav}   ${stringResource(R.string.loddbokliste_nummer)}: ${loddbok.start}-${loddbok.end}",
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(onClick = { onEditClick(loddbok) }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.loddbokliste_edit), tint = textColor)
                        }
                        IconButton(onClick = { onDeleteClick(loddbok) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.loddbokliste_delete), tint = textColor)
                        }
                    }
                }
            }
        }
    }
}
