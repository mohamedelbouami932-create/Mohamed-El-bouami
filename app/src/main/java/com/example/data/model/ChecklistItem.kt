package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChecklistCategory(val label: String, val weightPercent: Float) {
    MISSIONS("Storyline Missions", 35.0f),
    TERRITORIES("Gang Turf Control", 15.0f),
    COLLECTIBLES("Collectibles (250 Total)", 20.0f),
    VEHICLE_JOBS("R3 Vehicle & Courier", 12.0f),
    SCHOOLS_CHALLENGES("Schools & Stadiums", 10.0f),
    ASSETS_PROPERTIES("Assets & Safehouses", 8.0f)
}

data class GenericChecklistItem(
    val id: String,
    val category: ChecklistCategory,
    val title: String,
    val subtitle: String,
    val requirement: String,
    val rewardUnlocked: String,
    val location: String,
    val proTip: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "checklist_progress")
data class ChecklistProgressEntity(
    @PrimaryKey val id: String,
    val isCompleted: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
