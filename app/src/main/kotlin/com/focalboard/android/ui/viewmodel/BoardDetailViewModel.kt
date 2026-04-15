package com.focalboard.android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focalboard.android.data.api.*
import com.focalboard.android.data.local.SettingsManager
import com.focalboard.android.data.repository.BoardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class BoardDetailUiState {
    data object Loading : BoardDetailUiState()
    data class Success(
        val board: Board,
        val views: List<BoardView>,
        val rows: List<BoardRow>,
        val isLoading: Boolean = false,
        val selectedViewId: String? = null
    ) : BoardDetailUiState()
    data class Error(val message: String) : BoardDetailUiState()
    data object NotAuthenticated : BoardDetailUiState()
}

class BoardDetailViewModel(
    application: Application,
    private val boardId: String
) : AndroidViewModel(application) {
    
    private val settingsManager = SettingsManager(application)
    private val apiService = ApiService()
    
    private val _uiState = MutableStateFlow<BoardDetailUiState>(BoardDetailUiState.Loading)
    val uiState: StateFlow<BoardDetailUiState> = _uiState.asStateFlow()
    
    private var currentBoard: Board? = null
    private var currentViews: List<BoardView> = emptyList()
    private var currentRows: List<BoardRow> = emptyList()
    private var currentViewId: String? = null
    
    init {
        loadBoardDetail()
    }
    
    private fun loadBoardDetail() {
        viewModelScope.launch {
            val token = settingsManager.getAuthToken().first()
            val serverUrl = settingsManager.getServerUrl().first()
            
            if (token == null || serverUrl == null) {
                _uiState.value = BoardDetailUiState.NotAuthenticated
                return@launch
            }
            
            _uiState.value = BoardDetailUiState.Loading
            
            try {
                val api = apiService.getFocalboardApi(serverUrl)
                val csrfToken = "1" // CSRF token should be retrieved from cookie jar
                
                // Load board details
                val board = api.getBoard(csrfToken, token, boardId)
                currentBoard = board
                
                // Load board views
                val views = api.getBoardViews(csrfToken, token, boardId)
                currentViews = views
                
                // Select the first view or a Kanban view if available
                val selectedView = views.find { it.type == "kanban" } ?: views.firstOrNull()
                currentViewId = selectedView?.id
                
                // Load rows for the selected view
                if (selectedView != null) {
                    val rows = api.getBoardRows(csrfToken, token, boardId, selectedView.id)
                    currentRows = rows
                }
                
                _uiState.value = BoardDetailUiState.Success(
                    board = board,
                    views = views,
                    rows = currentRows,
                    selectedViewId = currentViewId
                )
            } catch (e: Exception) {
                _uiState.value = BoardDetailUiState.Error(e.message ?: "Failed to load board")
            }
        }
    }
    
    fun changeView(viewId: String) {
        viewModelScope.launch {
            val token = settingsManager.getAuthToken().first()
            val serverUrl = settingsManager.getServerUrl().first()
            
            if (token == null || serverUrl == null || currentBoard == null) return@launch
            
            try {
                _uiState.value = when (val currentState = _uiState.value) {
                    is BoardDetailUiState.Success -> currentState.copy(isLoading = true)
                    else -> BoardDetailUiState.Loading
                }
                
                val api = apiService.getFocalboardApi(serverUrl)
                val csrfToken = "1"
                
                val rows = api.getBoardRows(csrfToken, token, currentBoard!!.id, viewId)
                currentRows = rows
                currentViewId = viewId
                
                _uiState.value = BoardDetailUiState.Success(
                    board = currentBoard!!,
                    views = currentViews,
                    rows = rows,
                    selectedViewId = viewId
                )
            } catch (e: Exception) {
                _uiState.value = BoardDetailUiState.Error(e.message ?: "Failed to load view")
            }
        }
    }
    
    fun createCard(name: String, statusColumnId: String? = null, statusValue: String? = null) {
        viewModelScope.launch {
            val token = settingsManager.getAuthToken().first()
            val serverUrl = settingsManager.getServerUrl().first()
            
            if (token == null || serverUrl == null || currentBoard == null || currentViewId == null) return@launch
            
            try {
                val api = apiService.getFocalboardApi(serverUrl)
                val csrfToken = "1"
                
                val cells = mutableMapOf<String, CellValue>()
                
                // Add title cell (usually column ID is "title")
                cells["title"] = CellValue(type = "text", text = name)
                
                // Add status cell if provided
                if (statusColumnId != null && statusValue != null) {
                    cells[statusColumnId] = CellValue(type = "select", selectOptionId = statusValue)
                }
                
                val newRow = api.createRow(csrfToken, token, currentBoard!!.id, currentViewId!!, 
                    BoardRowCreateRequest(cells = cells))
                
                // Update local state
                currentRows = currentRows + newRow
                
                _uiState.value = when (val currentState = _uiState.value) {
                    is BoardDetailUiState.Success -> currentState.copy(rows = currentRows)
                    else -> BoardDetailUiState.Success(
                        board = currentBoard ?: Board("", "", "", "", "", ""),
                        views = currentViews,
                        rows = currentRows,
                        selectedViewId = currentViewId
                    )
                }
            } catch (e: Exception) {
                _uiState.value = BoardDetailUiState.Error(e.message ?: "Failed to create card")
            }
        }
    }
    
    fun updateCard(rowId: String, updates: Map<String, CellValue>) {
        viewModelScope.launch {
            val token = settingsManager.getAuthToken().first()
            val serverUrl = settingsManager.getServerUrl().first()
            
            if (token == null || serverUrl == null || currentBoard == null) return@launch
            
            try {
                val api = apiService.getFocalboardApi(serverUrl)
                val csrfToken = "1"
                
                // Get existing row and merge updates
                val existingRow = currentRows.find { it.id == rowId }
                val mergedCells = (existingRow?.cells ?: emptyMap()) + updates
                
                val updatedRow = api.updateRow(csrfToken, token, currentBoard!!.id, rowId,
                    BoardRowUpdateRequest(cells = mergedCells))
                
                // Update local state
                currentRows = currentRows.map { if (it.id == rowId) updatedRow else it }
                
                _uiState.value = when (val currentState = _uiState.value) {
                    is BoardDetailUiState.Success -> currentState.copy(rows = currentRows)
                    else -> BoardDetailUiState.Success(
                        board = currentBoard ?: Board("", "", "", "", "", ""),
                        views = currentViews,
                        rows = currentRows,
                        selectedViewId = currentViewId
                    )
                }
            } catch (e: Exception) {
                _uiState.value = BoardDetailUiState.Error(e.message ?: "Failed to update card")
            }
        }
    }
    
    fun deleteCard(rowId: String) {
        viewModelScope.launch {
            val token = settingsManager.getAuthToken().first()
            val serverUrl = settingsManager.getServerUrl().first()
            
            if (token == null || serverUrl == null || currentBoard == null) return@launch
            
            try {
                val api = apiService.getFocalboardApi(serverUrl)
                val csrfToken = "1"
                
                api.deleteRow(csrfToken, token, currentBoard!!.id, rowId)
                
                // Update local state
                currentRows = currentRows.filter { it.id != rowId }
                
                _uiState.value = when (val currentState = _uiState.value) {
                    is BoardDetailUiState.Success -> currentState.copy(rows = currentRows)
                    else -> BoardDetailUiState.Success(
                        board = currentBoard ?: Board("", "", "", "", "", ""),
                        views = currentViews,
                        rows = currentRows,
                        selectedViewId = currentViewId
                    )
                }
            } catch (e: Exception) {
                _uiState.value = BoardDetailUiState.Error(e.message ?: "Failed to delete card")
            }
        }
    }
    
    fun refresh() {
        loadBoardDetail()
    }
    
    /**
     * Get status column ID from view options (groupByColumnId for Kanban)
     */
    fun getStatusColumnId(): String? {
        val view = currentViews.find { it.id == currentViewId }
        return view?.options?.groupByColumnId
    }
    
    /**
     * Group rows by status column for Kanban view
     */
    fun getGroupedRows(): Map<String, List<BoardRow>> {
        val statusColumnId = getStatusColumnId()
        
        if (statusColumnId == null) {
            // No status column, return all rows in a single group
            return mapOf("All" to currentRows)
        }
        
        return currentRows.groupBy { row ->
            val statusValue = row.cells[statusColumnId]?.selectOptionId ?: "Unassigned"
            statusValue
        }
    }
}
