package com.attendancehr.app.data.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "attendance_hr_session")

data class SessionState(
    val hasSeenOnboarding: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUserId: String? = null,
)

class SessionStore(private val appContext: Context) {
    private object Keys {
        val HasSeenOnboarding = booleanPreferencesKey("has_seen_onboarding")
        val IsLoggedIn = booleanPreferencesKey("is_logged_in")
        val CurrentUserId = stringPreferencesKey("current_user_id")
    }

    val session: Flow<SessionState> = appContext.dataStore.data.map { prefs ->
        prefs.toSessionState()
    }

    suspend fun markOnboardingSeen() {
        appContext.dataStore.edit { it[Keys.HasSeenOnboarding] = true }
    }

    suspend fun setLoggedIn(userId: String) {
        appContext.dataStore.edit {
            it[Keys.IsLoggedIn] = true
            it[Keys.CurrentUserId] = userId
        }
    }

    suspend fun logout() {
        appContext.dataStore.edit {
            it[Keys.IsLoggedIn] = false
            it.remove(Keys.CurrentUserId)
        }
    }

    private fun Preferences.toSessionState(): SessionState {
        return SessionState(
            hasSeenOnboarding = this[Keys.HasSeenOnboarding] ?: false,
            isLoggedIn = this[Keys.IsLoggedIn] ?: false,
            currentUserId = this[Keys.CurrentUserId],
        )
    }
}

