package com.bearkingsoftware.loddboka.ui

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bearkingsoftware.loddboka.LocaleManager
import com.bearkingsoftware.loddboka.ui.settings.LanguageSelector
import kotlinx.coroutines.launch

@Composable
fun InitialConfigurationScreen(
    onConfigurationComplete: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val localeManager = remember { LocaleManager(context) }
    val scope = rememberCoroutineScope()

    Scaffold {
        Column(
            modifier = Modifier.fillMaxSize().padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LanguageSelector {
                scope.launch {
                    localeManager.setLocale(it)
                    activity?.recreate()
                    onConfigurationComplete()
                }
            }
        }
    }
}
