package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CollectibleItem
import com.example.data.model.CollectibleType
import com.example.ui.theme.*

@Composable
fun CollectiblesScreen(
    collectibles: List<CollectibleItem>,
    selectedType: CollectibleType,
    onSelectType: (CollectibleType) -> Unit,
    statsMap: Map<CollectibleType, Pair<Int, Int>>,
    onToggleCollected: (String, Boolean) -> Unit,
    onResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var filterRemainingOnly by remember { mutableStateOf(false) }

    val currentStats = statsMap[selectedType] ?: Pair(0, selectedType.totalCount)
    val completedCount = currentStats.first
    val totalCount = currentStats.second
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val displayedItems = if (filterRemainingOnly) {
        collectibles.filter { !it.isCollected }
    } else {
        collectibles
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("collectibles_screen")
    ) {
        // Collectible Type Selector (Scrollable TabRow)
        ScrollableTabRow(
            selectedTabIndex = selectedType.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SunsetGold,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedType.ordinal]),
                    color = SunsetGold
                )
            }
        ) {
            CollectibleType.entries.forEach { type ->
                val typeStat = statsMap[type] ?: Pair(0, type.totalCount)
                Tab(
                    selected = selectedType == type,
                    onClick = { onSelectType(type) },
                    text = {
                        Text(
                            text = "${type.title} (${typeStat.first}/${typeStat.second})",
                            fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Summary Card with Progress & Reward Details
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedType.city} - ${selectedType.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$completedCount of $totalCount collected (${(progressFraction * 100).toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SunsetGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Progress",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (progressFraction == 1f) GroveGreen else SunsetGold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MilitaryTech,
                            contentDescription = null,
                            tint = SunsetGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reward: ${selectedType.reward}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Filter Bar (Remaining Only Toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Locations & Hints",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            FilterChip(
                selected = filterRemainingOnly,
                onClick = { filterRemainingOnly = !filterRemainingOnly },
                label = { Text("Uncollected Only") },
                leadingIcon = {
                    if (filterRemainingOnly) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                },
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Collectibles List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedItems, key = { it.id }) { item ->
                CollectibleRowCard(
                    item = item,
                    onToggle = { onToggleCollected(item.id, item.isCollected) }
                )
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Collectibles Progress?") },
            text = { Text("This will clear all marked items back to 0%. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetAll()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = WantedRed)
                ) {
                    Text("Reset All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CollectibleRowCard(
    item: CollectibleItem,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .border(
                1.dp,
                if (item.isCollected) GroveGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .animateContentSize()
            .testTag("collectible_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCollected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = GroveGreen,
                    checkmarkColor = MidnightBlack
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (item.isCollected) GroveGreen.copy(alpha = 0.2f) else SunsetGold.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "#${item.number} ${item.area}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isCollected) GroveGreen else SunsetGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Hint: ${item.hint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
