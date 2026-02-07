package com.bearkingsoftware.loddboka.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
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

data class Language(val code: String, val name: String, val flag: String, val isSami: Boolean = false)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguageSelector(
    currentLocale: Locale,
    useSystemLanguage: Boolean,
    onUseSystemLanguageChange: (Boolean) -> Unit,
    onLocaleChange: (Locale) -> Unit
) {
    val languages = listOf(
        Language("en", "English", "🇬🇧"),
        Language("nb", "Norsk", "🇳🇴"),
        Language("sv", "Svenska", "🇸🇪"),
        Language("da", "Dansk", "🇩🇰"),
        Language("fi", "Suomi", "🇫🇮"),
        Language("se", "Sámegiella", "", isSami = true),
        Language("fr", "Français", "🇫🇷"),
        Language("de", "Deutsch", "🇩🇪")
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.settings_language_use_system))
            Switch(
                checked = useSystemLanguage,
                onCheckedChange = { onUseSystemLanguageChange(it) }
            )
        }
        if (!useSystemLanguage) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                maxItemsInEachRow = 2
            ) {
                languages.forEach { language ->
                    Button(
                        onClick = { onLocaleChange(Locale.forLanguageTag(language.code)) },
                        modifier = Modifier.weight(1f)
                    ) {
                        if (language.isSami) {
                            Image(
                                painter = painterResource(id = R.drawable.sami_flag),
                                contentDescription = null, // Decorative
                                modifier = Modifier.size(24.dp).scale(0.75f)
                            )
                        } else {
                            Text(language.flag)
                        }
                        Text(language.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}