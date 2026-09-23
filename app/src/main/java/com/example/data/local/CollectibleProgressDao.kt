package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CollectibleProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectibleProgressDao {
    @Query("SELECT * FROM collectible_progress")
    fun getAllProgress(): Flow<List<CollectibleProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setProgress(progress: CollectibleProgressEntity)

    @Query("UPDATE collectible_progress SET isCollected = :isCollected WHERE id = :id")
    suspend fun updateStatus(id: String, isCollected: Boolean)

    @Query("DELETE FROM collectible_progress")
    suspend fun resetAll()
}
