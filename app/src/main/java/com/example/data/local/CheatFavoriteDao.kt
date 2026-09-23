package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CheatFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheatFavoriteDao {
    @Query("SELECT cheatId FROM cheat_favorites")
    fun getAllFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: CheatFavoriteEntity)

    @Query("DELETE FROM cheat_favorites WHERE cheatId = :cheatId")
    suspend fun removeFavorite(cheatId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM cheat_favorites WHERE cheatId = :cheatId)")
    suspend fun isFavorite(cheatId: String): Boolean
}
