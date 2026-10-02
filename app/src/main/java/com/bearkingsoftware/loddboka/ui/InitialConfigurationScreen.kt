package com.bearkingsoftware.loddboka.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearkingsoftware.loddboka.LocalLocaleManager
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.ui.settings.LanguageSelector
import kotlinx.coroutines.launch

@Composable
fun InitialConfigurationScreen(
    onConfigurationComplete: () -> Unit
) {
    val localeManager = LocalLocaleManager.current
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.first_run_intro_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
            Text(
                text = stringResource(R.string.first_run_intro_body),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
            LanguageSelector { locale ->
                scope.launch {
                    // Persists choice + AppCompat locales; navigate leaves first-run
                    // immediately (recreate would restore this destination via NavHost).
                    localeManager.setLocale(locale)
                    onConfigurationComplete()
                }
            }
        }
    }
}
