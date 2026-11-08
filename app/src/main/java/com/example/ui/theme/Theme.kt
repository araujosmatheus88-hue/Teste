package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DramaBoxColorScheme = darkColorScheme(
    primary = NeonPink,
    onPrimary = TextWhite,
    primaryContainer = ElectricPurple,
    onPrimaryContainer = TextWhite,
    secondary = ElectricPurple,
    onSecondary = TextWhite,
    secondaryContainer = DeepSurfaceVariant,
    onSecondaryContainer = Lavender,
    tertiary = AccentYellow,
    onTertiary = DeepBlack,
    background = DeepBlack,
    onBackground = TextWhite,
    surface = DeepSurface,
    onSurface = TextWhite,
    surfaceVariant = DeepSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = DeepCardBorder,
    outlineVariant = NeonPinkGlow
)

@Composable
fun DramaBoxTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DramaBoxColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    DramaBoxTheme(content = content)
}
