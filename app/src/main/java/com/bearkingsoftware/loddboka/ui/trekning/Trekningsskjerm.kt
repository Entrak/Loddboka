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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.ui.getContrastingTextColor
import com.bearkingsoftware.loddboka.ui.toStringResource
import com.bearkingsoftware.loddboka.viewmodel.LoddbokViewModel
import com.bearkingsoftware.loddboka.viewmodel.Vinner

@Composable
fun Trekningsskjerm(
    viewModel: LoddbokViewModel,
    onNavigateToHistorikk: () -> Unit
) {
    val trekningState by viewModel.trekningState.collectAsState()

    LaunchedEffect(trekningState.loddTilTrekking.isEmpty() && trekningState.sisteVinner == null) {
        if (trekningState.loddTilTrekking.isEmpty() && trekningState.sisteVinner == null) {
            viewModel.initialiserTrekning()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val screenHeight = maxHeight

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
            } else if (trekningState.ingenLodd) {
                Text(stringResource(R.string.trekning_no_tickets_left))
            } else {
                Text(stringResource(R.string.trekning_press_button_to_draw))
            }
        }

        // Knapp for trekking
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.65f)
                .height(screenHeight * 0.1f),
            contentAlignment = Alignment.Center
        ) {
            if (trekningState.ingenLodd) {
                Button(onClick = { viewModel.initialiserTrekning() }, modifier = Modifier.fillMaxHeight()) {
                    Text(stringResource(R.string.trekning_start_again))
                }
            } else {
                Button(onClick = { viewModel.trekkVinner() }, modifier = Modifier.fillMaxHeight()) {
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