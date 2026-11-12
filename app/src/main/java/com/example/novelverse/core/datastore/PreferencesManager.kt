package com.example.novelverse.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "novelverse_preferences")

data class UserSettings(
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val accentColor: String = "INDIGO", // INDIGO, AMBER, EMERALD, CRIMSON, ROSE
    val notificationsEnabled: Boolean = true,
    val scraperBaseUrl: String = "https://novelfull.com",
    val scraperTitleSelector: String = "h3.title",
    val scraperContentSelector: String = "div#chapter-content",
    val scraperChapterListSelector: String = "ul.list-chapter li a"
)

data class ReaderPreferences(
    val fontSizeSp: Int = 18,
    val fontFamily: String = "Serif", // Sans-Serif, Serif, Monospace, Cursive
    val lineSpacingMultiplier: Float = 1.6f,
    val paragraphSpacingDp: Int = 16,
    val readerTheme: String = "Sepia", // White, Sepia, Cream, Night, Amoled, Forest
    val isPagedMode: Boolean = false,
    val autoScrollSpeed: Int = 0, // 0 = off, 1-5 speed
    val brightnessPercent: Int = 100 // 10-100%
)

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        
        // Reader
        val FONT_SIZE_SP = intPreferencesKey("font_size_sp")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val PARAGRAPH_SPACING = intPreferencesKey("paragraph_spacing")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val IS_PAGED_MODE = booleanPreferencesKey("is_paged_mode")
        val BRIGHTNESS = intPreferencesKey("brightness_percent")
        
        // Scraper config
        val SCRAPER_BASE_URL = stringPreferencesKey("scraper_base_url")
        val SCRAPER_TITLE_SELECTOR = stringPreferencesKey("scraper_title_selector")
        val SCRAPER_CONTENT_SELECTOR = stringPreferencesKey("scraper_content_selector")
        val SCRAPER_CHAPTER_SELECTOR = stringPreferencesKey("scraper_chapter_selector")
    }

    val accessTokenFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.ACCESS_TOKEN]
        }

    val refreshTokenFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.REFRESH_TOKEN]
        }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            UserSettings(
                themeMode = prefs[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
                accentColor = prefs[PreferencesKeys.ACCENT_COLOR] ?: "INDIGO",
                notificationsEnabled = prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                scraperBaseUrl = prefs[PreferencesKeys.SCRAPER_BASE_URL] ?: "https://novelfull.com",
                scraperTitleSelector = prefs[PreferencesKeys.SCRAPER_TITLE_SELECTOR] ?: "h3.title",
                scraperContentSelector = prefs[PreferencesKeys.SCRAPER_CONTENT_SELECTOR] ?: "div#chapter-content",
                scraperChapterListSelector = prefs[PreferencesKeys.SCRAPER_CHAPTER_SELECTOR] ?: "ul.list-chapter li a"
            )
        }

    val readerPreferencesFlow: Flow<ReaderPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            ReaderPreferences(
                fontSizeSp = prefs[PreferencesKeys.FONT_SIZE_SP] ?: 18,
                fontFamily = prefs[PreferencesKeys.FONT_FAMILY] ?: "Serif",
                lineSpacingMultiplier = prefs[PreferencesKeys.LINE_SPACING] ?: 1.6f,
                paragraphSpacingDp = prefs[PreferencesKeys.PARAGRAPH_SPACING] ?: 16,
                readerTheme = prefs[PreferencesKeys.READER_THEME] ?: "Sepia",
                isPagedMode = prefs[PreferencesKeys.IS_PAGED_MODE] ?: false,
                brightnessPercent = prefs[PreferencesKeys.BRIGHTNESS] ?: 100
            )
        }

    suspend fun saveAuthTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ACCESS_TOKEN] = accessToken
            prefs[PreferencesKeys.REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearAuthTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.ACCESS_TOKEN)
            prefs.remove(PreferencesKeys.REFRESH_TOKEN)
        }
    }

    suspend fun updateThemeMode(themeMode: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = themeMode
        }
    }

    suspend fun updateAccentColor(accentColor: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ACCENT_COLOR] = accentColor
        }
    }

    suspend fun updateNotifications(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateReaderPreferences(readerPreferences: ReaderPreferences) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.FONT_SIZE_SP] = readerPreferences.fontSizeSp
            prefs[PreferencesKeys.FONT_FAMILY] = readerPreferences.fontFamily
            prefs[PreferencesKeys.LINE_SPACING] = readerPreferences.lineSpacingMultiplier
            prefs[PreferencesKeys.PARAGRAPH_SPACING] = readerPreferences.paragraphSpacingDp
            prefs[PreferencesKeys.READER_THEME] = readerPreferences.readerTheme
            prefs[PreferencesKeys.IS_PAGED_MODE] = readerPreferences.isPagedMode
            prefs[PreferencesKeys.BRIGHTNESS] = readerPreferences.brightnessPercent
        }
    }

    suspend fun updateScraperConfig(baseUrl: String, titleSelector: String, contentSelector: String, chapterSelector: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SCRAPER_BASE_URL] = baseUrl
            prefs[PreferencesKeys.SCRAPER_TITLE_SELECTOR] = titleSelector
            prefs[PreferencesKeys.SCRAPER_CONTENT_SELECTOR] = contentSelector
            prefs[PreferencesKeys.SCRAPER_CHAPTER_SELECTOR] = chapterSelector
        }
    }
}
