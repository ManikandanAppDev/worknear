package com.worknear.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = PrimaryBlue,
    tertiary = PrimaryBlue
)

// Brand palette: map Material's primary/secondary/tertiary to WorkNear blue so component
// defaults (chip borders, ripples, etc.) never pick up the dusty-rose/pink defaults.
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = PrimaryBlue,
    onSecondary = Color.White,
    tertiary = PrimaryBlue,
    onTertiary = Color.White,
    outline = BorderGray,
    outlineVariant = BorderGray
)

@Composable
fun WorkNearTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color OFF by default so the brand blue is consistent across devices
    // (Android 12+ wallpaper colors were tinting chip outlines pink/red).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}