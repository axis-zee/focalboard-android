package com.focalboard.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boards")
data class BoardEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val workspaceId: String,
    val serverUrl: String,
    val createdAt: Long,
    val updatedAt: Long
)
