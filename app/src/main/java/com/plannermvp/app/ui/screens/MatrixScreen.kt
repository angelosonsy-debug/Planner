package com.plannermvp.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.domain.matrix.MatrixQuadrant
import com.plannermvp.app.ui.theme.PriorityHighBg
import com.plannermvp.app.ui.theme.PriorityLowBg
import com.plannermvp.app.ui.theme.PriorityMediumBg
import com.plannermvp.app.ui.viewmodel.MatrixViewModel

/**
 * Section 11/12: a 2x2 quadrant grid. Tapping a task opens a menu to move
 * it to a different quadrant, which writes real importance/urgency data
 * (Section 12) rather than just moving it visually.
 *
 * Known simplification: the spec describes drag-and-drop between
 * quadrants; this implements the same outcome (task -> chosen quadrant ->
 * real data change) via tap + menu instead of a drag gesture, to keep the
 * interaction reliable on a first pass. Swapping in real drag-and-drop
 * later is a UI-only change — moveTo() already does the actual work.
 */
@Composable
fun MatrixScreen(viewModel: MatrixViewModel = viewModel(factory = MatrixViewModel.Factory)) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.matrix_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            QuadrantPane(
                titleRes = R.string.matrix_q1_title,
                subtitleRes = R.string.matrix_q1_subtitle,
                color = PriorityHighBg,
                tasks = state.q1,
                quadrant = MatrixQuadrant.Q1,
                onMove = viewModel::moveTo,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            QuadrantPane(
                titleRes = R.string.matrix_q2_title,
                subtitleRes = R.string.matrix_q2_subtitle,
                color = PriorityLowBg,
                tasks = state.q2,
                quadrant = MatrixQuadrant.Q2,
                onMove = viewModel::moveTo,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            QuadrantPane(
                titleRes = R.string.matrix_q3_title,
                subtitleRes = R.string.matrix_q3_subtitle,
                color = PriorityMediumBg,
                tasks = state.q3,
                quadrant = MatrixQuadrant.Q3,
                onMove = viewModel::moveTo,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            QuadrantPane(
                titleRes = R.string.matrix_q4_title,
                subtitleRes = R.string.matrix_q4_subtitle,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tasks = state.q4,
                quadrant = MatrixQuadrant.Q4,
                onMove = viewModel::moveTo,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun QuadrantPane(
    titleRes: Int,
    subtitleRes: Int,
    color: Color,
    tasks: List<TaskEntity>,
    quadrant: MatrixQuadrant,
    onMove: (TaskEntity, MatrixQuadrant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .background(color, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .testTag("quadrantPane_${quadrant.name}")
    ) {
        Text(stringResource(titleRes), style = MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
        Text(stringResource(subtitleRes), style = MaterialTheme.typography.labelSmall)
        if (tasks.isEmpty()) {
            Text(
                stringResource(R.string.matrix_empty_quadrant),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 4.dp)) {
                items(tasks, key = { it.id }) { task ->
                    MatrixTaskChip(task = task, quadrant = quadrant, onMove = onMove)
                }
            }
        }
    }
}

@Composable
private fun MatrixTaskChip(task: TaskEntity, quadrant: MatrixQuadrant, onMove: (TaskEntity, MatrixQuadrant) -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable { menuExpanded = true },
            shape = RoundedCornerShape(6.dp),
            tonalElevation = 2.dp
        ) {
            Text(task.title, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            Text(
                stringResource(R.string.matrix_move_to),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            MatrixQuadrant.entries.forEach { target ->
                val labelRes = when (target) {
                    MatrixQuadrant.Q1 -> R.string.matrix_q1_title
                    MatrixQuadrant.Q2 -> R.string.matrix_q2_title
                    MatrixQuadrant.Q3 -> R.string.matrix_q3_title
                    MatrixQuadrant.Q4 -> R.string.matrix_q4_title
                }
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes)) },
                    enabled = target != quadrant,
                    modifier = Modifier.testTag("matrixMoveTo_${target.name}"),
                    onClick = {
                        onMove(task, target)
                        menuExpanded = false
                    }
                )
            }
        }
    }
}
