package com.focalboard.android.data.repository

import com.focalboard.android.data.api.*
import com.focalboard.android.data.local.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val settingsManager: SettingsManager
) {
    
    private val apiService = ApiService()
    
    val isLoggedIn: Boolean
        get() = settingsManager.getAuthToken() != null
    
    val currentServerUrl: String?
        get() = settingsManager.getServerUrl()
    
    val currentToken: String?
        get() = settingsManager.getAuthToken()
    
    val currentUser: UserInfo?
        get() = settingsManager.getUserInfo()
    
    suspend fun login(serverUrl: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val normalizedUrl = if (serverUrl.endsWith("/")) serverUrl.substring(0, serverUrl.length - 1) else serverUrl
            
            val api = apiService.getFocalboardApi(normalizedUrl)
            val response = api.login(LoginRequest(email, password))
            
            // Save credentials
            settingsManager.saveServerUrl(normalizedUrl)
            settingsManager.saveAuthToken(response.token)
            settingsManager.saveUserInfo(response.user)
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        settingsManager.clearAuth()
    }
    
    fun getServerUrl(): String? = settingsManager.getServerUrl()
    fun getAuthToken(): String? = settingsManager.getAuthToken()
}
