package com.bearkingsoftware.loddboka.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bearkingsoftware.loddboka.R
import com.bearkingsoftware.loddboka.data.Farge

@Composable
fun Farge.toStringResource(): String {
    return when (this) {
        Farge.RØD -> stringResource(R.string.color_red)
        Farge.BLÅ -> stringResource(R.string.color_blue)
        Farge.GRØNN -> stringResource(R.string.color_green)
        Farge.GUL -> stringResource(R.string.color_yellow)
        Farge.ORANSJE -> stringResource(R.string.color_orange)
        Farge.ROSA -> stringResource(R.string.color_pink)
        Farge.LILLA -> stringResource(R.string.color_purple)
        Farge.HVIT -> stringResource(R.string.color_white)
    }
}