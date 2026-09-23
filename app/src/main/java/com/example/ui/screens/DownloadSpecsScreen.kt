package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceSpecs
import com.example.data.model.StorePlatform
import com.example.ui.theme.*

@Composable
fun DownloadSpecsScreen(
    specs: DeviceSpecs,
    onRefreshSpecs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expandedGuideIndex by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("download_specs_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. SYSTEM SPECIFICATIONS & COMPATIBILITY INSPECTOR ---
        item {
            CompatibilityCard(specs = specs, onRefresh = onRefreshSpecs)
        }

        // --- 2. OFFICIAL DOWNLOAD & STORE LAUNCHER ---
        item {
            Text(
                text = "Official Legal Downloads",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Choose the best edition for your Android device and subscription status.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Netflix Edition Card
        item {
            StoreDownloadCard(
                platform = StorePlatform.NETFLIX_EDITION,
                isInstalled = specs.isNetflixVersionInstalled,
                isCompatible = specs.isDefinitiveCompatible,
                onLaunchOrDownload = {
                    handleGameAction(context, StorePlatform.NETFLIX_EDITION.packageName, StorePlatform.NETFLIX_EDITION.storeUrl)
                }
            )
        }

        // Classic Play Store Edition Card
        item {
            StoreDownloadCard(
                platform = StorePlatform.CLASSIC_PLAYSTORE,
                isInstalled = specs.isClassicVersionInstalled,
                isCompatible = specs.isClassicCompatible,
                onLaunchOrDownload = {
                    handleGameAction(context, StorePlatform.CLASSIC_PLAYSTORE.packageName, StorePlatform.CLASSIC_PLAYSTORE.storeUrl)
                }
            )
        }

        // Rockstar Official Website Card
        item {
            StoreDownloadCard(
                platform = StorePlatform.ROCKSTAR_STORE,
                isInstalled = false,
                isCompatible = true,
                onLaunchOrDownload = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(StorePlatform.ROCKSTAR_STORE.storeUrl))
                    context.startActivity(intent)
                }
            )
        }

        // --- 3. INSTALLATION & SETUP GUIDES ---
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Installation & Setup Guides",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Step-by-step instructions for storage, graphics optimization, and controllers.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val guides = listOf(
            GuideItem(
                title = "1. How to Play Free with Netflix",
                icon = Icons.Default.SmartDisplay,
                summary = "Active Netflix members can install and play GTA San Andreas Definitive Edition at zero extra cost.",
                steps = listOf(
                    "Ensure you have an active Netflix subscription.",
                    "Tap 'Download on Google Play' in the Netflix Edition card above.",
                    "Install the app (~150MB initial APK, then additional ~3.9GB data will automatically download inside).",
                    "Launch the game and choose your Netflix profile when prompted.",
                    "Cloud saves are automatically synced to your Netflix account across all Android and iOS devices!"
                )
            ),
            GuideItem(
                title = "2. Storage Space & OBB Download Guide",
                icon = Icons.Default.SdStorage,
                summary = "Avoid download pauses and 'Insufficient storage' errors during installation.",
                steps = listOf(
                    "Total space required: 2.6 GB for Classic, 4.5 GB for Definitive Edition.",
                    "Ensure your device has at least 1-2 GB extra headroom above the minimum for smooth decompressing.",
                    "If the download pauses with 'Download paused because WiFi is disabled', open Play Store Settings > Network preferences > App download preference > set to 'Over any network'.",
                    "Never delete files from Android/obb or Android/data as these contain the radio stations and high-res world textures."
                )
            ),
            GuideItem(
                title = "3. Best Graphics Settings for 60 FPS",
                icon = Icons.Default.Tune,
                summary = "Configure graphics for buttery smooth frame rate on mid-range and high-end phones.",
                steps = listOf(
                    "Open Game Menu > Options > Graphics.",
                    "Visual Quality / Shadows: Set to 'Medium' (drastically improves performance without visual loss).",
                    "Draw Distance: Set between 65% and 75% for optimal balance of view distance and frame stability.",
                    "Resolution: Keep at 100% on 1080p screens, or reduce to 80% if experiencing thermal throttling.",
                    "Frame Limiter: Disable the 30 FPS Frame Limiter to unlock 60 FPS / 120 FPS high refresh rate gaming!"
                )
            ),
            GuideItem(
                title = "4. Bluetooth Controller Setup (PS5 / Xbox)",
                icon = Icons.Default.SportsEsports,
                summary = "San Andreas features native support for wireless gamepads and mobile clips.",
                steps = listOf(
                    "Put your controller into pairing mode (PS5: Hold Create + PS button; Xbox: Hold Sync button).",
                    "Open your phone's Bluetooth settings and tap the controller to connect.",
                    "Launch GTA San Andreas. The game automatically switches button prompts to console layout!",
                    "Go to Options > Controls to switch between Classic and Adapted steering controls."
                )
            ),
            GuideItem(
                title = "5. Troubleshooting Crashes on Android 12/13/14+",
                icon = Icons.Default.Build,
                summary = "Quick fixes for black screens, audio glitches, or startup crashes.",
                steps = listOf(
                    "Go to Phone Settings > Apps > GTA San Andreas > Storage > Clear Cache.",
                    "Restart your phone to clear RAM background apps.",
                    "If crashing during the opening intro, tap the screen repeatedly to skip the Rockstar intro sequence.",
                    "Ensure Battery Saver is set to 'Unrestricted' for GTA San Andreas so background processes don't kill the game."
                )
            )
        )

        items(guides.size) { index ->
            val guide = guides[index]
            val isExpanded = expandedGuideIndex == index

            GuideAccordionCard(
                guide = guide,
                isExpanded = isExpanded,
                onToggle = {
                    expandedGuideIndex = if (isExpanded) -1 else index
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CompatibilityCard(
    specs: DeviceSpecs,
    onRefresh: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SunsetGold.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = null,
                                tint = SunsetGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Device Compatibility",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = specs.deviceModel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onRefresh, modifier = Modifier.testTag("refresh_specs_btn")) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh System Specs",
                        tint = SunsetGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Meter
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Available Storage",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${specs.availableStorageGb} GB / ${specs.totalStorageGb} GB",
                        style = MaterialTheme.typography.bodySmall,
                        color = SunsetGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val usedFraction = if (specs.totalStorageGb > 0f) {
                    ((specs.totalStorageGb - specs.availableStorageGb) / specs.totalStorageGb).coerceIn(0f, 1f)
                } else 0.5f

                LinearProgressIndicator(
                    progress = { usedFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (specs.availableStorageGb >= 4.5f) GroveGreen else if (specs.availableStorageGb >= 2.5f) SunsetGold else WantedRed,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Spec Highlights Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecBadge(
                    label = "RAM Memory",
                    value = "${specs.totalRamGb} GB Total",
                    isPass = specs.totalRamGb >= 2.8f,
                    modifier = Modifier.weight(1f)
                )
                SpecBadge(
                    label = "Android OS",
                    value = "${specs.androidVersion} (API ${specs.apiLevel})",
                    isPass = specs.apiLevel >= 24,
                    modifier = Modifier.weight(1f)
                )
                SpecBadge(
                    label = "Definitive Ready",
                    value = if (specs.isDefinitiveCompatible) "Compatible" else "Needs Space",
                    isPass = specs.isDefinitiveCompatible,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SpecBadge(
    label: String,
    value: String,
    isPass: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isPass) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isPass) GroveGreen else WantedRed,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StoreDownloadCard(
    platform: StorePlatform,
    isInstalled: Boolean,
    isCompatible: Boolean,
    onLaunchOrDownload: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isInstalled) GroveGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = platform.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = platform.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (platform.freeWithSubscription) GroveGreen.copy(alpha = 0.2f) else SunsetGold.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = if (platform.freeWithSubscription) "NETFLIX INCLUDED" else platform.estimatedSize,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (platform.freeWithSubscription) GroveGreen else SunsetGold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DataUsage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Approx ${platform.estimatedSize}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onLaunchOrDownload,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInstalled) GroveGreen else SunsetGold,
                        contentColor = MidnightBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("action_btn_${platform.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = if (isInstalled) Icons.Default.PlayArrow else Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isInstalled) "Launch Game" else if (platform.freeWithSubscription) "Get on Play Store" else "View on Store",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class GuideItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val summary: String,
    val steps: List<String>
)

@Composable
private fun GuideAccordionCard(
    guide: GuideItem,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = guide.icon,
                        contentDescription = null,
                        tint = SunsetGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = guide.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = guide.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    guide.steps.forEachIndexed { i, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                color = SunsetGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleGameAction(context: Context, packageName: String?, fallbackUrl: String) {
    if (packageName != null) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            context.startActivity(launchIntent)
            return
        }
        // Try opening in Play Store app directly
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(marketIntent)
            return
        } catch (_: Exception) {
            // Fall back to web URL below
        }
    }

    try {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(webIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
