package com.focalboard.android.data.api

import retrofit2.http.*

/**
 * Focalboard v2 API client interface.
 * 
 * Based on Focalboard's v2 REST API with CSRF protection.
 * Reference: https://github.com/mattermost/focalboard
 */
interface FocalboardApi {
    
    /**
     * Get CSRF token by making a request to the API
     * This will set the CSRF cookie in the client
     */
    @GET("api/v2/auth/csrf")
    suspend fun getCsrfToken(): CsrfResponse
    
    /**
     * Authenticate with the server using v2 API
     */
    @POST("api/v2/login")
    suspend fun login(
        @Header("X-Requested-With") xhrHeader: String = "XmlHttpRequest",
        @Body credentials: LoginRequestV2
    ): AuthResponseV2
    
    /**
     * Get current user info
     */
    @GET("api/v2/users/me")
    suspend fun getCurrentUser(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String
    ): UserInfo
    
    /**
     * Get all boards for the current user
     */
    @GET("api/v2/workspace/boards")
    suspend fun getBoards(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String
    ): List<Board>
    
    /**
     * Get a specific board by ID
     */
    @GET("api/v2/workspace/boards/{boardId}")
    suspend fun getBoard(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String
    ): Board
    
    /**
     * Create a new board
     */
    @POST("api/v2/workspace/boards")
    suspend fun createBoard(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String,
        @Body board: BoardCreateRequest
    ): Board
    
    /**
     * Get all views (layouts) for a board
     */
    @GET("api/v2/workspace/boards/{boardId}/views")
    suspend fun getBoardViews(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String
    ): List<BoardView>
    
    /**
     * Get rows (cards) for a specific view
     */
    @GET("api/v2/workspace/boards/{boardId}/views/{viewId}/rows")
    suspend fun getBoardRows(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String,
        @Path("viewId") viewId: String
    ): List<BoardRow>
    
    /**
     * Create a new row (card)
     */
    @POST("api/v2/workspace/boards/{boardId}/views/{viewId}/rows")
    suspend fun createRow(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String,
        @Path("viewId") viewId: String,
        @Body row: BoardRowCreateRequest
    ): BoardRow
    
    /**
     * Update a row (card)
     */
    @PUT("api/v2/workspace/boards/{boardId}/rows/{rowId}")
    suspend fun updateRow(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String,
        @Path("rowId") rowId: String,
        @Body row: BoardRowUpdateRequest
    ): BoardRow
    
    /**
     * Delete a row (card)
     */
    @DELETE("api/v2/workspace/boards/{boardId}/rows/{rowId}")
    suspend fun deleteRow(
        @Header("X-CSRF-Token") csrfToken: String,
        @Header("Authorization") token: String,
        @Path("boardId") boardId: String,
        @Path("rowId") rowId: String
    )
}

// Request/Response Models for v2 API

data class LoginRequestV2(
    val type: String = "normal",
    val username: String,
    val password: String
)

data class AuthResponseV2(
    val token: String,
    val user: UserInfo
)

data class CsrfResponse(
    val token: String
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
