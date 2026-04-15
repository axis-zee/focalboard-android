package com.focalboard.android.data.repository

import com.focalboard.android.data.api.ApiService
import com.focalboard.android.data.api.Board
import com.focalboard.android.data.local.BoardDao
import com.focalboard.android.data.local.BoardEntity
import com.focalboard.android.data.local.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class BoardRepository(
    private val boardDao: BoardDao,
    private val settingsManager: SettingsManager,
    private val apiService: ApiService
) {
    
    /**
     * Get boards from local database, filtered by current server URL
     */
    fun getBoards(): Flow<List<Board>> {
        return settingsManager.getServerUrl().map { serverUrl ->
            serverUrl ?: return@map emptyList()
        }.combine(settingsManager.getAuthToken()) { serverUrl, token ->
            if (serverUrl != null && token != null) {
                // Boards will be loaded from local DB
                Pair(serverUrl, token)
            } else {
                null
            }
        }.flatMapLatest { pair ->
            if (pair != null) {
                boardDao.getBoardsByServer(pair.first).map { entities ->
                    entities.map { it.toBoard() }
                }
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
    }
    
    /**
     * Fetch boards from server and cache them locally
     */
    suspend fun refreshBoards(serverUrl: String, token: String, csrfToken: String): Result<List<Board>> {
        return try {
            val api = apiService.getFocalboardApi(serverUrl)
            val boards = api.getBoards(csrfToken, token)
            
            // Cache boards locally
            val boardEntities = boards.map { board ->
                BoardEntity(
                    id = board.id,
                    name = board.name,
                    description = board.description,
                    workspaceId = board.workspaceId,
                    serverUrl = serverUrl,
                    createdAt = board.createdAt,
                    updatedAt = board.updatedAt
                )
            }
            
            boardDao.insertBoards(boardEntities)
            Result.success(boards)
        } catch (e: Exception) {
            // Return cached boards on network error
            try {
                val cachedBoards = boardDao.getBoardsByServerSync(serverUrl)
                Result.success(cachedBoards.map { it.toBoard() })
            } catch (e2: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get a single board by ID
     */
    suspend fun getBoardById(boardId: String): Result<Board> {
        return try {
            val entity = boardDao.getBoardById(boardId)
            if (entity != null) {
                Result.success(entity.toBoard())
            } else {
                Result.failure(Exception("Board not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear cached boards for a server
     */
    suspend fun clearCachedBoards(serverUrl: String) {
        boardDao.deleteBoardsByServer(serverUrl)
    }
}

// Extension function to convert BoardEntity to Board
private fun BoardEntity.toBoard(): Board {
    return Board(
        id = this.id,
        name = this.name,
        description = this.description,
        workspaceId = this.workspaceId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
