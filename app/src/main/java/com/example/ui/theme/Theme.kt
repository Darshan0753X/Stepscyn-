package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val StepSyncDarkColorScheme = darkColorScheme(
    primary = CyberLime,
    onPrimary = DarkBg,
    primaryContainer = CyberLimeMuted,
    onPrimaryContainer = Color.White,
    secondary = ElectricTeal,
    onSecondary = DarkBg,
    tertiary = MintSuccess,
    onTertiary = DarkBg,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    error = CrimsonCoral,
    onError = DarkBg
)

val StepSyncLightColorScheme = lightColorScheme(
    primary = Color(0xFF388E3C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF00897B),
    onSecondary = Color.White,
    tertiary = Color(0xFF2E7D32),
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    error = Color(0xFFD32F2F),
    onError = Color.White
)

@Composable
fun StepSyncTheme(
    darkTheme: Boolean = true, // Default to dark mode per product specification
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StepSyncDarkColorScheme else StepSyncLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StepSyncTypography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    StepSyncTheme(darkTheme = darkTheme, content = content)
}
