package com.plannermvp.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskStatus
import com.plannermvp.app.ui.theme.AccentGreen
import com.plannermvp.app.ui.util.CompletionSoundPlayer
import com.plannermvp.app.ui.viewmodel.TodayViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(viewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory)) {
    val summary          by viewModel.summary.collectAsState()
    val completedExpanded by viewModel.completedExpanded.collectAsState()
    val soundEnabled     by viewModel.soundEnabled.collectAsState()

    val today    = LocalDate.now(ZoneId.systemDefault())
    val dayFmt   = DateTimeFormatter.ofPattern("EEEE", Locale("ar"))
    val dateFmt  = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))

    val pending   = summary.tasks.filter { it.status != TaskStatus.COMPLETED }
    val completed = summary.tasks.filter { it.status == TaskStatus.COMPLETED }
    val total     = pending.size + completed.size
    val progress  = if (total > 0) completed.size.toFloat() / total else 0f

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick           = { /* navigate to add task */ },
                containerColor    = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
            }
        }
    ) { padding ->
        if (summary.tasks.isEmpty()) {
            EmptyState(
                title   = today.format(dayFmt),
                message = stringResource(R.string.today_empty),
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // ── Compact header ────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                    Text(
                        text  = today.format(dayFmt),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.Bottom
                    ) {
                        Text(
                            text       = today.format(dateFmt),
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (total > 0) {
                            Text(
                                text  = "${completed.size} / $total",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (total > 0) {
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress      = { progress },
                            modifier      = Modifier.fillMaxWidth().height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color         = AccentGreen,
                            trackColor    = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // ── Pending tasks ─────────────────────────────────────────────
            items(pending, key = { it.id }) { task ->
                TaskRow(
                    task        = task,
                    onToggle    = { viewModel.toggleComplete(task) },
                    onPostpone  = { viewModel.postponeToTomorrow(task) },
                    onDelete    = { viewModel.deleteTask(task) },
                    onSoundPlay = { if (soundEnabled) CompletionSoundPlayer.play() }
                )
            }

            // ── Completed section (collapsed by default) ──────────────────
            if (completed.isNotEmpty()) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { viewModel.toggleCompletedSection() }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint     = AccentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                text  = "المهام المكتملة (${completed.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            if (completedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (completedExpanded) {
                    items(completed, key = { "done-\${it.id}" }) { task ->
                        Box(modifier = Modifier.alpha(0.5f)) {
                            TaskRow(
                                task       = task,
                                onToggle   = { viewModel.toggleComplete(task) },
                                onPostpone = { viewModel.postponeToTomorrow(task) },
                                onDelete   = { viewModel.deleteTask(task) }
                            )
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
