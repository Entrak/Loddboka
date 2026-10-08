package com.bearkingsoftware.loddboka

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * JVM-only checks for language-tag normalization used by in-app locale selection.
 * Does not require a device/emulator or Android framework.
 */
class LocaleNormalizeTest {

    @Test
    fun norwegianDeviceTagsMapToNb() {
        assertEquals("nb", LocaleTags.normalizeLanguageCode("no"))
        assertEquals("nb", LocaleTags.normalizeLanguageCode("nn"))
        assertEquals("nb", LocaleTags.normalizeLanguageCode("nb"))
        assertEquals("nb", LocaleTags.normalizeLanguageCode("nb-NO"))
        assertEquals("nb", LocaleTags.normalizeLanguageCode("no_NO"))
    }

    @Test
    fun otherSupportedCodesPassThrough() {
        assertEquals("en", LocaleTags.normalizeLanguageCode("en"))
        assertEquals("sv", LocaleTags.normalizeLanguageCode("sv-SE"))
        assertEquals("da", LocaleTags.normalizeLanguageCode("da"))
        assertEquals("fi", LocaleTags.normalizeLanguageCode("fi"))
        assertEquals("se", LocaleTags.normalizeLanguageCode("se"))
        assertEquals("fr", LocaleTags.normalizeLanguageCode("fr-FR"))
        assertEquals("de", LocaleTags.normalizeLanguageCode("de"))
    }

    @Test
    fun trimsAndLowercases() {
        assertEquals("sv", LocaleTags.normalizeLanguageCode(" SV "))
        assertEquals("en", LocaleTags.normalizeLanguageCode("EN-us"))
    }
}
