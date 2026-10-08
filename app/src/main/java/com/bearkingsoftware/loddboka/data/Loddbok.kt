package com.bearkingsoftware.loddboka.data

/**
 * Domain color id. UI maps [argb] to Compose Color — keep this layer free of Compose.
 */
enum class Farge(val argb: Long) {
    RØD(0xFFFF0000),
    BLÅ(0xFF0000FF),
    GRØNN(0xFF008000),
    GUL(0xFFFFFF00),
    ORANSJE(0xFFFFA500),
    ROSA(0xFFFFC0CB),
    LILLA(0xFF800080),
    HVIT(0xFFFFFFFF),
}

data class Loddbok(
    val id: Long = 0L,
    val farge: Farge,
    val bokstav: Char,
    val start: Int,
    val end: Int
)
