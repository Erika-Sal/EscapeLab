package com.example.escapelab.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary          = AmberMid,
    onPrimary        = BackgroundDark,
    primaryContainer = AmberDim,
    background       = BackgroundDark,
    surface          = BackgroundCard,
    onBackground     = Parchment,
    onSurface        = Parchment,
    error            = ErrorRed,
    outline          = AmberDim
)

@Composable
fun EscapeLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}