package com.bearkingsoftware.loddboka.ui.trekning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.data.TicketPool
import com.bearkingsoftware.loddboka.ui.color
import com.bearkingsoftware.loddboka.ui.getContrastingTextColor
import com.bearkingsoftware.loddboka.ui.toStringResource
import com.bearkingsoftware.loddboka.viewmodel.LoddbokViewModel
import com.bearkingsoftware.loddboka.viewmodel.Vinner

@Composable
fun Trekningsskjerm(
    viewModel: LoddbokViewModel,
    onNavigateToHistorikk: () -> Unit,
    onAddLoddbok: () -> Unit
) {
    val trekningState by viewModel.trekningState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    // No books (or no ticket numbers) set up at all — distinct from a pool drawn empty this session.
    val noTicketsSetUp = uiState.isLoaded &&
        uiState.loddboker.none { TicketPool.rangeSize(it.start..it.end) > 0L }
    var showEmptyPoolDialog by remember { mutableStateOf(false) }
    var showNewDrawConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(
        uiState.isLoaded,
        trekningState.remainingByBookId.isEmpty(),
        trekningState.sisteVinner,
        trekningState.vinnere.isEmpty(),
        trekningState.sessionDrawn.isEmpty()
    ) {
        // Fresh draw session: no remaining ranges yet and no draws made.
        // Wait for saved books to load so the pool isn't built from an empty list.
        if (uiState.isLoaded &&
            trekningState.remainingByBookId.isEmpty() &&
            trekningState.sisteVinner == null &&
            trekningState.vinnere.isEmpty() &&
            trekningState.sessionDrawn.isEmpty()
        ) {
            viewModel.initialiserTrekning()
        }
    }

    // Reset end dialog when pool is refilled (e.g. New draw).
    LaunchedEffect(trekningState.ingenLodd) {
        if (!trekningState.ingenLodd) {
            showEmptyPoolDialog = false
        }
    }

    if (showEmptyPoolDialog && trekningState.ingenLodd && !noTicketsSetUp) {
        AlertDialog(
            onDismissRequest = {
                showEmptyPoolDialog = false
            },
            title = { Text(stringResource(R.string.trekning_empty_pool_title)) },
            text = { Text(stringResource(R.string.trekning_no_tickets_left)) },
            confirmButton = {
                Button(
                    onClick = {
                        showEmptyPoolDialog = false
                        onNavigateToHistorikk()
                    }
                ) {
                    Text(stringResource(R.string.trekning_see_winners))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showEmptyPoolDialog = false
                        showNewDrawConfirm = true
                    }
                ) {
                    Text(stringResource(R.string.trekning_new_draw))
                }
            }
        )
    }

    if (showNewDrawConfirm) {
        AlertDialog(
            onDismissRequest = { showNewDrawConfirm = false },
            title = { Text(stringResource(R.string.trekning_new_draw_confirm_title)) },
            text = { Text(stringResource(R.string.trekning_new_draw_confirm_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        showNewDrawConfirm = false
                        viewModel.startNewDraw()
                    }
                ) {
                    Text(stringResource(R.string.trekning_new_draw_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDrawConfirm = false }) {
                    Text(stringResource(R.string.settings_reset_dialog_cancel))
                }
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        val screenHeight = maxHeight
        val poolEmpty = trekningState.ingenLodd

        // Hovedinnhold boks (Vinnerlodd eller placeholder)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.1f)
                .fillMaxWidth(0.7f)
                .height(screenHeight * 0.5f),
            contentAlignment = Alignment.Center
        ) {
            if (trekningState.sisteVinner != null) {
                VinnerLodd(vinner = trekningState.sisteVinner!!)
            } else if (noTicketsSetUp) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.trekning_no_books_title),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.trekning_no_books_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (poolEmpty) {
                Text(stringResource(R.string.trekning_no_tickets_left))
            } else {
                Text(stringResource(R.string.trekning_press_button_to_draw))
            }
        }

        // Draw button, Add raffle book when nothing is set up,
        // or Finished when pool is empty (opens end dialog; no auto-advance)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.65f)
                .height(screenHeight * 0.1f),
            contentAlignment = Alignment.Center
        ) {
            if (noTicketsSetUp) {
                Button(
                    onClick = onAddLoddbok,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(stringResource(R.string.add_loddbok))
                }
            } else if (poolEmpty) {
                Button(
                    onClick = { showEmptyPoolDialog = true },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Text(stringResource(R.string.trekning_finished))
                }
            } else {
                Button(
                    onClick = { viewModel.trekkVinner() },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    val buttonText = if (trekningState.sisteVinner != null) {
                        stringResource(R.string.trekning_draw_new_winner)
                    } else {
                        stringResource(R.string.trekning_draw_winner)
                    }
                    Text(buttonText)
                }
            }
        }

        // Historikk ( viser 3 forrige vinnere )
        if (trekningState.vinnere.size > 1) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = screenHeight * 0.8f)
                    .height(screenHeight * 0.25f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(stringResource(R.string.trekning_previous_winners), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                trekningState.vinnere.drop(1).take(3).forEach { vinner ->
                    val backgroundColor = vinner.loddbok.farge.color
                    val textColor = getContrastingTextColor(backgroundColor)
                    Box(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(backgroundColor)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${vinner.loddbok.farge.toStringResource()} ${vinner.loddbok.bokstav} - ${vinner.loddNummer}",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = textColor
                        )
                    }
                }
            }
        }

        if (trekningState.vinnere.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
            ) {
                Button(onClick = { onNavigateToHistorikk() }) {
                    Text(stringResource(R.string.trekning_all_winners))
                }
            }
        }
    }
}

@Composable
private fun VinnerLodd(vinner: Vinner) {
    val textColor = getContrastingTextColor(vinner.loddbok.farge.color)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(vinner.loddbok.farge.color)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.trekning_winner_ticket),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val fontSize = (32.sp.value * (maxWidth.value / 320f)).coerceIn(24f, 42f).sp
                Text(
                    text = vinner.loddbok.farge.toStringResource(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val fontSize = (48.sp.value * (maxWidth.value / 320f)).coerceIn(32f, 80f).sp
                Text(
                    text = vinner.loddbok.bokstav.toString(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val fontSize = (80.sp.value * (maxWidth.value / 320f)).coerceIn(44f, 120f).sp
                Text(
                    text = vinner.loddNummer.toString(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}