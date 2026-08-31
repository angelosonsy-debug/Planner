package com.plannermvp.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.plannermvp.app.R

private data class MoreEntry(val labelRes: Int, val onClick: (() -> Unit)? = null)

@Composable
fun MoreScreen(
    onMatrixClick: () -> Unit = {},
    onImportClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val entries = listOf(
        MoreEntry(R.string.more_matrix, onMatrixClick),
        MoreEntry(R.string.more_calendar),
        MoreEntry(R.string.more_import, onImportClick),
        MoreEntry(R.string.more_settings, onSettingsClick)
    )

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        items(entries) { entry ->
            ListItem(
                headlineContent = { Text(stringResource(entry.labelRes)) },
                modifier = entry.onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
            )
            HorizontalDivider()
        }
    }
}
