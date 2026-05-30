package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElegantPrimary,
    secondary = ElegantSecondary,
    tertiary = ElegantTertiary,
    background = ElegantBg,
    surface = ElegantSurface,
    surfaceVariant = ElegantSurfaceVariant,
    onPrimary = ElegantOnPrimary,
    onSecondary = ElegantOnPrimary,
    onTertiary = Color(0xFF1E2F18),
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = TextMuted,
    error = ErrorRed
)

// In an earning dashboard, a premium dark background is much more appealing and visually rich.
// We will use our custom DarkColorScheme for both light and dark systems to preserve the elite fintech look,
// with subtle alterations if requested, but defaulting to full dark-mode elegance.
private val BaseColorScheme = DarkColorScheme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BaseColorScheme,
        typography = Typography,
        content = content
    )
}
