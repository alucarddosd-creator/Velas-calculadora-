package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VibrantPrimary,
    onPrimary = VibrantOnPrimary,
    primaryContainer = VibrantPrimaryContainer,
    onPrimaryContainer = VibrantOnPrimaryContainer,
    secondary = VibrantSecondary,
    onSecondary = VibrantOnSecondary,
    secondaryContainer = VibrantSecondaryContainer,
    onSecondaryContainer = VibrantOnSecondaryContainer,
    tertiary = VibrantTertiary,
    onTertiary = VibrantOnTertiary,
    tertiaryContainer = VibrantTertiaryContainer,
    onTertiaryContainer = VibrantOnTertiaryContainer,
    background = VibrantBackgroundDark,
    onBackground = VibrantOnSurfaceDark,
    surface = VibrantSurfaceDark,
    onSurface = VibrantOnSurfaceDark,
    surfaceVariant = VibrantSurfaceVariantDark,
    onSurfaceVariant = VibrantOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantPrimary,
    onPrimary = VibrantOnPrimary,
    primaryContainer = VibrantPrimaryContainer,
    onPrimaryContainer = VibrantOnPrimaryContainer,
    secondary = VibrantSecondary,
    onSecondary = VibrantOnSecondary,
    secondaryContainer = VibrantSecondaryContainer,
    onSecondaryContainer = VibrantOnSecondaryContainer,
    tertiary = VibrantTertiary,
    onTertiary = VibrantOnTertiary,
    tertiaryContainer = VibrantTertiaryContainer,
    onTertiaryContainer = VibrantOnTertiaryContainer,
    background = VibrantBackgroundLight,
    onBackground = VibrantOnSurfaceLight,
    surface = VibrantSurfaceLight,
    onSurface = VibrantOnSurfaceLight,
    surfaceVariant = VibrantSurfaceVariantLight,
    onSurfaceVariant = VibrantOnSurfaceVariantLight,
    outline = VibrantOutlineLight
)

@Composable
fun CostosVelasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
