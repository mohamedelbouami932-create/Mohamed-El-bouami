package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.Overall100Stats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionChecklistScreen(
    stats: Overall100Stats,
    territories: List<TerritoryItem>,
    missions: List<MissionItem>,
    collectibles: List<CollectibleItem>,
    checklistItems: List<GenericChecklistItem>,
    selectedTab: Int,
    searchQuery: String,
    filterPendingOnly: Boolean,
    onTabSelected: (Int) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFilterPendingChanged: (Boolean) -> Unit,
    onToggleTerritory: (String, Boolean) -> Unit,
    onToggleMission: (String, Boolean) -> Unit,
    onToggleCollectible: (String, Boolean) -> Unit,
    onToggleChecklistItem: (String, Boolean) -> Unit,
    onResetAllProgress: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showRewardsDialog by remember { mutableStateOf(false) }

    val tabTitles = listOf(
        "Overview",
        "Gang Turf (${territories.count { it.isControlled }}/${territories.size})",
        "Missions (${missions.count { it.isCompleted }}/${missions.size})",
        "Collectibles (${collectibles.count { it.isCollected }}/${collectibles.size})",
        "R3 Sub-Jobs",
        "Schools & Stadiums",
        "Assets & Properties"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "100% Completion Tracker",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Serif
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(
                                containerColor = SanAndreasGreen,
                                contentColor = Color.Black
                            ) {
                                Text(
                                    text = "${stats.percentage}%",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = "Room SQLite Saved • ${stats.totalCompletedItems} of ${stats.totalPossibleItems} done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showRewardsDialog = true },
                        modifier = Modifier.testTag("btn_view_rewards")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "100% Rewards",
                            tint = WarningYellow
                        )
                    }
                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("btn_reset_tracker")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Progress",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Hero Completion Gauge
            HeroProgressCard(
                stats = stats,
                onViewRewards = { showRewardsDialog = true }
            )

            // Primary Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SanAndreasGreen,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = SanAndreasGreen,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                ),
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            // Search and Pending-only Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_checklist_search"),
                    placeholder = { Text("Filter items, turf, rewards...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SanAndreasGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = filterPendingOnly,
                    onClick = { onFilterPendingChanged(!filterPendingOnly) },
                    label = { Text("Pending", fontSize = 12.sp) },
                    leadingIcon = {
                        if (filterPendingOnly) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarningYellow.copy(alpha = 0.2f),
                        selectedLabelColor = WarningYellow
                    ),
                    modifier = Modifier.testTag("chip_filter_pending")
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> OverviewTabContent(
                    stats = stats,
                    onNavigateToTab = onTabSelected
                )
                1 -> TerritoriesTabContent(
                    territories = territories,
                    onToggle = onToggleTerritory
                )
                2 -> MissionsTabContent(
                    missions = missions,
                    onToggle = onToggleMission
                )
                3 -> CollectiblesTabContent(
                    collectibles = collectibles,
                    onToggle = onToggleCollectible
                )
                4 -> GenericChecklistTabContent(
                    items = checklistItems.filter { it.category == ChecklistCategory.VEHICLE_JOBS },
                    categoryTitle = "R3 Vehicle Sub-Missions & Couriers",
                    categoryIcon = Icons.Default.DirectionsCar,
                    onToggle = onToggleChecklistItem
                )
                5 -> GenericChecklistTabContent(
                    items = checklistItems.filter { it.category == ChecklistCategory.SCHOOLS_CHALLENGES },
                    categoryTitle = "Schools, Stadiums & Special Challenges",
                    categoryIcon = Icons.Default.SportsMotorsports,
                    onToggle = onToggleChecklistItem
                )
                6 -> GenericChecklistTabContent(
                    items = checklistItems.filter { it.category == ChecklistCategory.ASSETS_PROPERTIES },
                    categoryTitle = "Safehouses & Cash-Generating Assets",
                    categoryIcon = Icons.Default.HomeWork,
                    onToggle = onToggleChecklistItem
                )
            }
        }
    }

    // Reset Progress Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset 100% Progress?")
                }
            },
            text = {
                Text(
                    "This will clear all saved Room progress for story missions, gang territories, collectibles (Tags, Snapshots, Horseshoes, Oysters), and sub-missions.\n\nAre you sure you want to reset?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetAllProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("btn_confirm_reset")
                ) {
                    Text("Yes, Reset All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 100% Rewards Detailed Dialog
    if (showRewardsDialog) {
        RewardsModalDialog(onDismiss = { showRewardsDialog = false })
    }
}

@Composable
private fun HeroProgressCard(
    stats: Overall100Stats,
    onViewRewards: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = stats.percentage / 100f,
        label = "heroProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress with % inside
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    strokeWidth = 7.dp
                )
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (stats.percentage >= 100f) WarningYellow else SanAndreasGreen,
                    strokeWidth = 7.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.percentage}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (stats.percentage >= 100f) WarningYellow else SanAndreasGreen
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stats.rankTitle,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (stats.percentage >= 100f) WarningYellow else SanAndreasGreen
                )
                Text(
                    text = stats.rankSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (stats.percentage >= 100f) WarningYellow else SanAndreasGreen,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stats.totalCompletedItems} / ${stats.totalPossibleItems} Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "View 100% Rewards ›",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = WarningYellow
                        ),
                        modifier = Modifier.clickable { onViewRewards() }
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    stats: Overall100Stats,
    onNavigateToTab: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "100% Breakdown Gauges",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        item {
            OverviewCategoryCard(
                title = "Gang Turf Control",
                subtitle = "Take over Los Santos territories from Ballas & Vagos",
                count = stats.territoriesControlled,
                total = stats.totalTerritories,
                icon = Icons.Default.Security,
                accentColor = Color(0xFF6A1B9A),
                onClick = { onNavigateToTab(1) }
            )
        }

        item {
            OverviewCategoryCard(
                title = "Storyline Missions",
                subtitle = "Los Santos, Badlands, San Fierro, Desert, Las Venturas & Riots",
                count = stats.missionsCompleted,
                total = stats.totalMissions,
                icon = Icons.Default.Assignment,
                accentColor = SanAndreasGreen,
                onClick = { onNavigateToTab(2) }
            )
        }

        item {
            OverviewCategoryCard(
                title = "Collectibles (250 Total)",
                subtitle = "100 Tags, 50 Snapshots, 50 Horseshoes, 50 Oysters",
                count = stats.collectiblesCollected,
                total = stats.totalCollectibles,
                icon = Icons.Default.PinDrop,
                accentColor = WarningYellow,
                onClick = { onNavigateToTab(3) }
            )
        }

        item {
            OverviewCategoryCard(
                title = "R3 Vehicle Jobs & Couriers",
                subtitle = "Firefighter, Paramedic, Vigilante, Taxi 50, Pimping, Freight, Couriers",
                count = stats.vehicleJobsCompleted,
                total = stats.totalVehicleJobs,
                icon = Icons.Default.DirectionsCar,
                accentColor = Color(0xFF0288D1),
                onClick = { onNavigateToTab(4) }
            )
        }

        item {
            OverviewCategoryCard(
                title = "Schools & Stadiums",
                subtitle = "Driving, Flying, Boat, Bike schools, 8-Track, Bloodring, Dirt Track, Gyms",
                count = stats.schoolsCompleted,
                total = stats.totalSchools,
                icon = Icons.Default.SportsMotorsports,
                accentColor = Color(0xFFE64A19),
                onClick = { onNavigateToTab(5) }
            )
        }

        item {
            OverviewCategoryCard(
                title = "Assets & Safehouses",
                subtitle = "Wang Cars, Zero's RC, Verdant Meadows, Quarry, 29 Safehouses",
                count = stats.assetsCompleted,
                total = stats.totalAssets,
                icon = Icons.Default.HomeWork,
                accentColor = Color(0xFF00796B),
                onClick = { onNavigateToTab(6) }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = SanAndreasGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "How Room Persistence Works",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Every toggle on this screen immediately updates the local Room SQLite database on your device. Your progress stays 100% preserved between app launches, device restarts, and offline play.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewCategoryCard(
    title: String,
    subtitle: String,
    count: Int,
    total: Int,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    val progress = if (total > 0) count.toFloat() / total else 0f
    val isComplete = count >= total && total > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$count / $total",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (isComplete) SanAndreasGreen else MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (isComplete) SanAndreasGreen else accentColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ----------------------------------------------------
// TERRITORIES TAB (LOS SANTOS GANG WARS)
// ----------------------------------------------------
@Composable
private fun TerritoriesTabContent(
    territories: List<TerritoryItem>,
    onToggle: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1B9A).copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFCE93D8))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Los Santos Gang Warfare",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Provoke turf wars by eliminating 3 rival gang members on foot. Survive 3 waves to secure the territory for Grove Street Families.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (territories.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No territories match your filter", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(territories, key = { it.id }) { territory ->
                TerritoryCardItem(
                    territory = territory,
                    onToggle = { onToggle(territory.id, territory.isControlled) }
                )
            }
        }
    }
}

