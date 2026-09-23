package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CheatCategory
import com.example.data.model.CheatCode
import com.example.data.model.CheatPlatform
import com.example.ui.theme.*

@Composable
fun CheatsScreen(
    cheats: List<CheatCode>,
    selectedCategory: CheatCategory,
    onSelectCategory: (CheatCategory) -> Unit,
    selectedPlatform: CheatPlatform,
    onSelectPlatform: (CheatPlatform) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showKeypadDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("cheats_screen")
    ) {
        // Platform Selection Tabs
        TabRow(
            selectedTabIndex = selectedPlatform.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SunsetGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedPlatform.ordinal]),
                    color = SunsetGold
                )
            }
        ) {
            CheatPlatform.entries.forEach { platform ->
                Tab(
                    selected = selectedPlatform == platform,
                    onClick = { onSelectPlatform(platform) },
                    text = {
                        Text(
                            text = platform.label,
                            fontWeight = if (selectedPlatform == platform) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        // Search Bar & Practice Keypad Trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search cheats, codes, weapons...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = SunsetGold)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SunsetGold,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("cheat_search_input")
            )

            FilledIconButton(
                onClick = { showKeypadDialog = true },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .size(52.dp)
                    .testTag("cheat_keyboard_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Cheat Keypad Simulator",
                    tint = SunsetGold
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CheatCategory.entries) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text(category.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SunsetGold,
                        selectedLabelColor = MidnightBlack,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Cheats List
        if (cheats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No cheats found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Try searching for HESOYAM, weapons, or jetpack.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cheats, key = { it.id }) { cheat ->
                    CheatCard(
                        cheat = cheat,
                        platform = selectedPlatform,
                        onToggleFavorite = { onToggleFavorite(cheat.id) },
                        onCopyCode = { code ->
                            copyToClipboard(context, code)
                        }
                    )
                }
            }
        }
    }

    if (showKeypadDialog) {
        CheatKeypadSimulatorDialog(
            cheats = cheats,
            onDismiss = { showKeypadDialog = false }
        )
    }
}

@Composable
private fun CheatCard(
    cheat: CheatCode,
    platform: CheatPlatform,
    onToggleFavorite: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    val codeText = when (platform) {
        CheatPlatform.ANDROID -> cheat.codePC // Android mobile keyboards use standard PC cheat letters
        CheatPlatform.PC -> cheat.codePC
        CheatPlatform.PLAYSTATION -> cheat.codePlayStation
        CheatPlatform.XBOX -> cheat.codeXbox
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (cheat.isFavorite) SunsetGold.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
            )
            .testTag("cheat_card_${cheat.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cheat.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = cheat.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = SunsetGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (cheat.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite Cheat",
                        tint = if (cheat.isFavorite) SunsetGold else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = cheat.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Code Display Box
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${platform.label} Code:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = codeText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = SunsetGold,
                            letterSpacing = 1.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = { onCopyCode(codeText) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = SunsetGold.copy(alpha = 0.2f),
                            contentColor = SunsetGold
                        ),
                        modifier = Modifier.testTag("copy_btn_${cheat.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheatKeypadSimulatorDialog(
    cheats: List<CheatCode>,
    onDismiss: () -> Unit
) {
    var typedBuffer by remember { mutableStateOf("") }
    var matchedCheat by remember { mutableStateOf<CheatCode?>(null) }
    val context = LocalContext.current

    val alphabet = ('A'..'Z').toList()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cheat Keypad Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap letters to enter cheat sequence in real-time:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input monitor
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (typedBuffer.isEmpty()) "TYPE CHEAT HERE..." else typedBuffer,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (typedBuffer.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else SunsetGold,
                            letterSpacing = 2.sp
                        )
                    }
                }

                if (matchedCheat != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GroveGreen.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = GroveGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHEAT ACTIVATED: ${matchedCheat?.title}!",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = GroveGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Grid of letters
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    alphabet.chunked(7).forEach { rowLetters ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowLetters.forEach { letter ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clickable {
                                            typedBuffer += letter
                                            // check match
                                            val found = cheats.firstOrNull { cheat ->
                                                val pcCode = cheat.codePC.split(" ").firstOrNull() ?: cheat.codePC
                                                typedBuffer.endsWith(pcCode, ignoreCase = true)
                                            }
                                            if (found != null) {
                                                matchedCheat = found
                                                triggerVibration(context)
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = letter.toString(),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            typedBuffer = ""
                            matchedCheat = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Buffer")
                    }

                    Button(
                        onClick = {
                            if (typedBuffer.isNotEmpty()) {
                                copyToClipboard(context, typedBuffer)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SunsetGold, contentColor = MidnightBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy Code", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    val clip = ClipData.newPlainText("GTA San Andreas Cheat", text)
    clipboard?.setPrimaryClip(clip)
    triggerVibration(context)
    Toast.makeText(context, "Cheat copied: $text", Toast.LENGTH_SHORT).show()
}

private fun triggerVibration(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    } catch (_: Exception) {}
}
