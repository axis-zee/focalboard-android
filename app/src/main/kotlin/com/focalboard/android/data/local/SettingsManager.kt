package com.focalboard.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.focalboard.android.data.api.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    
    private val dataStore = context.dataStore
    
    companion object {
        val SERVER_URL = stringPreferencesKey("server_url")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_USERNAME = stringPreferencesKey("user_username")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
    }
    
    fun getServerUrl(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[SERVER_URL]
    }
    
    fun getAuthToken(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN]
    }
    
    suspend fun saveServerUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[SERVER_URL] = url
        }
    }
    
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }
    
    fun getUserInfo(): Flow<UserInfo?> = dataStore.data.map { preferences ->
        val id = preferences[USER_ID]
        val email = preferences[USER_EMAIL]
        val username = preferences[USER_USERNAME]
        val displayName = preferences[USER_DISPLAY_NAME]
        
        if (id != null && email != null) {
            UserInfo(id, email, username ?: "", displayName ?: "")
        } else {
            null
        }
    }
    
    suspend fun saveUserInfo(user: UserInfo) {
        dataStore.edit { preferences ->
            preferences[USER_ID] = user.id
            preferences[USER_EMAIL] = user.email
            preferences[USER_USERNAME] = user.username
            user.displayName?.let { name ->
                if (name.isNotEmpty()) {
                    preferences[USER_DISPLAY_NAME] = name
                }
            }
        }
    }
    
    suspend fun clearAuth() {
        dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(USER_EMAIL)
            preferences.remove(USER_USERNAME)
            preferences.remove(USER_DISPLAY_NAME)
        }
    }
}
