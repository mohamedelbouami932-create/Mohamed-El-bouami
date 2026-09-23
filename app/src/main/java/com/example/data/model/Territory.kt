package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GangType(val label: String, val colorHex: Long) {
    GROVE_STREET("Grove Street Families", 0xFF2E7D32),
    BALLAS("Front Yard / Rollin' Heights Ballas", 0xFF6A1B9A),
    LOS_SANTOS_VAGOS("Los Santos Vagos", 0xFFF57F17),
    VARRIOS_LOS_AZTECAS("Varrios Los Aztecas", 0xFF00838F)
}

data class TerritoryItem(
    val id: String,
    val name: String,
    val district: String,
    val originalGang: GangType,
    val waveDifficulty: String, // High (AK-47 / SMG), Medium (Micro SMG), Low (9mm / Bat)
    val weaponSpawns: String,
    val tacticTip: String,
    val isControlled: Boolean = false
)

@Entity(tableName = "territory_progress")
data class TerritoryProgressEntity(
    @PrimaryKey val id: String,
    val isControlled: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
