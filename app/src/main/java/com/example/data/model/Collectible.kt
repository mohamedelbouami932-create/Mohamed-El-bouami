package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CollectibleType(
    val title: String,
    val city: String,
    val totalCount: Int,
    val reward: String
) {
    TAGS("Gang Tags", "Los Santos", 100, "Weapons at Johnson House (AK-47, Sawn-off, Tec-9, Molotovs) & armed homies"),
    SNAPSHOTS("Photo Snapshots", "San Fierro", 50, "$100,000 Cash + Sniper Rifle, Micro SMG, Shotgun, Grenades at Doherty Garage"),
    HORSESHOES("Horseshoes", "Las Venturas", 50, "$100,000 Cash + Max Casino Luck + M4, MP5, Combat Shotgun at Four Dragons"),
    OYSTERS("Oysters", "San Andreas Waters", 50, "$100,000 Cash + Max Lung Capacity + All Girlfriends Receptive")
}

data class CollectibleItem(
    val id: String,
    val type: CollectibleType,
    val number: Int,
    val area: String,
    val description: String,
    val hint: String,
    val isCollected: Boolean = false
)

@Entity(tableName = "collectible_progress")
data class CollectibleProgressEntity(
    @PrimaryKey val id: String,
    val isCollected: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
