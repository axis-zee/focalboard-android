package com.focalboard.android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focalboard.android.data.api.ApiService
import com.focalboard.android.data.local.FocalboardDatabase
import com.focalboard.android.data.local.SettingsManager
import com.focalboard.android.data.repository.BoardRepository
import com.focalboard.android.data.repository.AuthRepository
import com.focalboard.android.data.api.Board
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class MainUiState {
    data object Loading : MainUiState()
    data class Success(val boards: List<Board>, val isLoading: Boolean = false) : MainUiState()
    data class Error(val message: String, val boards: List<Board> = emptyList()) : MainUiState()
    data object NotAuthenticated : MainUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsManager = SettingsManager(application)
    private val database = FocalboardDatabase.getDatabase(application)
    private val boardDao = database.boardDao()
    private val apiService = ApiService()
    
    private val boardRepository = BoardRepository(boardDao, settingsManager, apiService)
    private val authRepository = AuthRepository(settingsManager)
    
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _refreshTrigger = MutableStateFlow(0L)
    val refreshTrigger: StateFlow<Long> = _refreshTrigger.asStateFlow()
    
    init {
        observeAuthAndBoards()
    }
    
    private fun observeAuthAndBoards() {
        viewModelScope.launch {
            val authFlow = settingsManager.getAuthToken().map { it != null }
            val serverFlow = settingsManager.getServerUrl()
            
            combine(authFlow, serverFlow) { isAuthenticated, serverUrl ->
                isAuthenticated && serverUrl != null
            }.collect { hasAuth ->
                if (hasAuth) {
                    _uiState.value = MainUiState.Loading
                    observeBoards()
                } else {
                    _uiState.value = MainUiState.NotAuthenticated
                }
            }
        }
    }
    
    private fun observeBoards() {
        viewModelScope.launch {
            combine(boardRepository.getBoards(), _refreshTrigger) { boards, _ ->
                boards
            }.collect { boards ->
                when (val currentState = _uiState.value) {
                    is MainUiState.Loading -> {
                        _uiState.value = MainUiState.Success(boards, isLoading = false)
                    }
                    is MainUiState.Success -> {
                        _uiState.value = MainUiState.Success(boards, isLoading = false)
                    }
                    is MainUiState.Error -> {
                        _uiState.value = MainUiState.Success(boards, isLoading = false)
                    }
                    else -> {}
                }
            }
        }
    }
    
    fun refreshBoards() {
        viewModelScope.launch {
            val token = settingsManager.getAuthToken().first()
            val serverUrl = settingsManager.getServerUrl().first()
            
            if (token != null && serverUrl != null) {
                _uiState.value = MainUiState.Success(
                    when (_uiState.value) {
                        is MainUiState.Success -> (_uiState.value as MainUiState.Success).boards
                        is MainUiState.Error -> (_uiState.value as MainUiState.Error).boards
                        else -> emptyList()
                    },
                    isLoading = true
                )
                
                try {
                    val result = boardRepository.refreshBoards(serverUrl, token, "1")
                    result.fold(
                        onSuccess = { boards ->
                            _uiState.value = MainUiState.Success(boards, isLoading = false)
                        },
                        onFailure = { error ->
                            val currentBoards = when (_uiState.value) {
                                is MainUiState.Success -> (_uiState.value as MainUiState.Success).boards
                                is MainUiState.Error -> (_uiState.value as MainUiState.Error).boards
                                else -> emptyList()
                            }
                            _uiState.value = MainUiState.Error(
                                message = error.message ?: "Failed to refresh boards",
                                boards = currentBoards
                            )
                        }
                    )
                } catch (e: Exception) {
                    val currentBoards = when (_uiState.value) {
                        is MainUiState.Success -> (_uiState.value as MainUiState.Success).boards
                        is MainUiState.Error -> (_uiState.value as MainUiState.Error).boards
                        else -> emptyList()
                    }
                    _uiState.value = MainUiState.Error(
                        message = e.message ?: "Failed to refresh boards",
                        boards = currentBoards
                    )
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = MainUiState.NotAuthenticated
        }
    }
    
    suspend fun getServerUrl(): String? {
        return settingsManager.getServerUrl().firstOrNull()
    }
}
