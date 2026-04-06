package com.focalboard.android.data.api

import retrofit2.http.*

/**
 * Focalboard API client interface.
 * 
 * Based on Focalboard's REST API endpoints.
 * Note: This is a simplified version - actual API may vary.
 */
interface FocalboardApi {
    
    /**
     * Authenticate with the server
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body credentials: LoginRequest): AuthResponse
    
    /**
     * Get current user info
     */
    @GET("api/v1/users/me")
    suspend fun getCurrentUser(@Header("Authorization") token: String): UserInfo
    
    /**
     * Get all boards for the current user
     */
    @GET("api/v1/workspace/boards")
    suspend fun getBoards(@Header("Authorization") token: String): List<Board>
    
    /**
     * Get a specific board by ID
     */
    @GET("api/v1/workspace/boards/{boardId}")
    suspend fun getBoard(
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String
    ): Board
    
    /**
     * Create a new board
     */
    @POST("api/v1/workspace/boards")
    suspend fun createBoard(
        @Header("Authorization") token: String,
        @Body board: BoardCreateRequest
    ): Board
    
    /**
     * Get all views (layouts) for a board
     */
    @GET("api/v1/workspace/boards/{boardId}/views")
    suspend fun getBoardViews(
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String
    ): List<BoardView>
    
    /**
     * Get rows (cards) for a specific view
     */
    @GET("api/v1/workspace/boards/{boardId}/views/{viewId}/rows")
    suspend fun getBoardRows(
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String,
        @Path("viewId") viewId: String
    ): List<BoardRow>
    
    /**
     * Create a new row (card)
     */
    @POST("api/v1/workspace/boards/{boardId}/views/{viewId}/rows")
    suspend fun createRow(
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String,
        @Path("viewId") viewId: String,
        @Body row: BoardRowCreateRequest
    ): BoardRow
    
    /**
     * Update a row (card)
     */
    @PUT("api/v1/workspace/boards/{boardId}/rows/{rowId}")
    suspend fun updateRow(
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String,
        @Path("rowId") rowId: String,
        @Body row: BoardRowUpdateRequest
    ): BoardRow
    
    /**
     * Delete a row (card)
     */
    @DELETE("api/v1/workspace/boards/{boardId}/rows/{rowId}")
    suspend fun deleteRow(
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String,
        @Path("rowId") rowId: String
    )
}

// Request/Response Models

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: UserInfo
)

data class UserInfo(
    val id: String,
    val email: String,
    val username: String,
    val displayName: String?
)

data class Board(
    val id: String,
    val name: String,
    val description: String?,
    val workspaceId: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class BoardCreateRequest(
    val name: String,
    val description: String? = null
)

data class BoardView(
    val id: String,
    val name: String,
    val boardId: String,
    val type: String, // "grid", "kanban", "gallery", etc.
    val options: ViewOptions?
)

data class ViewOptions(
    val groupByColumnId: String?,
    val sortColumnIds: List<String>?,
    val filter: Filter?
)

data class Filter(
    val columnId: String,
    val operator: String,
    val value: Any?
)

data class BoardRow(
    val id: String,
    val boardId: String,
    val viewId: String,
    val cells: Map<String, CellValue>,
    val createdAt: Long,
    val updatedAt: Long
)

data class CellValue(
    val type: String,
    val text: String? = null,
    val number: Double? = null,
    val date: Long? = null,
    val selectOptionId: String? = null
)

data class BoardRowCreateRequest(
    val cells: Map<String, CellValue>
)

data class BoardRowUpdateRequest(
    val cells: Map<String, CellValue>
)
