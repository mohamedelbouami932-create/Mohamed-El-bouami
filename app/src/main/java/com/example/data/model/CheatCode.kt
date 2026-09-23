package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CheatCategory(val label: String, val iconName: String) {
    ALL("All Cheats", "All"),
    WEAPONS_HEALTH("Health & Weapons", "Shield"),
    WANTED("Wanted Level", "Star"),
    VEHICLES("Spawn Vehicles", "DirectionsCar"),
    CJ_STATS("CJ Abilities", "FitnessCenter"),
    WEATHER("Weather & Time", "WbSunny"),
    PEDS_WORLD("Chaos & Peds", "Groups")
}

enum class CheatPlatform(val label: String) {
    ANDROID("Android / Touch"),
    PC("PC Keyboard"),
    PLAYSTATION("PlayStation"),
    XBOX("Xbox")
}

data class CheatCode(
    val id: String,
    val title: String,
    val description: String,
    val category: CheatCategory,
    val codeAndroid: String,
    val codePC: String,
    val codePlayStation: String,
    val codeXbox: String,
    val isFavorite: Boolean = false
)

@Entity(tableName = "cheat_favorites")
data class CheatFavoriteEntity(
    @PrimaryKey val cheatId: String,
    val savedAt: Long = System.currentTimeMillis()
)
