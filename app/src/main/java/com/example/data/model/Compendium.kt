package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class VehicleInfo(
    val name: String,
    val category: String,
    val speedRating: Int, // 1 to 10
    val handlingRating: Int,
    val description: String,
    val primarySpawn: String,
    val cheatCodePc: String? = null
)

data class WeaponInfo(
    val name: String,
    val category: String,
    val damageRating: Int, // 1 to 10
    val ammoCapacity: String,
    val description: String,
    val location: String
)

data class RadioStationInfo(
    val name: String,
    val frequency: String,
    val genre: String,
    val dj: String,
    val notableTracks: List<String>,
    val description: String
)

@Entity(tableName = "user_notes")
data class UserNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis()
)
