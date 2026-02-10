package com.bearkingsoftware.loddboka

import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bearkingsoftware.loddboka.ui.settings.Language
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class LocaleManager(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language_key")
        val IS_FIRST_RUN_KEY = booleanPreferencesKey("is_first_run")
    }

    private val supportedLanguages = Language.entries.map { it.code }

    val localeFlow = context.dataStore.data.map { preferences ->
        val language = preferences[LANGUAGE_KEY]
        if (language != null) {
            Locale.forLanguageTag(language)
        } else {
            getSystemLocale() ?: Locale.forLanguageTag(Language.ENGLISH.code)
        }
    }

    private fun getSystemLocale(): Locale? {
        val systemLanguage = Resources.getSystem().configuration.locales[0].language
        val supportedLanguage = supportedLanguages.find { it == systemLanguage }
        return if (supportedLanguage != null) {
            Locale.forLanguageTag(supportedLanguage)
        } else {
            null
        }
    }

    suspend fun isFirstRun(): Boolean {
        return context.dataStore.data.map { it[IS_FIRST_RUN_KEY] ?: true }.first()
    }

    suspend fun setLocale(locale: Locale) {
        context.dataStore.edit { settings ->
            settings[LANGUAGE_KEY] = locale.toLanguageTag()
            settings[IS_FIRST_RUN_KEY] = false
        }
    }
}
