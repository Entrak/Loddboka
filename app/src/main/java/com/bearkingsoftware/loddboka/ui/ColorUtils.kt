package com.bearkingsoftware.loddboka.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun getContrastingTextColor(backgroundColor: Color): Color {
    val contrastWithWhite = ColorUtils.calculateContrast(Color.White.toArgb(), backgroundColor.toArgb())
    val contrastWithBlack = ColorUtils.calculateContrast(Color.Black.toArgb(), backgroundColor.toArgb())
    return if (contrastWithWhite > contrastWithBlack) Color.White else Color.Black
}
