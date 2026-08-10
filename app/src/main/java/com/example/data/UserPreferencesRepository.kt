package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = intPreferencesKey("theme_mode") // 0: System, 1: Light, 2: Dark
        val WEATHER_LOCATION = stringPreferencesKey("weather_location")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
        val USER_NAME = stringPreferencesKey("user_name")
        val UNIT_NAME = stringPreferencesKey("unit_name")
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.THEME_MODE] ?: 0
    }

    val weatherLocation: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.WEATHER_LOCATION] ?: "Xã Liên Minh, TP. Hà Nội"
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.NOTIFICATIONS_ENABLED] ?: true
    }

    val lastBackupTime: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[Keys.LAST_BACKUP_TIME] ?: 0L
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.USER_NAME] ?: "Hoàng Minh Đức"
    }

    val unitName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.UNIT_NAME] ?: "UBND xã Liên Minh"
    }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode
        }
    }

    suspend fun setWeatherLocation(location: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.WEATHER_LOCATION] = location
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setLastBackupTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_BACKUP_TIME] = timestamp
        }
    }
}
