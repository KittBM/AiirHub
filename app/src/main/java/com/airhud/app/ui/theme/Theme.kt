package com.airhud.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Accent = Color(0xFF4FD1C5)

private val DarkColors = darkColorScheme(
    primary = Accent,
    secondary = Accent,
    background = Color(0xFF101418),
    surface = Color(0xFF181D22)
)

private val LightColors = lightColorScheme(
    primary = Accent,
    secondary = Accent
)

@Composable
fun AirHudTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
