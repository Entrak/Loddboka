package com.bearkingsoftware.loddboka.ui.hovedskjerm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.data.Loddbok
import com.bearkingsoftware.loddboka.Routes
import com.bearkingsoftware.loddboka.viewmodel.LoddbokViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Hovedskjerm(navController: NavController, viewModel: LoddbokViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingDelete by remember { mutableStateOf<Loddbok?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            floatingActionButton = {
                if (uiState.loddboker.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                        Spacer(Modifier.width(screenWidth * 0.1f))
                        Box(
                            modifier = Modifier
                                .width(screenWidth * 0.65f)
                                .shadow(
                                    elevation = 16.dp,
                                    shape = SpeechBubbleShape(cornerRadius = 18.dp, tipSize = 15.dp),
                                    ambientColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                                    spotColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.35f)
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = SpeechBubbleShape(cornerRadius = 18.dp, tipSize = 15.dp)
                                )
                                .padding(end = 10.dp) // For tip
                        ) {
                            Text(
                                stringResource(R.string.hovedskjerm_add_loddbok_prompt),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        AddLoddbokFab(onClick = { navController.navigate(Routes.leggTilRediger()) })
                        Spacer(Modifier.width(16.dp))
                    }
                } else {
                    AddLoddbokFab(onClick = { navController.navigate(Routes.leggTilRediger()) })
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .statusBarsPadding()
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = if (uiState.loddboker.isEmpty()) Arrangement.Center else Arrangement.Top
                ) {
                    if (uiState.loddboker.isEmpty()) {
                        Text(
                            stringResource(R.string.hovedskjerm_welcome_title),
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 38.sp
                        )
                        Text(
                            stringResource(R.string.hovedskjerm_welcome_description),
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        LoddbokListe(
                            loddboker = uiState.loddboker,
                            modifier = Modifier.padding(top = 16.dp),
                            onDeleteClick = { loddbok -> pendingDelete = loddbok },
                            onEditClick = { loddbok ->
                                navController.navigate(Routes.leggTilRediger(loddbok.id))
                            }
                        )
                    }
                }

            }
        }

        pendingDelete?.let { book ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text(stringResource(R.string.loddbokliste_delete_dialog_title)) },
                text = { Text(stringResource(R.string.loddbokliste_delete_dialog_text)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteLoddbok(book)
                            pendingDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.loddbokliste_delete_dialog_confirm))
                    }
                },
                dismissButton = {
                    Button(onClick = { pendingDelete = null }) {
                        Text(stringResource(R.string.loddbokliste_delete_dialog_cancel))
                    }
                }
            )
        }

    }
}

@Composable
private fun AddLoddbokFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(72.dp)
            .border(
                BorderStroke(4.dp, MaterialTheme.colorScheme.secondary),
                CircleShape
            )
            .padding(4.dp),
        shape = CircleShape,
    ) {
        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.hovedskjerm_add_loddbok_cd))
    }
}
