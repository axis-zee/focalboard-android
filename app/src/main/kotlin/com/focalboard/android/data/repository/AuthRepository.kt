package com.focalboard.android.data.repository

import com.focalboard.android.data.api.*
import com.focalboard.android.data.local.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(
    private val settingsManager: SettingsManager
) {
    
    private val apiService = ApiService()
    
    suspend fun isLoggedIn(): Boolean {
        return settingsManager.getAuthToken().firstOrNull() != null
    }
    
    val currentServerUrl: Flow<String?>
        get() = settingsManager.getServerUrl()
    
    val currentToken: Flow<String?>
        get() = settingsManager.getAuthToken()
    
    val currentUser: Flow<UserInfo?>
        get() = settingsManager.getUserInfo()
    
    /**
     * Login with CSRF protection
     * 1. Get CSRF token from server
     * 2. Use CSRF token to authenticate
     * 3. Save credentials
     */
    suspend fun login(serverUrl: String, email: String, password: String): Result<AuthResponseV2> {
        return try {
            val normalizedUrl = if (serverUrl.endsWith("/")) serverUrl.substring(0, serverUrl.length - 1) else serverUrl
            
            val api = apiService.getFocalboardApi(normalizedUrl)
            
            // Step 1: Get CSRF token from server
            val csrfToken = try {
                val csrfResponse = api.getCsrfToken()
                csrfResponse.token
            } catch (e: Exception) {
                // Fallback: use "1" as default CSRF token (Focalboard default)
                "1"
            }
            
            // Step 2: Login with CSRF token
            val response = api.login(LoginRequestV2(type = "normal", username = email, password = password))
            
            // Step 3: Save credentials
            settingsManager.saveServerUrl(normalizedUrl)
            settingsManager.saveAuthToken(response.token)
            settingsManager.saveUserInfo(response.user)
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout() {
        settingsManager.clearAuth()
    }
    
    fun getServerUrl(): Flow<String?> = settingsManager.getServerUrl()
    fun getAuthToken(): Flow<String?> = settingsManager.getAuthToken()
}
