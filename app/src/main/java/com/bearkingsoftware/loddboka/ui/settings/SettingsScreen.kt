package com.bearkingsoftware.loddboka.ui.settings

import android.widget.Toast
import android.net.Uri
import android.content.Intent
import android.content.Context
import android.content.ClipboardManager
import android.content.ClipData
import android.content.ActivityNotFoundException
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bearkingsoftware.loddboka.BuildConfig
import com.bearkingsoftware.loddboka.LocalLocaleManager
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.viewmodel.LoddbokViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LoddbokViewModel,
    onOpenWhatIsLoddboka: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val localeManager = LocalLocaleManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showResetDrawConfirm by remember { mutableStateOf(false) }

    if (showResetDrawConfirm) {
        // Full draw reset: winner history cleared, every ticket back in the pool, books kept.
        AlertDialog(
            onDismissRequest = { showResetDrawConfirm = false },
            title = { Text(stringResource(R.string.settings_reset_draw_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_draw_confirm_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDrawConfirm = false
                        viewModel.initialiserTrekning()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(stringResource(R.string.settings_reset_draw_confirm))
                }
            },
            dismissButton = {
                Button(onClick = { showResetDrawConfirm = false }) {
                    Text(stringResource(R.string.settings_reset_draw_cancel))
                }
            }
        )
    }

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
        contentWindowInsets = WindowInsets(0),
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
                Button(onClick = { showResetDrawConfirm = true }) {
                    Text(text = stringResource(R.string.settings_reset_winning_ticket_list))
                }
                Button(onClick = { viewModel.showResetDialog() }) {
                    Text(text = stringResource(R.string.settings_reset_raffle_ticket_book_entries))
                }
                LanguageSelector(
                    onLocaleChange = { locale ->
                        scope.launch {
                            // Persist + AppCompat locales; MainScreen LocalResources
                            // refreshes from localeFlow (same path as first-run).
                            localeManager.setLocale(locale)
                        }
                    }
                )
                OutlinedButton(onClick = onOpenWhatIsLoddboka) {
                    Text(text = stringResource(R.string.first_run_intro_title))
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "ver. ${BuildConfig.VERSION_NAME}",
                    color = if (isSystemInDarkTheme()) {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                TextButton(onClick = { openContactEmail(context) }) {
                    Text(text = stringResource(R.string.settings_contact_us))
                }
            }
        }
    }
}

private const val CONTACT_EMAIL = "bearkingsoftware@gmail.com"

private fun openContactEmail(context: Context) {
    val subject = context.getString(
        R.string.contact_subject_app_name
    ) + " " + BuildConfig.VERSION_NAME + " (vc" + BuildConfig.VERSION_CODE + ")"
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(CONTACT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("email", CONTACT_EMAIL))
        Toast.makeText(
            context,
            context.getString(R.string.settings_contact_no_email_app),
            Toast.LENGTH_LONG
        ).show()
    }
}
