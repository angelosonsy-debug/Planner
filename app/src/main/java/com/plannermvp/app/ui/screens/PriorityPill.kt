package com.plannermvp.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskPriority
import com.plannermvp.app.ui.theme.PriorityHighBg
import com.plannermvp.app.ui.theme.PriorityHighFg
import com.plannermvp.app.ui.theme.PriorityLowBg
import com.plannermvp.app.ui.theme.PriorityLowFg
import com.plannermvp.app.ui.theme.PriorityMediumBg
import com.plannermvp.app.ui.theme.PriorityMediumFg

/** Color always encodes priority (Section 35: color must mean something). */
@Composable
fun PriorityPill(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (bg, fg, labelRes) = when (priority) {
        TaskPriority.HIGH -> Triple(PriorityHighBg, PriorityHighFg, R.string.priority_high)
        TaskPriority.MEDIUM -> Triple(PriorityMediumBg, PriorityMediumFg, R.string.priority_medium)
        TaskPriority.LOW -> Triple(PriorityLowBg, PriorityLowFg, R.string.priority_low)
    }
    Text(
        text = stringResource(labelRes),
        color = fg,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
