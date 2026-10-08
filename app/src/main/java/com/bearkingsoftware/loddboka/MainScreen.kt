package com.bearkingsoftware.loddboka

import android.content.res.Configuration
import android.os.LocaleList
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

/**
 * Ensures Compose [stringResource] resolves against the in-app selected locale.
 *
 * Modern Compose reads strings via [LocalResources] (not [LocalContext]). Wrapping only
 * [LocalLayoutDirection] left [LocalResources] on the Activity configuration, so after
 * picking e.g. German some UI stayed on the previous/system language (often English).
 *
 * We intentionally do **not** replace [LocalContext], so callers can still cast to
 * [android.app.Activity] for [android.app.Activity.recreate].
 */
@Composable
fun MainScreen(locale: Locale) {
    val baseContext = LocalContext.current
    val baseConfiguration = LocalConfiguration.current

    val localizedResources = remember(locale, baseContext) {
        val config = Configuration(baseContext.resources.configuration)
        config.setLocales(LocaleList(locale))
        baseContext.createConfigurationContext(config).resources
    }
    val localizedConfiguration = remember(locale, baseConfiguration) {
        Configuration(baseConfiguration).apply {
            setLocales(LocaleList(locale))
        }
    }
    val layoutDirection = when (TextUtils.getLayoutDirectionFromLocale(locale)) {
        android.util.LayoutDirection.RTL -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfiguration,
        LocalResources provides localizedResources,
        LocalLayoutDirection provides layoutDirection,
    ) {
        AppNavigation()
    }
}
