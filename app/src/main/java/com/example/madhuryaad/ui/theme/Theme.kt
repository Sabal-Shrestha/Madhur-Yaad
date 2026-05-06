package com.example.madhuryaad.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MadhurYaadColorScheme = lightColorScheme(
    primary = BKRed,
    onPrimary = BKWhite,
    primaryContainer = BKRedContainer,
    onPrimaryContainer = BKRedDark,

    secondary = BKTextSecondary,
    onSecondary = BKWhite,
    secondaryContainer = BKSurfaceVariant,
    onSecondaryContainer = BKTextMain,

    background = BKBackground,
    onBackground = BKTextMain,

    surface = BKWhite,
    onSurface = BKTextMain,

    surfaceVariant = BKSurfaceVariant,
    onSurfaceVariant = BKTextSecondary,

    outline = BKOutline,
    
    error = BKRed,
    onError = BKWhite
)

@Composable
fun MadhurYaadTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MadhurYaadColorScheme,
        typography = Typography,
        content = content
    )
}
