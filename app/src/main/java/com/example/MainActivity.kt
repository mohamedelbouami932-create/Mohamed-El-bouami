package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HeroHeader
import com.example.ui.screens.*
import com.example.ui.theme.MidnightBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SunsetGold
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

private enum class NavTab(val title: String, val icon: ImageVector, val tag: String) {
    DOWNLOADS("Download", Icons.Default.Download, "tab_downloads"),
    CHEATS("Cheats", Icons.Default.Key, "tab_cheats"),
    MISSIONS("Missions", Icons.Default.Assignment, "tab_missions"),
    COLLECTIBLES("100% Map", Icons.Default.PinDrop, "tab_collectibles"),
    COMPENDIUM("Guide", Icons.Default.MenuBook, "tab_compendium")
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedBottomTab.collectAsStateWithLifecycle()
    val specs by viewModel.deviceSpecs.collectAsStateWithLifecycle()

    // Cheats states
    val cheats by viewModel.cheatsList.collectAsStateWithLifecycle()
    val selectedCheatCategory by viewModel.selectedCheatCategory.collectAsStateWithLifecycle()
    val selectedCheatPlatform by viewModel.selectedCheatPlatform.collectAsStateWithLifecycle()
    val cheatSearchQuery by viewModel.cheatSearchQuery.collectAsStateWithLifecycle()

    // Collectibles states
    val collectibles by viewModel.collectiblesList.collectAsStateWithLifecycle()
    val selectedCollectibleType by viewModel.selectedCollectibleType.collectAsStateWithLifecycle()
    val collectibleStats by viewModel.totalCollectibleStats.collectAsStateWithLifecycle()

    // Missions states
    val missions by viewModel.missionsList.collectAsStateWithLifecycle()
    val selectedAct by viewModel.selectedMissionAct.collectAsStateWithLifecycle()
    val missionStats by viewModel.missionStats.collectAsStateWithLifecycle()

    // Compendium states
    val selectedCompendiumTab by viewModel.selectedCompendiumTab.collectAsStateWithLifecycle()
    val userNotes by viewModel.userNotes.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SunsetGold,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                NavTab.entries.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { viewModel.selectedBottomTab.value = index },
                        icon = {
                            Icon(imageVector = tab.icon, contentDescription = tab.title)
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MidnightBlack,
                            selectedTextColor = SunsetGold,
                            indicatorColor = SunsetGold,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Prominent Hero Banner shown on the Download screen or compact header on other tabs
            if (selectedTab == 0) {
                HeroHeader(
                    specs = specs,
                    onNavigateDownloads = { viewModel.selectedBottomTab.value = 0 }
                )
            } else {
                TopAppBarCompact(
                    title = NavTab.entries[selectedTab].title,
                    availableGb = specs.availableStorageGb,
                    onGoToDownload = { viewModel.selectedBottomTab.value = 0 }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> DownloadSpecsScreen(
                        specs = specs,
                        onRefreshSpecs = { viewModel.refreshSpecs() }
                    )
                    1 -> CheatsScreen(
                        cheats = cheats,
                        selectedCategory = selectedCheatCategory,
                        onSelectCategory = { viewModel.selectedCheatCategory.value = it },
                        selectedPlatform = selectedCheatPlatform,
                        onSelectPlatform = { viewModel.selectedCheatPlatform.value = it },
                        searchQuery = cheatSearchQuery,
                        onSearchQueryChange = { viewModel.cheatSearchQuery.value = it },
                        onToggleFavorite = { viewModel.toggleCheatFavorite(it) }
                    )
                    2 -> MissionsScreen(
                        missions = missions,
                        selectedAct = selectedAct,
                        onSelectAct = { viewModel.selectedMissionAct.value = it },
                        missionStats = missionStats,
                        onToggleCompleted = { id, curr -> viewModel.toggleMission(id, curr) },
                        onResetMissions = { viewModel.resetAllMissions() }
                    )
                    3 -> CollectiblesScreen(
                        collectibles = collectibles,
                        selectedType = selectedCollectibleType,
                        onSelectType = { viewModel.selectedCollectibleType.value = it },
                        statsMap = collectibleStats,
                        onToggleCollected = { id, curr -> viewModel.toggleCollectible(id, curr) },
                        onResetAll = { viewModel.resetAllCollectibles() }
                    )
                    4 -> CompendiumScreen(
                        vehicles = viewModel.vehicles,
                        weapons = viewModel.weapons,
                        radioStations = viewModel.radioStations,
                        userNotes = userNotes,
                        selectedSubTab = selectedCompendiumTab,
                        onSelectSubTab = { viewModel.selectedCompendiumTab.value = it },
                        onAddNote = { t, c, cat -> viewModel.addNote(t, c, cat) },
                        onDeleteNote = { viewModel.deleteNote(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopAppBarCompact(
    title: String,
    availableGb: Float,
    onGoToDownload: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SAN ANDREAS HUB",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = SunsetGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AssistChip(
                onClick = onGoToDownload,
                label = {
                    Text(
                        text = "Get Game (${availableGb}GB Free)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = SunsetGold,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = null
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
