package com.plannermvp.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = InkDark,
    onPrimary = Surface,
    background = Background,
    surface = Surface,
    onSurface = InkDark,
    outline = Border
)

private val DarkColors = darkColorScheme(
    primary = Surface,
    onPrimary = InkDark,
    background = InkDark,
    surface = InkMedium,
    onSurface = Surface,
    outline = Slate
)

@Composable
fun PlannerMvpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = PlannerTypography,
        content = content
    )
}