@Composable
private fun TerritoryCardItem(
    territory: TerritoryItem,
    onToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val gangColor = when (territory.originalGang) {
        GangType.GROVE_STREET -> SanAndreasGreen
        GangType.BALLAS -> Color(0xFFAB47BC)
        GangType.LOS_SANTOS_VAGOS -> Color(0xFFFFA726)
        GangType.VARRIOS_LOS_AZTECAS -> Color(0xFF26C6DA)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("territory_item_${territory.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (territory.isControlled) {
                SanAndreasGreen.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (territory.isControlled) borderCard(SanAndreasGreen.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = territory.isControlled,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SanAndreasGreen,
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.testTag("chk_territory_${territory.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = territory.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (territory.isControlled) SanAndreasGreen else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(gangColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = territory.originalGang.label.split(" ").last(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = gangColor
                            )
                        }
                    }

                    Text(
                        text = "${territory.district} • ${territory.waveDifficulty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, end = 8.dp, top = 6.dp, bottom = 4.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Tactical Strategy:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SanAndreasGreen)
                    )
                    Text(
                        text = territory.tacticTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Weapon Spawns:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = WarningYellow)
                    )
                    Text(
                        text = territory.weaponSpawns,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// STORY MISSIONS TAB
// ----------------------------------------------------
@Composable
private fun MissionsTabContent(
    missions: List<MissionItem>,
    onToggle: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (missions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No missions match filter", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(missions, key = { it.id }) { mission ->
                MissionChecklistCard(
                    mission = mission,
                    onToggle = { onToggle(mission.id, mission.isCompleted) }
                )
            }
        }
    }
}

