package com.example.madhuryaad.ui.theme

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

private val MadhurYaadColorScheme = lightColorScheme(
    primary = Color(0xFF7B4FC6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF24113F),

    secondary = Color(0xFF9C6B3F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE1C2),
    onSecondaryContainer = Color(0xFF331B00),

    background = Color(0xFFFFF8FF),
    onBackground = Color(0xFF1D1A22),

    surface = Color(0xFFFFF8FF),
    onSurface = Color(0xFF1D1A22),

    surfaceVariant = Color(0xFFEDE3F2),
    onSurfaceVariant = Color(0xFF4B4453),

    outline = Color(0xFF7C7284)
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