package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ChecklistProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_progress")
    fun getAllProgress(): Flow<List<ChecklistProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setProgress(progress: ChecklistProgressEntity)

    @Query("UPDATE checklist_progress SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateStatus(id: String, isCompleted: Boolean)

    @Query("DELETE FROM checklist_progress")
    suspend fun resetAll()
}