@Composable
private fun MissionChecklistCard(
    mission: MissionItem,
    onToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("mission_item_${mission.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.isCompleted) {
                SanAndreasGreen.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (mission.isCompleted) borderCard(SanAndreasGreen.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = mission.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SanAndreasGreen,
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.testTag("chk_mission_${mission.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (mission.isCompleted) SanAndreasGreen else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${mission.act.title} • Boss: ${mission.giver}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = mission.difficulty,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, end = 8.dp, top = 6.dp, bottom = 4.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Briefing: ${mission.briefing}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Walkthrough Tip: ${mission.walkthroughTip}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = SanAndreasGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reward: ${mission.reward}",
                        style = MaterialTheme.typography.labelSmall.copy(color = WarningYellow)
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// COLLECTIBLES TAB
// ----------------------------------------------------
@Composable
private fun CollectiblesTabContent(
    collectibles: List<CollectibleItem>,
    onToggle: (String, Boolean) -> Unit
) {
    var selectedTypeFilter by remember { mutableStateOf<CollectibleType?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Collectible Type Selector Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedTypeFilter == null,
                    onClick = { selectedTypeFilter = null },
                    label = { Text("All (${collectibles.size})", fontSize = 12.sp) }
                )
            }
            items(CollectibleType.entries) { type ->
                val typeCount = collectibles.count { it.type == type }
                val completedCount = collectibles.count { it.type == type && it.isCollected }
                FilterChip(
                    selected = selectedTypeFilter == type,
                    onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                    label = { Text("${type.title} ($completedCount/$typeCount)", fontSize = 12.sp) }
                )
            }
        }

        val filteredCollectibles = collectibles.filter {
            selectedTypeFilter == null || it.type == selectedTypeFilter
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredCollectibles, key = { it.id }) { item ->
                CollectibleChecklistCard(
                    item = item,
                    onToggle = { onToggle(item.id, item.isCollected) }
                )
            }
        }
    }
}

