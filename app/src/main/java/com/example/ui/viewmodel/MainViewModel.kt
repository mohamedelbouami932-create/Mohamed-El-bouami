package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SanAndreasRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SanAndreasRepository

    private val _deviceSpecs = MutableStateFlow(
        DeviceSpecs(
            totalStorageGb = 0f,
            availableStorageGb = 0f,
            totalRamGb = 0f,
            availableRamGb = 0f,
            androidVersion = "Android",
            apiLevel = 34,
            deviceModel = "Device",
            cpuAbi = "arm64-v8a",
            isNetflixVersionInstalled = false,
            isClassicVersionInstalled = false
        )
    )
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    // Cheat filters
    val selectedCheatCategory = MutableStateFlow(CheatCategory.ALL)
    val selectedCheatPlatform = MutableStateFlow(CheatPlatform.ANDROID)
    val cheatSearchQuery = MutableStateFlow("")

    // Collectibles filters
    val selectedCollectibleType = MutableStateFlow(CollectibleType.TAGS)

    // Missions filters
    val selectedMissionAct = MutableStateFlow<MissionAct?>(null)

    // Compendium selection
    val selectedCompendiumTab = MutableStateFlow(0) // 0: Vehicles, 1: Weapons, 2: Radio, 3: Notes

    // Active bottom navigation item
    val selectedBottomTab = MutableStateFlow(0)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SanAndreasRepository(
            context = application,
            cheatDao = db.cheatFavoriteDao(),
            collectibleDao = db.collectibleProgressDao(),
            missionDao = db.missionProgressDao(),
            noteDao = db.userNoteDao(),
            territoryDao = db.territoryDao(),
            checklistDao = db.checklistDao()
        )
        refreshSpecs()
    }

    fun refreshSpecs() {
        viewModelScope.launch {
            _deviceSpecs.value = repository.checkDeviceSpecs()
        }
    }

    // Cheats List with Room Favorite mapping
    val cheatsList: StateFlow<List<CheatCode>> = combine(
        selectedCheatCategory,
        cheatSearchQuery,
        repository.favoriteCheatIds
    ) { category, query, favoriteIds ->
        val rawCheats = repository.getCheats()
        rawCheats.map { cheat ->
            cheat.copy(isFavorite = favoriteIds.contains(cheat.id))
        }.filter { cheat ->
            val matchesCategory = category == CheatCategory.ALL || cheat.category == category
            val matchesQuery = query.isBlank() ||
                    cheat.title.contains(query, ignoreCase = true) ||
                    cheat.description.contains(query, ignoreCase = true) ||
                    cheat.codePC.contains(query, ignoreCase = true) ||
                    cheat.codeAndroid.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }.sortedByDescending { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleCheatFavorite(cheatId: String) {
        viewModelScope.launch {
            repository.toggleCheatFavorite(cheatId)
        }
    }

    // Collectibles List with Room status mapping
    val collectiblesList: StateFlow<List<CollectibleItem>> = combine(
        selectedCollectibleType,
        repository.collectibleProgress
    ) { type, progressMap ->
        val rawItems = repository.getCollectibles()
        rawItems.filter { it.type == type }.map { item ->
            item.copy(isCollected = progressMap[item.id] == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCollectibleStats: StateFlow<Map<CollectibleType, Pair<Int, Int>>> = repository.collectibleProgress.map { progressMap ->
        val all = repository.getCollectibles()
        CollectibleType.entries.associateWith { type ->
            val typeItems = all.filter { it.type == type }
            val completed = typeItems.count { progressMap[it.id] == true }
            Pair(completed, typeItems.size)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun toggleCollectible(id: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.toggleCollectible(id, currentState)
        }
    }

    fun resetAllCollectibles() {
        viewModelScope.launch {
            repository.resetAllCollectibles()
        }
    }

    // Missions List with Room status mapping
    val missionsList: StateFlow<List<MissionItem>> = combine(
        selectedMissionAct,
        repository.missionProgress
    ) { act, progressMap ->
        val rawMissions = repository.getMissions()
        rawMissions.filter { act == null || it.act == act }.map { mission ->
            mission.copy(isCompleted = progressMap[mission.id] == true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val missionStats: StateFlow<Pair<Int, Int>> = repository.missionProgress.map { progressMap ->
        val all = repository.getMissions()
        val completed = all.count { progressMap[it.id] == true }
        Pair(completed, all.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    fun toggleMission(id: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.toggleMissionCompleted(id, currentState)
        }
    }

    fun resetAllMissions() {
        viewModelScope.launch {
            repository.resetAllMissions()
        }
    }

    // Compendium Static Lists
    val vehicles: List<VehicleInfo> = repository.getVehicles()
    val weapons: List<WeaponInfo> = repository.getWeapons()
    val radioStations: List<RadioStationInfo> = repository.getRadioStations()

    // Notes
    val userNotes: StateFlow<List<UserNoteEntity>> = repository.userNotes.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addNote(title: String, content: String, category: String) {
        viewModelScope.launch {
            repository.saveNote(title, content, category)
        }
    }

    fun deleteNote(note: UserNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // ==========================================
    // 100% COMPLETION CHECKLIST & ROOM STATE
    // ==========================================
    val checklistTab = MutableStateFlow(0) // 0: Overview, 1: Territories, 2: Missions, 3: Collectibles, 4: R3 Vehicles, 5: Schools & Challenges, 6: Assets
    val checklistSearchQuery = MutableStateFlow("")
    val checklistFilterPendingOnly = MutableStateFlow(false)

    // Gang Territories with Room progress
    val territoriesList: StateFlow<List<TerritoryItem>> = combine(
        checklistSearchQuery,
        checklistFilterPendingOnly,
        repository.territoryProgress
    ) { query, pendingOnly, progressMap ->
        val raw = repository.getTerritories()
        raw.map { item ->
            item.copy(isControlled = progressMap[item.id] == true)
        }.filter { item ->
            val matchesPending = !pendingOnly || !item.isControlled
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.district.contains(query, ignoreCase = true) ||
                    item.originalGang.label.contains(query, ignoreCase = true)
            matchesPending && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val territoryStats: StateFlow<Pair<Int, Int>> = repository.territoryProgress.map { progressMap ->
        val all = repository.getTerritories()
        val controlled = all.count { progressMap[it.id] == true }
        Pair(controlled, all.size)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0, 0))

    fun toggleTerritory(id: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.toggleTerritory(id, currentState)
        }
    }

    fun resetAllTerritories() {
        viewModelScope.launch {
            repository.resetAllTerritories()
        }
    }

    // Generic Checklist Items (R3 jobs, schools, stadiums, assets) with Room progress
    val checklistItemsList: StateFlow<List<GenericChecklistItem>> = combine(
        checklistSearchQuery,
        checklistFilterPendingOnly,
        repository.checklistProgress
    ) { query, pendingOnly, progressMap ->
        val raw = repository.getChecklistItems()
        raw.map { item ->
            item.copy(isCompleted = progressMap[item.id] == true)
        }.filter { item ->
            val matchesPending = !pendingOnly || !item.isCompleted
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.subtitle.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true) ||
                    item.rewardUnlocked.contains(query, ignoreCase = true)
            matchesPending && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checklistProgressMap: StateFlow<Map<String, Boolean>> = repository.checklistProgress.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    fun toggleChecklistItem(id: String, currentState: Boolean) {
        viewModelScope.launch {
            repository.toggleChecklistItem(id, currentState)
        }
    }

    fun resetAllChecklist() {
        viewModelScope.launch {
            repository.resetAllChecklist()
        }
    }

    // Full 100% Game Completion Stats combining Room data from all 4 systems:
    // 1. Story Missions
    // 2. Gang Territories
    // 3. Collectibles (Tags, Snapshots, Horseshoes, Oysters)
    // 4. Checklist Items (R3 Jobs, Schools, Stadiums, Assets)
    val overall100Stats: StateFlow<Overall100Stats> = combine(
        repository.missionProgress,
        repository.territoryProgress,
        repository.collectibleProgress,
        repository.checklistProgress
    ) { mProgress, tProgress, cProgress, chkProgress ->
        val missions = repository.getMissions()
        val mCount = missions.count { mProgress[it.id] == true }
        val mTotal = missions.size

        val territories = repository.getTerritories()
        val tCount = territories.count { tProgress[it.id] == true }
        val tTotal = territories.size

        val collectibles = repository.getCollectibles()
        val cCount = collectibles.count { cProgress[it.id] == true }
        val cTotal = collectibles.size

        val allChecklist = repository.getChecklistItems()
        val r3Items = allChecklist.filter { it.category == ChecklistCategory.VEHICLE_JOBS }
        val r3Count = r3Items.count { chkProgress[it.id] == true }
        val r3Total = r3Items.size

        val schoolItems = allChecklist.filter { it.category == ChecklistCategory.SCHOOLS_CHALLENGES }
        val schoolCount = schoolItems.count { chkProgress[it.id] == true }
        val schoolTotal = schoolItems.size

        val assetItems = allChecklist.filter { it.category == ChecklistCategory.ASSETS_PROPERTIES }
        val assetCount = assetItems.count { chkProgress[it.id] == true }
        val assetTotal = assetItems.size

        // Calculate weighted percentage according to San Andreas official 100% criteria
        val missionWeight = 0.35f
        val territoryWeight = 0.15f
        val collectibleWeight = 0.20f
        val r3Weight = 0.12f
        val schoolWeight = 0.10f
        val assetWeight = 0.08f

        val missionPart = if (mTotal > 0) (mCount.toFloat() / mTotal) * missionWeight else 0f
        val territoryPart = if (tTotal > 0) (tCount.toFloat() / tTotal) * territoryWeight else 0f
        val collectiblePart = if (cTotal > 0) (cCount.toFloat() / cTotal) * collectibleWeight else 0f
        val r3Part = if (r3Total > 0) (r3Count.toFloat() / r3Total) * r3Weight else 0f
        val schoolPart = if (schoolTotal > 0) (schoolCount.toFloat() / schoolTotal) * schoolWeight else 0f
        val assetPart = if (assetTotal > 0) (assetCount.toFloat() / assetTotal) * assetWeight else 0f

        val totalPercent = (missionPart + territoryPart + collectiblePart + r3Part + schoolPart + assetPart) * 100f
        val clampedPercent = totalPercent.coerceIn(0f, 100f)

        val totalCompletedItems = mCount + tCount + cCount + r3Count + schoolCount + assetCount
        val totalPossibleItems = mTotal + tTotal + cTotal + r3Total + schoolTotal + assetTotal

        val (rank, subtitle) = when {
            clampedPercent >= 100f -> "100% SAN ANDREAS GOD" to "All rewards unlocked: Infinite ammo, Hydra & Rhino at Grove St!"
            clampedPercent >= 75f -> "Grove Street Kingpin" to "True OG ruling Los Santos, San Fierro & Las Venturas"
            clampedPercent >= 50f -> "San Andreas Shot Caller" to "Halfway to legend status across the state"
            clampedPercent >= 25f -> "Respected Homie" to "Making a serious name on Grove Street"
            clampedPercent > 0f -> "Buster / Hustler" to "Putting in early work for the Families"
            else -> "Fresh off the Plane" to "Welcome home to San Andreas, CJ"
        }

        Overall100Stats(
            percentage = (clampedPercent * 10f).toInt() / 10f,
            totalCompletedItems = totalCompletedItems,
            totalPossibleItems = totalPossibleItems,
            missionsCompleted = mCount,
            totalMissions = mTotal,
            territoriesControlled = tCount,
            totalTerritories = tTotal,
            collectiblesCollected = cCount,
            totalCollectibles = cTotal,
            vehicleJobsCompleted = r3Count,
            totalVehicleJobs = r3Total,
            schoolsCompleted = schoolCount,
            totalSchools = schoolTotal,
            assetsCompleted = assetCount,
            totalAssets = assetTotal,
            rankTitle = rank,
            rankSubtitle = subtitle
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Overall100Stats(0f, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "Fresh off the Plane", "Welcome home")
    )

    fun resetEntire100PercentProgress() {
        viewModelScope.launch {
            repository.resetAllMissions()
            repository.resetAllTerritories()
            repository.resetAllCollectibles()
            repository.resetAllChecklist()
        }
    }
}

data class Overall100Stats(
    val percentage: Float,
    val totalCompletedItems: Int,
    val totalPossibleItems: Int,
    val missionsCompleted: Int,
    val totalMissions: Int,
    val territoriesControlled: Int,
    val totalTerritories: Int,
    val collectiblesCollected: Int,
    val totalCollectibles: Int,
    val vehicleJobsCompleted: Int,
    val totalVehicleJobs: Int,
    val schoolsCompleted: Int,
    val totalSchools: Int,
    val assetsCompleted: Int,
    val totalAssets: Int,
    val rankTitle: String,
    val rankSubtitle: String
)
