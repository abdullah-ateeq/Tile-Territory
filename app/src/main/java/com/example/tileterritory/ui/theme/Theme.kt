package com.example.tileterritory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.tileterritory.data.BoardTheme
import com.example.tileterritory.data.ThemeMode

private val DarkColors = darkColorScheme(
    primary = Bronze,
    onPrimary = Graphite,
    secondary = MutedStone,
    onSecondary = Graphite,
    background = Graphite,
    onBackground = Parchment,
    surface = GraphiteSurface,
    onSurface = Parchment,
    surfaceVariant = GraphiteSurfaceHigh,
    onSurfaceVariant = MutedStone,
    surfaceContainer = GraphiteSurface,
    surfaceContainerHigh = GraphiteSurfaceHigh,
    surfaceContainerHighest = GraphiteSurfaceHigh,
    outline = GraphiteOutline,
    outlineVariant = GraphiteOutline,
    error = ErrorMuted
)

private val LightColors = lightColorScheme(
    primary = BronzeDeep,
    onPrimary = Color.White,
    secondary = InkMuted,
    onSecondary = Color.White,
    background = Linen,
    onBackground = Ink,
    surface = LinenSurface,
    onSurface = Ink,
    surfaceVariant = LinenSurfaceHigh,
    onSurfaceVariant = InkMuted,
    surfaceContainer = LinenSurface,
    surfaceContainerHigh = LinenSurfaceHigh,
    surfaceContainerHighest = LinenSurfaceHigh,
    outline = LinenOutline,
    outlineVariant = LinenOutline,
    error = ErrorMuted
)

@Composable
fun TileTerritoryTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    boardTheme: BoardTheme = BoardTheme.CLASSIC,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(LocalBoardPalette provides boardTheme.palette()) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}
