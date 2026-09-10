package com.plannermvp.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Compact, productivity-first typography.
 *
 * Rule: task content is more important than chrome.
 * Keep labels small, keep hierarchy clear, avoid oversized headings.
 */
val PlannerTypography = Typography(
    // Screen title (e.g. "اليوم") — readable, not oversized
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),

    // Section headers within a screen
    titleLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    titleSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp, lineHeight = 18.sp),

    // Task titles and body text — compact but readable
    bodyLarge  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    bodySmall  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),

    // Labels, chips, secondary metadata
    labelLarge  = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
    labelSmall  = TextStyle(fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 14.sp),
)
