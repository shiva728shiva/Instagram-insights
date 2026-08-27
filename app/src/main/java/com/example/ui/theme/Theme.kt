package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = IgMagenta,
    onPrimary = Color.White,
    secondary = IgPurple,
    onSecondary = Color.White,
    tertiary = IgGreen,
    background = IgBackground,
    onBackground = IgTextPrimary,
    surface = IgBackground,
    onSurface = IgTextPrimary,
    surfaceVariant = IgCardBg,
    onSurfaceVariant = IgTextSecondary,
    outline = IgBorder,
    outlineVariant = IgDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
