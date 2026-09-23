package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SanAndreasDarkColorScheme = darkColorScheme(
    primary = SunsetGold,
    onPrimary = MidnightBlack,
    primaryContainer = SunsetGoldDark,
    onPrimaryContainer = SunsetGoldLight,
    secondary = GroveGreen,
    onSecondary = MidnightBlack,
    secondaryContainer = GroveGreenDark,
    onSecondaryContainer = GroveGreenLight,
    tertiary = SunsetOrange,
    onTertiary = MidnightBlack,
    background = MidnightBlack,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = DarkOutline,
    error = WantedRed
)

private val SanAndreasLightColorScheme = lightColorScheme(
    primary = SunsetGoldDark,
    onPrimary = DaylightSurface,
    primaryContainer = SunsetGoldLight,
    onPrimaryContainer = MidnightBlack,
    secondary = GroveGreenDark,
    onSecondary = DaylightSurface,
    secondaryContainer = GroveGreenLight,
    onSecondaryContainer = MidnightBlack,
    tertiary = SunsetOrange,
    onTertiary = DaylightSurface,
    background = DaylightBackground,
    onBackground = TextPrimaryLight,
    surface = DaylightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = DaylightSurfaceCard,
    onSurfaceVariant = TextSecondaryLight,
    outline = DaylightOutline,
    error = WantedRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinctive San Andreas branding by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SanAndreasDarkColorScheme
        else -> SanAndreasDarkColorScheme // GTA San Andreas aesthetic shines best in dark theme!
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
