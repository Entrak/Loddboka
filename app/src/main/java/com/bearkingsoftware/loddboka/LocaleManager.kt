package com.bearkingsoftware.loddboka

import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.os.LocaleListCompat
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

val LocalLocaleManager = staticCompositionLocalOf<LocaleManager> {
    error("LocalLocaleManager not provided")
}

class LocaleManager(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language_key")
        val IS_FIRST_RUN_KEY = booleanPreferencesKey("is_first_run")

        /** Supported BCP-47 language codes from [Language]. */
        val supportedLanguageCodes: Set<String> =
            Language.entries.map { it.code }.toSet()

        /** @see LocaleTags.normalizeLanguageCode */
        fun normalizeLanguageCode(tagOrCode: String): String =
            LocaleTags.normalizeLanguageCode(tagOrCode)

        fun localeForCode(code: String): Locale =
            Locale.forLanguageTag(normalizeLanguageCode(code))

        fun applyAppLocales(locale: Locale) {
            val tag = normalizeLanguageCode(locale.toLanguageTag())
            val desired = LocaleListCompat.forLanguageTags(tag)
            if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != desired.toLanguageTags()) {
                AppCompatDelegate.setApplicationLocales(desired)
            }
        }
    }

    val localeFlow = context.dataStore.data.map { preferences ->
        val stored = preferences[LANGUAGE_KEY]
        if (stored != null) {
            localeForCode(stored)
        } else {
            getSystemLocale() ?: localeForCode(Language.ENGLISH.code)
        }
    }

    private fun getSystemLocale(): Locale? {
        val locales = Resources.getSystem().configuration.locales
        if (locales.isEmpty) return null
        val systemLanguage = locales[0].language
        val normalized = normalizeLanguageCode(systemLanguage)
        return if (normalized in supportedLanguageCodes) {
            Locale.forLanguageTag(normalized)
        } else {
            null
        }
    }

    suspend fun isFirstRun(): Boolean {
        return context.dataStore.data.map { it[IS_FIRST_RUN_KEY] ?: true }.first()
    }

    suspend fun currentLocale(): Locale = localeFlow.first()

    suspend fun setLocale(locale: Locale) {
        val code = normalizeLanguageCode(locale.toLanguageTag())
        context.dataStore.edit { settings ->
            settings[LANGUAGE_KEY] = code
            settings[IS_FIRST_RUN_KEY] = false
        }
        applyAppLocales(Locale.forLanguageTag(code))
    }
}
