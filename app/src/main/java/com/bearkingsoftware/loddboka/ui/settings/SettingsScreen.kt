package com.bearkingsoftware.loddboka.ui.settings

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bearkingsoftware.loddboka.BuildConfig
import com.bearkingsoftware.loddboka.LocaleManager
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.viewmodel.LoddbokViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LoddbokViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val localeManager = remember { LocaleManager(context) }
    val scope = rememberCoroutineScope()

    if (uiState.showResetDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideResetDialog() },
            title = { Text(stringResource(R.string.settings_reset_dialog_title)) },
            text = { Text(stringResource(R.string.settings_reset_dialog_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetLoddbøker()
                        viewModel.hideResetDialog()
                    }
                ) {
                    Text(stringResource(R.string.settings_reset_dialog_confirm))
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.hideResetDialog() }) {
                    Text(stringResource(R.string.settings_reset_dialog_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { viewModel.initialiserTrekning() }) {
                    Text(text = stringResource(R.string.settings_reset_winning_ticket_list))
                }
                Button(onClick = { viewModel.showResetDialog() }) {
                    Text(text = stringResource(R.string.settings_reset_raffle_ticket_book_entries))
                }
                LanguageSelector(
                    onLocaleChange = { locale ->
                        scope.launch {
                            localeManager.setLocale(locale)
                            activity?.recreate()
                        }
                    }
                )
            }
            Text(
                text = "ver. ${BuildConfig.VERSION_NAME}",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(padding)
                    .padding(8.dp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
            )
        }
    }
}
