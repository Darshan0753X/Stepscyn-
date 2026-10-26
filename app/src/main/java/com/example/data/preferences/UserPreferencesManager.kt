package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

/**
 * Manages user preferences stored securely via Android Jetpack DataStore Preferences.
 */
class UserPreferencesManager(private val context: Context) {

    companion object {
        val KEY_DAILY_STEP_GOAL = longPreferencesKey("target_daily_step_goal")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_STEP_STRIDE_LENGTH = intPreferencesKey("step_stride_length_cm")
        val KEY_LAST_SYNCED_TIMESTAMP = longPreferencesKey("last_health_connect_sync")
        val KEY_DEFAULT_VIEW_MODE = stringPreferencesKey("default_view_mode")

        const val DEFAULT_DAILY_GOAL = 10000L
        const val MIN_DAILY_GOAL = 1000L
        const val MAX_DAILY_GOAL = 50000L
    }

    /**
     * Flow of the user's persisted daily step goal, defaulting to 10,000 steps.
     */
    val dailyStepGoalFlow: Flow<Long> = context.userDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_DAILY_STEP_GOAL] ?: DEFAULT_DAILY_GOAL
        }

    /**
     * Updates and persists the user's daily step goal into DataStore.
     */
    suspend fun setDailyStepGoal(goal: Long): Boolean {
        val clampedGoal = goal.coerceIn(MIN_DAILY_GOAL, MAX_DAILY_GOAL)
        context.userDataStore.edit { preferences ->
            preferences[KEY_DAILY_STEP_GOAL] = clampedGoal
        }
        return true
    }

    /**
     * Synchronously fetches current step goal value from DataStore.
     */
    suspend fun getDailyStepGoal(): Long {
        return try {
            dailyStepGoalFlow.first()
        } catch (_: Exception) {
            DEFAULT_DAILY_GOAL
        }
    }

    /**
     * Flow of whether daily goal reminder notifications are enabled.
     */
    val notificationsEnabledFlow: Flow<Boolean> = context.userDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.userDataStore.edit { preferences ->
            preferences[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Resets preferences to factory defaults.
     */
    suspend fun resetToDefaults() {
        context.userDataStore.edit { preferences ->
            preferences[KEY_DAILY_STEP_GOAL] = DEFAULT_DAILY_GOAL
            preferences[KEY_NOTIFICATIONS_ENABLED] = true
        }
    }
}
