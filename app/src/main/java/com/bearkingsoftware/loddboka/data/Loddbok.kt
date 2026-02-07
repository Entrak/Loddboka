package com.bearkingsoftware.loddboka.data

import androidx.compose.ui.graphics.Color

data class Loddbok(
    val id: Long = System.currentTimeMillis(),
    val farge: Farge,
    val bokstav: Char,
    val start: Int,
    val end: Int
)

enum class Farge(val color: Color) {
    RØD(Color(0xFFFF0000)),
    BLÅ(Color(0xFF0000FF)),
    GRØNN(Color(0xFF008000)),
    GUL(Color(0xFFFFFF00)),
    ORANSJE(Color(0xFFFFA500)),
    ROSA(Color(0xFFFFC0CB)),
    LILLA(Color(0xFF800080)),
    HVIT(Color(0xFFFFFFFF)),
}