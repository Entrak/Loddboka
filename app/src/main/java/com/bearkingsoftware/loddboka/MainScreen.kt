package com.bearkingsoftware.loddboka

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.text.layoutDirection
import java.util.Locale

@Composable
fun MainScreen(
    locale: Locale
) {
    val layoutDirection = when (locale.layoutDirection) {
        android.util.LayoutDirection.RTL -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }
    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        AppNavigation()
    }
}