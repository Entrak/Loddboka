package com.bearkingsoftware.loddboka.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bearkingsoftware.loddboka.R
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageSelector(
    onLocaleChange: (Locale) -> Unit
) {
    Column {
        Text(stringResource(R.string.settings_language_select))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            maxItemsInEachRow = 2
        ) {
            supportedLanguages.forEach { language ->
                Button(
                    onClick = { onLocaleChange(Locale.forLanguageTag(language.code)) },
                    modifier = Modifier.weight(1f)
                ) {
                    language.flag?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = null, // Decorative
                            modifier = Modifier.size(24.dp).scale(0.75f)
                        )
                    }
                    Text(language.text, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
