package com.plannermvp.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.domain.matrix.MatrixQuadrant
import com.plannermvp.app.ui.theme.AccentRed
import com.plannermvp.app.ui.theme.AccentAmber
import com.plannermvp.app.ui.theme.PrimaryBlue
import com.plannermvp.app.ui.theme.MatrixQ1Tint
import com.plannermvp.app.ui.theme.MatrixQ1Border
import com.plannermvp.app.ui.theme.MatrixQ2Tint
import com.plannermvp.app.ui.theme.MatrixQ2Border
import com.plannermvp.app.ui.theme.MatrixQ3Tint
import com.plannermvp.app.ui.theme.MatrixQ3Border
import com.plannermvp.app.ui.theme.MatrixQ4Tint
import com.plannermvp.app.ui.theme.MatrixQ4Border
import com.plannermvp.app.ui.viewmodel.MatrixViewModel

/**
 * Eisenhower Matrix — Release 1.0 readability fix.
 *
 * Problem: saturated Priority*Bg colors as full quadrant backgrounds
 * made text unreadable (low contrast).
 *
 * Fix: very light tinted fills + accent left-border strip + dark text always.
 * The move-to behavior is unchanged — it still writes real importance/urgency data.
 */
@Composable
fun MatrixScreen(viewModel: MatrixViewModel = viewModel(factory = MatrixViewModel.Factory)) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text     = stringResource(R.string.matrix_title),
            style    = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Row(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 4.dp)) {
            QuadrantPane(
                titleRes    = R.string.matrix_q1_title,
                subtitleRes = R.string.matrix_q1_subtitle,
                tint        = MatrixQ1Tint, border = MatrixQ1Border, accent = AccentRed,
                tasks       = state.q1, quadrant = MatrixQuadrant.Q1, onMove = viewModel::moveTo,
                modifier    = Modifier.weight(1f).fillMaxHeight()
            )
            Spacer(Modifier.width(4.dp))
            QuadrantPane(
                titleRes    = R.string.matrix_q2_title,
                subtitleRes = R.string.matrix_q2_subtitle,
                tint        = MatrixQ2Tint, border = MatrixQ2Border, accent = PrimaryBlue,
                tasks       = state.q2, quadrant = MatrixQuadrant.Q2, onMove = viewModel::moveTo,
                modifier    = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 4.dp)) {
            QuadrantPane(
                titleRes    = R.string.matrix_q3_title,
                subtitleRes = R.string.matrix_q3_subtitle,
                tint        = MatrixQ3Tint, border = MatrixQ3Border, accent = AccentAmber,
                tasks       = state.q3, quadrant = MatrixQuadrant.Q3, onMove = viewModel::moveTo,
                modifier    = Modifier.weight(1f).fillMaxHeight()
            )
            Spacer(Modifier.width(4.dp))
            QuadrantPane(
                titleRes    = R.string.matrix_q4_title,
                subtitleRes = R.string.matrix_q4_subtitle,
                tint        = MatrixQ4Tint, border = MatrixQ4Border,
                accent      = MaterialTheme.colorScheme.outline,
                tasks       = state.q4, quadrant = MatrixQuadrant.Q4, onMove = viewModel::moveTo,
                modifier    = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun QuadrantPane(
    titleRes: Int, subtitleRes: Int,
    tint: Color, border: Color, accent: Color,
    tasks: List<TaskEntity>,
    quadrant: MatrixQuadrant,
    onMove: (TaskEntity, MatrixQuadrant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tint)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .testTag("quadrantPane_${quadrant.name}")
    ) {
        // Accent left bar + title — text always dark
        Row {
            Box(
                modifier = Modifier
                    .width(3.dp).height(14.dp)
                    .background(accent, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    stringResource(titleRes),
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface  // always dark
                )
                Text(
                    stringResource(subtitleRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = border)

        if (tasks.isEmpty()) {
            Text(
                stringResource(R.string.matrix_empty_quadrant),
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 2.dp)) {
                items(tasks, key = { it.id }) { task ->
                    MatrixTaskChip(task = task, quadrant = quadrant, onMove = onMove)
                }
            }
        }
    }
}

@Composable
private fun MatrixTaskChip(
    task: TaskEntity,
    quadrant: MatrixQuadrant,
    onMove: (TaskEntity, MatrixQuadrant) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            modifier       = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .clickable { menuExpanded = true },
            shape          = RoundedCornerShape(6.dp),
            color          = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Text(
                text     = task.title,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                style    = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color    = MaterialTheme.colorScheme.onSurface,  // ALWAYS dark
                maxLines = 2
            )
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            Text(
                stringResource(R.string.matrix_move_to),
                style    = MaterialTheme.typography.labelSmall,
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
                    text    = { Text(stringResource(labelRes)) },
                    enabled = target != quadrant,
                    modifier = Modifier.testTag("matrixMoveTo_${target.name}"),
                    onClick = { onMove(task, target); menuExpanded = false }
                )
            }
        }
    }
}
