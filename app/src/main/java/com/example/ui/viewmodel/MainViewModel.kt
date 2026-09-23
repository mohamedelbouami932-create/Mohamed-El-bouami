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
            noteDao = db.userNoteDao()
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
}
