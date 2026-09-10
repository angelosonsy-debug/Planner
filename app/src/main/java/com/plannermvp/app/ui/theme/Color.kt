package com.plannermvp.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Core neutrals ─────────────────────────────────────────────────────────────
val InkDark       = Color(0xFF111827)
val InkMedium     = Color(0xFF374151)
val Slate         = Color(0xFF6B7280)
val SlateLight    = Color(0xFF9CA3AF)
val White         = Color(0xFFFFFFFF)

// ── Light surfaces ────────────────────────────────────────────────────────────
val BackgroundLight      = Color(0xFFF6F7FB)
val SurfaceLight         = Color(0xFFFFFFFF)
val SurfaceVariantLight  = Color(0xFFEEF0F8)
val Border               = Color(0xFFE5E7EB)
val BorderLight          = Color(0xFFF3F4F6)

// ── Dark surfaces ─────────────────────────────────────────────────────────────
val BackgroundDark      = Color(0xFF111827)
val SurfaceDark         = Color(0xFF1F2937)
val SurfaceVariantDark  = Color(0xFF374151)
val BorderDark          = Color(0xFF4B5563)
val BorderDarkVariant   = Color(0xFF6B7280)

// ── Primary (calm indigo-blue) ────────────────────────────────────────────────
val PrimaryBlue          = Color(0xFF4361EE)
val PrimaryContainer     = Color(0xFFDDE3FF)
val OnPrimaryContainer   = Color(0xFF001258)
val SlateBlue            = Color(0xFF5D5F72)

val PrimaryBlueDark       = Color(0xFFB8C4FF)
val PrimaryContainerDark  = Color(0xFF0035C3)
val SlateBlueDark         = Color(0xFFC6C4DC)

// ── Accent / status ───────────────────────────────────────────────────────────
val AccentGreen  = Color(0xFF2DC653)
val AccentAmber  = Color(0xFFF9C74F)
val AccentRed    = Color(0xFFE63946)
val AccentOrange = Color(0xFFF4845F)

// ── Error ─────────────────────────────────────────────────────────────────────
val ErrorRed          = Color(0xFFBA1A1A)
val ErrorContainer    = Color(0xFFFFDAD6)
val ErrorOnContainer  = Color(0xFF410002)
val ErrorRedDark      = Color(0xFFFFB4AB)
val ErrorContainerDark = Color(0xFF93000A)

// ── Priority indicators (dots/pills — NOT full backgrounds) ───────────────────
val PriorityHighDot   = Color(0xFFE63946)
val PriorityMediumDot = Color(0xFFF9C74F)
val PriorityLowDot    = Color(0xFF9CA3AF)

// Legacy names kept for existing PriorityPill.kt — chips, not full card bgs
val PriorityHighBg    = Color(0xFFFFF0F0)   // very light tint — used in Matrix
val PriorityHighFg    = Color(0xFF991B1B)
val PriorityMediumBg  = Color(0xFFFFFBEE)
val PriorityMediumFg  = Color(0xFF92400E)
val PriorityLowBg     = Color(0xFFF4F4F4)
val PriorityLowFg     = Color(0xFF166534)

// Surface/Background legacy aliases (used in old Theme.kt, keep for compat)
val Surface    = SurfaceLight
val Background = BackgroundLight

// ── Matrix quadrant tints (light fills, dark text on top) ─────────────────────
val MatrixQ1Tint   = Color(0xFFFFF0F0)   // Very light red
val MatrixQ1Border = Color(0xFFFFCDD2)
val MatrixQ2Tint   = Color(0xFFEFF8FF)   // Very light blue
val MatrixQ2Border = Color(0xFFBBDEFB)
val MatrixQ3Tint   = Color(0xFFFFFBEE)   // Very light amber
val MatrixQ3Border = Color(0xFFFFECB3)
val MatrixQ4Tint   = Color(0xFFF4F4F4)   // Light grey
val MatrixQ4Border = Color(0xFFE0E0E0)
