package com.plannermvp.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light (default) ───────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary             = PrimaryBlue,
    onPrimary           = White,
    primaryContainer    = PrimaryContainer,
    onPrimaryContainer  = OnPrimaryContainer,
    secondary           = SlateBlue,
    onSecondary         = White,
    background          = BackgroundLight,
    onBackground        = InkDark,
    surface             = SurfaceLight,
    onSurface           = InkDark,
    surfaceVariant      = SurfaceVariantLight,
    onSurfaceVariant    = Slate,
    outline             = Border,
    outlineVariant      = BorderLight,
    error               = ErrorRed,
    onError             = White,
    errorContainer      = ErrorContainer,
    onErrorContainer    = ErrorOnContainer,
)

// ── Dark ──────────────────────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary             = PrimaryBlueDark,
    onPrimary           = InkDark,
    primaryContainer    = PrimaryContainerDark,
    onPrimaryContainer  = PrimaryBlueDark,
    secondary           = SlateBlueDark,
    onSecondary         = InkDark,
    background          = BackgroundDark,
    onBackground        = SurfaceLight,
    surface             = SurfaceDark,
    onSurface           = SurfaceLight,
    surfaceVariant      = SurfaceVariantDark,
    onSurfaceVariant    = SlateLight,
    outline             = BorderDark,
    outlineVariant      = BorderDarkVariant,
    error               = ErrorRedDark,
    onError             = InkDark,
    errorContainer      = ErrorContainerDark,
    onErrorContainer    = ErrorRedDark,
)

/**
 * App theme.
 *
 * themeMode values (from SettingsEntity.themeMode):
 *   "light"  → always light (default for Release 1.0)
 *   "dark"   → always dark
 *   "system" → follows device setting
 *
 * Light is the default because the app is productivity-first:
 * bright, clean, easy to read quickly.
 */
@Composable
fun PlannerMvpTheme(
    themeMode: String = "light",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark"   -> true
        "light"  -> false
        else     -> isSystemInDarkTheme()   // "system"
    }

    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = PlannerTypography,
        content     = content
    )
}
