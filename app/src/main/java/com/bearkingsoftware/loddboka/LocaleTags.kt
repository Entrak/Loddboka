package com.bearkingsoftware.loddboka

import java.util.Locale

/**
 * Pure JVM helpers for in-app language tags (no Android framework dependency).
 */
object LocaleTags {
    const val NORWEGIAN = "nb"

    fun normalizeLanguageCode(tagOrCode: String): String {
        val language = tagOrCode
            .trim()
            .replace('_', '-')
            .substringBefore('-')
            .lowercase(Locale.ROOT)
        return when (language) {
            "no", "nn" -> NORWEGIAN
            else -> language
        }
    }
}
