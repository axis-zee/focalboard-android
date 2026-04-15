package com.focalboard.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BoardEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FocalboardDatabase : RoomDatabase() {
    
    abstract fun boardDao(): BoardDao
    
    companion object {
        private const val DATABASE_NAME = "focalboard_database"
        
        @Volatile
        private var INSTANCE: FocalboardDatabase? = null
        
        fun getDatabase(context: Context): FocalboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocalboardDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
