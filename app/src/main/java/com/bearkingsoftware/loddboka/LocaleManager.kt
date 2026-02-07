package com.bearkingsoftware.loddboka

import android.app.LocaleManager as AndroidLocaleManager
import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LocaleManager(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language_key")
        private val USE_SYSTEM_LANGUAGE_KEY = booleanPreferencesKey("use_system_language_key")
    }

    private val supportedLanguages = listOf("en", "nb", "sv", "da", "fi", "se", "fr", "de")

    val localeFlow = context.dataStore.data.map { preferences ->
        val useSystemLanguage = preferences[USE_SYSTEM_LANGUAGE_KEY] ?: true
        if (useSystemLanguage) {
            val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = context.getSystemService(Context.LOCALE_SERVICE) as AndroidLocaleManager
                val systemLocales = localeManager.systemLocales
                (0 until systemLocales.size()).map { systemLocales[it] }.firstOrNull { supportedLanguages.contains(it.language) }
            } else {
                @Suppress("DEPRECATION")
                val systemLocale = Resources.getSystem().configuration.locale
                if (supportedLanguages.contains(systemLocale.language)) systemLocale else null
            }
            systemLocale ?: Locale.forLanguageTag("en")
        } else {
            val language = preferences[LANGUAGE_KEY] ?: "en"
            Locale.forLanguageTag(language)
        }
    }

    val useSystemLanguageFlow = context.dataStore.data.map { preferences ->
        preferences[USE_SYSTEM_LANGUAGE_KEY] ?: true
    }

    suspend fun setLocale(locale: Locale) {
        context.dataStore.edit { settings ->
            settings[LANGUAGE_KEY] = locale.toLanguageTag()
            settings[USE_SYSTEM_LANGUAGE_KEY] = false
        }
    }

    suspend fun setUseSystemLanguage(useSystemLanguage: Boolean) {
        context.dataStore.edit { settings ->
            settings[USE_SYSTEM_LANGUAGE_KEY] = useSystemLanguage
        }
    }
}