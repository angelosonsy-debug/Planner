package com.plannermvp.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shared empty-state used by every tab until its real feature lands
 * (Tasks/Projects in Phase 2, Import in Phase 4, Matrix in Phase 5,
 * Habits in Phase 6+). Keeps every screen visually consistent from day one.
 */
@Composable
fun EmptyState(title: String? = null, message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!title.isNullOrBlank()) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
            }
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Small section label used to break Today into Top3 / Tasks / Progress. */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
