package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TerritoryProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TerritoryDao {
    @Query("SELECT * FROM territory_progress")
    fun getAllProgress(): Flow<List<TerritoryProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setProgress(progress: TerritoryProgressEntity)

    @Query("UPDATE territory_progress SET isControlled = :isControlled WHERE id = :id")
    suspend fun updateStatus(id: String, isControlled: Boolean)

    @Query("DELETE FROM territory_progress")
    suspend fun resetAll()
}
