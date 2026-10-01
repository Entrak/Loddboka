package com.bearkingsoftware.loddboka.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bearkingsoftware.loddboka.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesterTasksScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val formUrl = stringResource(R.string.tester_feedback_form_url)
    val feedbackEmail = stringResource(R.string.tester_feedback_email)
    val noAppMessage = stringResource(R.string.tester_no_app)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var taskAddBooks by rememberSaveable { mutableStateOf(false) }
    var taskDraw by rememberSaveable { mutableStateOf(false) }
    var taskLanguage by rememberSaveable { mutableStateOf(false) }
    var taskDelete by rememberSaveable { mutableStateOf(false) }

    fun openSafely(intent: Intent) {
        val chooser = Intent.createChooser(intent, null)
        val canResolve = chooser.resolveActivity(context.packageManager) != null ||
            intent.resolveActivity(context.packageManager) != null
        if (!canResolve) {
            scope.launch { snackbarHostState.showSnackbar(noAppMessage) }
            return
        }
        runCatching { context.startActivity(chooser) }
            .onFailure {
                scope.launch { snackbarHostState.showSnackbar(noAppMessage) }
            }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tester_tasks_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.tester_tasks_intro),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.tester_tasks_checklist_heading),
                style = MaterialTheme.typography.titleMedium
            )
            TaskCheckRow(
                checked = taskAddBooks,
                onCheckedChange = { taskAddBooks = it },
                label = stringResource(R.string.tester_task_add_books)
            )
            TaskCheckRow(
                checked = taskDraw,
                onCheckedChange = { taskDraw = it },
                label = stringResource(R.string.tester_task_draw)
            )
            TaskCheckRow(
                checked = taskLanguage,
                onCheckedChange = { taskLanguage = it },
                label = stringResource(R.string.tester_task_language)
            )
            TaskCheckRow(
                checked = taskDelete,
                onCheckedChange = { taskDelete = it },
                label = stringResource(R.string.tester_task_delete)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    openSafely(Intent(Intent.ACTION_VIEW, Uri.parse(formUrl)))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.tester_send_feedback))
            }
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$feedbackEmail")
                        putExtra(Intent.EXTRA_SUBJECT, "Loddboka closed-test feedback")
                    }
                    openSafely(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.tester_email_feedback))
            }
            Text(
                text = stringResource(R.string.tester_tasks_footer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskCheckRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, modifier = Modifier.padding(start = 4.dp))
    }
}
