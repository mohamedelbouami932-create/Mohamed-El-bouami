package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MissionProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionProgressDao {
    @Query("SELECT * FROM mission_progress")
    fun getAllProgress(): Flow<List<MissionProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setProgress(progress: MissionProgressEntity)

    @Query("UPDATE mission_progress SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateStatus(id: String, isCompleted: Boolean)

    @Query("DELETE FROM mission_progress")
    suspend fun resetAll()
}
