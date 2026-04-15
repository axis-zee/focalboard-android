package com.focalboard.android.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BoardDao {
    
    @Query("SELECT * FROM boards WHERE serverUrl = :serverUrl")
    fun getBoardsByServer(serverUrl: String): Flow<List<BoardEntity>>
    
    @Query("SELECT * FROM boards WHERE serverUrl = :serverUrl")
    suspend fun getBoardsByServerSync(serverUrl: String): List<BoardEntity>
    
    @Query("SELECT * FROM boards WHERE id = :boardId")
    suspend fun getBoardById(boardId: String): BoardEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoard(board: BoardEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoards(boards: List<BoardEntity>)
    
    @Delete
    suspend fun deleteBoard(board: BoardEntity)
    
    @Query("DELETE FROM boards WHERE serverUrl = :serverUrl")
    suspend fun deleteBoardsByServer(serverUrl: String)
    
    @Query("SELECT COUNT(*) FROM boards")
    suspend fun getBoardCount(): Int
}
