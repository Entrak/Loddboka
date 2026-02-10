package com.bearkingsoftware.loddboka.ui.settings

import androidx.annotation.DrawableRes
import com.bearkingsoftware.loddboka.R

enum class Language(
    val code: String,
    val text: String,
    val isSami: Boolean = false,
    @DrawableRes val flag: Int? = null
) {
    ENGLISH("en", "English", flag = R.drawable.gb),
    NORWEGIAN("nb", "Norsk", flag = R.drawable.no),
    SWEDISH("sv", "Svenska", flag = R.drawable.se),
    DANISH("da", "Dansk", flag = R.drawable.dk),
    FINNISH("fi", "Suomi", flag = R.drawable.fi),
    SAMI("se", "Sámegiella", isSami = true, flag = R.drawable.sami_flag),
    FRENCH("fr", "Français", flag = R.drawable.fr),
    GERMAN("de", "Deutsch", flag = R.drawable.de)
}

val supportedLanguages = listOf(
    Language.ENGLISH,
    Language.NORWEGIAN,
    Language.SWEDISH,
    Language.DANISH,
    Language.FINNISH,
    Language.SAMI,
    Language.FRENCH,
    Language.GERMAN
)
