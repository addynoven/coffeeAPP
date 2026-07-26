package com.example.testing1.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.testing1.data.settings.ThemeConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val THEME_CONFIG = stringPreferencesKey("theme_config")
        val LAST_COFFEE_SYNC = stringPreferencesKey("last_coffee_sync")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    fun getThemeConfig(): Flow<ThemeConfig> = dataStore.data.map { preferences ->
        val themeName = preferences[PreferencesKeys.THEME_CONFIG] ?: ThemeConfig.FOLLOW_SYSTEM.name
        try {
            ThemeConfig.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            ThemeConfig.FOLLOW_SYSTEM
        }
    }

    suspend fun setThemeConfig(config: ThemeConfig) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_CONFIG] = config.name
        }
    }

    fun getLastCoffeeSync(): Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_COFFEE_SYNC] ?: "1970-01-01T00:00:00Z"
    }

    suspend fun setLastCoffeeSync(timestamp: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_COFFEE_SYNC] = timestamp
        }
    }

    fun getSelectedLanguage(): Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: ""
    }

    suspend fun setSelectedLanguage(languageCode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = languageCode
        }
    }
}
