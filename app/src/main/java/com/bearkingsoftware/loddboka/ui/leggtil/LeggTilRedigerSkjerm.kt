package com.bearkingsoftware.loddboka.ui.leggtil

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.navigation.NavController
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.data.Farge
import com.bearkingsoftware.loddboka.data.Loddbok
import com.bearkingsoftware.loddboka.ui.toStringResource
import com.bearkingsoftware.loddboka.viewmodel.LoddbokViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeggTilRedigerSkjerm(
    navController: NavController,
    viewModel: LoddbokViewModel,
    id: Long
) {
    var bokstav by remember { mutableStateOf(TextFieldValue("")) }
    var start by remember { mutableStateOf(TextFieldValue("1")) }     // ← Default visible
    var end by remember { mutableStateOf(TextFieldValue("100")) }     // ← Default visible
    var selectedFarge by remember { mutableStateOf<Farge?>(null) }
    var bokstavError by remember { mutableStateOf(false) }
    var startError by remember { mutableStateOf(false) }
    var endError by remember { mutableStateOf(false) }
    var fargeError by remember { mutableStateOf(false) }
    val isEditing = id != -1L

    LaunchedEffect(id) {
        if (isEditing) {
            viewModel.getLoddbok(id)?.let {
                bokstav = TextFieldValue(it.bokstav.toString())
                start = TextFieldValue(it.start.toString())
                end = TextFieldValue(it.end.toString())
                selectedFarge = it.farge
            }
        }
    }

    LeggTilRedigerSkjermContent(
        isEditing = isEditing,
        bokstav = bokstav,
        start = start,
        end = end,
        selectedFarge = selectedFarge,
        bokstavError = bokstavError,
        startError = startError,
        endError = endError,
        fargeError = fargeError,
        onBokstavChange = {
            if (it.text.length <= 1) {
                bokstav = it
                bokstavError = false
            }
        },
        onStartChange = {
            start = it
            startError = false
        },
        onEndChange = {
            end = it
            endError = false
        },
        onFargeChange = {
            selectedFarge = it
            fargeError = false
        },
        onSaveClick = {
            val bokstavChar = bokstav.text.uppercase().firstOrNull()
            val startInt = if (start.text.isBlank()) 1 else start.text.toIntOrNull()
            val endInt = if (end.text.isBlank()) 100 else end.text.toIntOrNull()

            bokstavError = bokstavChar == null || bokstavChar !in 'A'..'Z'
            startError = startInt == null
            endError = endInt == null || (startInt != null && endInt < startInt)
            fargeError = selectedFarge == null

            if (!bokstavError && !startError && !endError && !fargeError) {
                val loddbok = Loddbok(
                    id = if (isEditing) id else System.currentTimeMillis(),
                    farge = selectedFarge!!,
                    bokstav = bokstavChar!!,
                    start = startInt!!,
                    end = endInt!!
                )
                if (isEditing) {
                    viewModel.updateLoddbok(loddbok)
                } else {
                    viewModel.addLoddbok(loddbok)
                }
                navController.popBackStack()
            }
        },
        onCloseClick = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LeggTilRedigerSkjermContent(
    isEditing: Boolean,
    bokstav: TextFieldValue,
    onBokstavChange: (TextFieldValue) -> Unit,
    start: TextFieldValue,
    onStartChange: (TextFieldValue) -> Unit,
    end: TextFieldValue,
    onEndChange: (TextFieldValue) -> Unit,
    selectedFarge: Farge?,
    onFargeChange: (Farge) -> Unit,
    bokstavError: Boolean,
    startError: Boolean,
    endError: Boolean,
    fargeError: Boolean,
    onSaveClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    var showBokstavHelp by remember { mutableStateOf(false) }
    var showStartHelp by remember { mutableStateOf(false) }
    var showEndHelp by remember { mutableStateOf(false) }

    fun getTextColor(backgroundColor: Color): Color {
        val backgroundArgb = backgroundColor.toArgb()
        val contrastWithWhite = ColorUtils.calculateContrast(Color.White.toArgb(), backgroundArgb)
        val contrastWithBlack = ColorUtils.calculateContrast(Color.Black.toArgb(), backgroundArgb)
        return if (contrastWithWhite > contrastWithBlack) Color.White else Color.Black
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(if (isEditing) R.string.edit_loddbok else R.string.add_loddbok),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onCloseClick) {
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
            // Color selection section (unchanged)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    stringResource(R.string.legg_til_rediger_farge_label),
                    style = MaterialTheme.typography.titleLarge
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Farge.entries.forEach { farge ->
                        val selected = selectedFarge == farge
                        val scale by animateFloatAsState(if (selected) 1.3f else 1.0f, label = "scale")
                        val shadow by animateDpAsState(if (selected) 8.dp else 0.dp, label = "shadow")
                        val cornerRadius = 4.dp

                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .scale(scale)
                                .shadow(shadow, RoundedCornerShape(cornerRadius))
                                .background(farge.color, RoundedCornerShape(cornerRadius))
                                .border(
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                                    RoundedCornerShape(cornerRadius)
                                )
                                .clickable { onFargeChange(farge) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = farge.toStringResource(),
                                color = getTextColor(farge.color)
                            )
                        }
                    }
                }
                if (fargeError) {
                    Text(
                        text = stringResource(R.string.legg_til_rediger_farge_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            // Letter field
            OutlinedTextField(
                value = bokstav,
                label = { Text(stringResource(R.string.legg_til_rediger_bokstav_label)) },
                onValueChange = onBokstavChange,
                placeholder = { Text(stringResource(R.string.legg_til_rediger_bokstav_placeholder)) },
                singleLine = true,
                isError = bokstavError,
                supportingText = { if (bokstavError) Text(stringResource(R.string.legg_til_rediger_bokstav_error)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showBokstavHelp = true }) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.show_help))
                    }
                    DropdownMenu(
                        expanded = showBokstavHelp,
                        onDismissRequest = { showBokstavHelp = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.bokstav_help)) },
                            onClick = { showBokstavHelp = false }
                        )
                    }
                }
            )

            // Start + End fields
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = start,
                    onValueChange = onStartChange,
                    label = { Text(stringResource(R.string.legg_til_rediger_start_label)) },
                    placeholder = { Text(stringResource(R.string.legg_til_rediger_start_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = startError,
                    supportingText = { if (startError) Text(stringResource(R.string.legg_til_rediger_start_error)) },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showStartHelp = true }) {
                            Icon(Icons.Default.Info, contentDescription = stringResource(R.string.show_help))
                        }
                        DropdownMenu(
                            expanded = showStartHelp,
                            onDismissRequest = { showStartHelp = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.start_help)) },
                                onClick = { showStartHelp = false }
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = end,
                    onValueChange = onEndChange,
                    label = { Text(stringResource(R.string.legg_til_rediger_slutt_label)) },
                    placeholder = { Text(stringResource(R.string.legg_til_rediger_slutt_placeholder)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSaveClick() }
                    ),
                    isError = endError,
                    supportingText = { if (endError) Text(stringResource(R.string.legg_til_rediger_slutt_error)) },
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        IconButton(onClick = { showEndHelp = true }) {
                            Icon(Icons.Default.Info, contentDescription = stringResource(R.string.show_help))
                        }
                        DropdownMenu(
                            expanded = showEndHelp,
                            onDismissRequest = { showEndHelp = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.end_help)) },
                                onClick = { showEndHelp = false }
                            )
                        }
                    }
                )
            }

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_loddbok))
            }
        }
    }
}
