package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserNoteDao {
    @Query("SELECT * FROM user_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<UserNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UserNoteEntity): Long

    @Update
    suspend fun updateNote(note: UserNoteEntity)

    @Delete
    suspend fun deleteNote(note: UserNoteEntity)
}
