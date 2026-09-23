package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MissionAct(val title: String, val region: String) {
    ACT_1_LOS_SANTOS("Act 1: Los Santos", "Los Santos & Red County"),
    ACT_2_BADLANDS("Act 2: The Badlands", "Flint & Whetstone Counties"),
    ACT_3_SAN_FIERRO("Act 3: San Fierro", "San Fierro & Bays"),
    ACT_4_DESERT("Act 4: Bone County Desert", "Verdant Meadows & Area 69"),
    ACT_5_LAS_VENTURAS("Act 5: Las Venturas", "The Strip & Caligula's"),
    ACT_6_RETURN_LS("Act 6: Return to Los Santos", "Grove Street & Riots")
}

data class MissionItem(
    val id: String,
    val act: MissionAct,
    val title: String,
    val giver: String,
    val reward: String,
    val difficulty: String, // Easy, Medium, Hard, Infamous
    val briefing: String,
    val walkthroughTip: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "mission_progress")
data class MissionProgressEntity(
    @PrimaryKey val id: String,
    val isCompleted: Boolean,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
