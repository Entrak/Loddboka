package com.bearkingsoftware.loddboka.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun getContrastingTextColor(backgroundColor: Color): Color {
    val opaqueBg = backgroundColor.copy(alpha = 1f)
    val contrastWithWhite = ColorUtils.calculateContrast(Color.White.toArgb(), opaqueBg.toArgb())
    val contrastWithBlack = ColorUtils.calculateContrast(Color.Black.toArgb(), opaqueBg.toArgb())
    return if (contrastWithWhite > contrastWithBlack) Color.White else Color.Black
}
