package com.example.data.model

data class DeviceSpecs(
    val totalStorageGb: Float,
    val availableStorageGb: Float,
    val totalRamGb: Float,
    val availableRamGb: Float,
    val androidVersion: String,
    val apiLevel: Int,
    val deviceModel: String,
    val cpuAbi: String,
    val isNetflixVersionInstalled: Boolean,
    val isClassicVersionInstalled: Boolean
) {
    val storagePercentFree: Int
        get() = if (totalStorageGb > 0) ((availableStorageGb / totalStorageGb) * 100).toInt() else 0

    val isClassicCompatible: Boolean
        get() = availableStorageGb >= 2.5f && totalRamGb >= 1.5f && apiLevel >= 24

    val isDefinitiveCompatible: Boolean
        get() = availableStorageGb >= 4.5f && totalRamGb >= 2.8f && apiLevel >= 26
}

enum class StorePlatform(
    val title: String,
    val subtitle: String,
    val packageName: String?,
    val storeUrl: String,
    val freeWithSubscription: Boolean,
    val estimatedSize: String
) {
    NETFLIX_EDITION(
        title = "GTA: San Andreas – The Definitive Edition",
        subtitle = "Included free with any Netflix Membership. Remastered HD visuals & controls.",
        packageName = "com.netflix.NGP.GTASanAndreasDefinitiveEdition",
        storeUrl = "https://play.google.com/store/apps/details?id=com.netflix.NGP.GTASanAndreasDefinitiveEdition",
        freeWithSubscription = true,
        estimatedSize = "4.2 GB"
    ),
    CLASSIC_PLAYSTORE(
        title = "Grand Theft Auto: San Andreas (Classic)",
        subtitle = "Original Rockstar Games port with full classic visuals & 70+ hour campaign.",
        packageName = "com.rockstargames.gtasa",
        storeUrl = "https://play.google.com/store/apps/details?id=com.rockstargames.gtasa",
        freeWithSubscription = false,
        estimatedSize = "2.6 GB"
    ),
    ROCKSTAR_STORE(
        title = "Rockstar Games Official Store (PC/Console)",
        subtitle = "Definitive Edition & Classic Trilogy on Rockstar Games Launcher, Steam & consoles.",
        packageName = null,
        storeUrl = "https://store.rockstargames.com/game/buy-grand-theft-auto-the-trilogy-the-definitive-edition",
        freeWithSubscription = false,
        estimatedSize = "45 GB (PC)"
    )
}
