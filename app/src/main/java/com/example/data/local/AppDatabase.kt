package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CheatFavoriteEntity
import com.example.data.model.ChecklistProgressEntity
import com.example.data.model.CollectibleProgressEntity
import com.example.data.model.MissionProgressEntity
import com.example.data.model.TerritoryProgressEntity
import com.example.data.model.UserNoteEntity

@Database(
    entities = [
        CheatFavoriteEntity::class,
        CollectibleProgressEntity::class,
        MissionProgressEntity::class,
        UserNoteEntity::class,
        TerritoryProgressEntity::class,
        ChecklistProgressEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cheatFavoriteDao(): CheatFavoriteDao
    abstract fun collectibleProgressDao(): CollectibleProgressDao
    abstract fun missionProgressDao(): MissionProgressDao
    abstract fun userNoteDao(): UserNoteDao
    abstract fun territoryDao(): TerritoryDao
    abstract fun checklistDao(): ChecklistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "san_andreas_hub.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