@Composable
private fun CollectibleChecklistCard(
    item: CollectibleItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("collectible_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCollected) {
                SanAndreasGreen.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(10.dp),
        border = if (item.isCollected) borderCard(SanAndreasGreen.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCollected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = SanAndreasGreen,
                    checkmarkColor = Color.Black
                ),
                modifier = Modifier.testTag("chk_collectible_${item.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${item.number} • ${item.area}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isCollected) SanAndreasGreen else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.type.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Hint: ${item.hint}",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarningYellow
                )
            }
        }
    }
}

// ----------------------------------------------------
// GENERIC CHECKLIST TAB (R3, SCHOOLS, ASSETS)
// ----------------------------------------------------
@Composable
private fun GenericChecklistTabContent(
    items: List<GenericChecklistItem>,
    categoryTitle: String,
    categoryIcon: ImageVector,
    onToggle: (String, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(categoryIcon, contentDescription = null, tint = SanAndreasGreen)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = categoryTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        if (items.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No items match filter", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(items, key = { it.id }) { item ->
                GenericChecklistCard(
                    item = item,
                    onToggle = { onToggle(item.id, item.isCompleted) }
                )
            }
        }
    }
}

@Composable
private fun GenericChecklistCard(
    item: GenericChecklistItem,
    onToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("checklist_item_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) {
                SanAndreasGreen.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (item.isCompleted) borderCard(SanAndreasGreen.copy(alpha = 0.5f)) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = item.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = SanAndreasGreen,
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.testTag("chk_generic_${item.id}")
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isCompleted) SanAndreasGreen else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, end = 8.dp, top = 6.dp, bottom = 4.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = "Requirement: ${item.requirement}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reward: ${item.rewardUnlocked}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = WarningYellow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Location: ${item.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pro Tip: ${item.proTip}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = SanAndreasGreen
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 100% REWARDS MODAL DIALOG
// ----------------------------------------------------
@Composable
private fun RewardsModalDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = WarningYellow)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Official 100% Rewards", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Upon achieving 100% Completion in GTA San Andreas, the game permanently unlocks:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                RewardRowItem(
                    title = "$1,000,000 Cash Reward",
                    desc = "Directly credited to CJ's bank balance."
                )
                RewardRowItem(
                    title = "Infinite Ammunition",
                    desc = "All weapons gain infinite reserve ammo without needing reload."
                )
                RewardRowItem(
                    title = "Hydra Fighter Jet on Sweet's Roof",
                    desc = "Permanent military jet spawn right above Grove Street cul-de-sac."
                )
                RewardRowItem(
                    title = "Rhino Military Tank Spawn",
                    desc = "Spawns underneath the railway overpass bridge in Ganton."
                )
                RewardRowItem(
                    title = "200% Vehicle Collision Armor",
                    desc = "Any vehicle CJ drives receives double durability against crashes."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SanAndreasGreen)
            ) {
                Text("Got It, OG!", color = Color.Black)
            }
        }
    )
}

@Composable
private fun RewardRowItem(title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = SanAndreasGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun borderCard(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
