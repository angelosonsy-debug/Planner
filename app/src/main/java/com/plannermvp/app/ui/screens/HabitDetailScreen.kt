package com.plannermvp.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.R
import com.plannermvp.app.data.local.HabitCheckInEntity
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.HabitTargetType
import com.plannermvp.app.domain.habits.isCheckInComplete
import com.plannermvp.app.ui.theme.PriorityHighBg
import com.plannermvp.app.ui.theme.PriorityLowBg
import com.plannermvp.app.ui.theme.PriorityMediumBg
import com.plannermvp.app.ui.viewmodel.HabitDetailViewModel
import java.time.LocalDate

/** Section 19: the heatmap supports Week / Month / Year ranges. */
private enum class HeatmapPeriod(val labelRes: Int, val days: Int) {
    WEEK(R.string.habit_period_week, 7),
    MONTH(R.string.habit_period_month, 35),
    YEAR(R.string.habit_period_year, 371) // 53 full weeks, keeps the 7-column grid aligned
}

@Composable
fun HabitDetailScreen(habitId: String) {
    val context = LocalContext.current
    val app = context.applicationContext as PlannerApp
    val viewModel: HabitDetailViewModel = viewModel(
        factory = remember(habitId) {
            viewModelFactory { initializer { HabitDetailViewModel(app.habitRepository, habitId) } }
        }
    )
    val state by viewModel.state.collectAsState()
    val current = state ?: return
    var period by remember { mutableStateOf(HeatmapPeriod.MONTH) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(current.habit.name, style = MaterialTheme.typography.titleLarge)
        Text(stringResource(R.string.habit_detail_title), style = MaterialTheme.typography.bodyMedium)

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            item { StatCard(stringResource(R.string.habit_stat_current_streak), current.stats.currentStreak.toString()) }
            item { StatCard(stringResource(R.string.habit_stat_best_streak), current.stats.bestStreak.toString()) }
            item { StatCard(stringResource(R.string.habit_stat_this_week), "${current.stats.completionsThisWeek}/7") }
            item { StatCard(stringResource(R.string.habit_stat_this_month), "${current.stats.completionsThisMonth}/30") }
            item { StatCard(stringResource(R.string.habit_stat_completion_rate), "${current.stats.completionRatePercent}%") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeatmapPeriod.entries.forEach { candidate ->
                FilterChip(
                    selected = period == candidate,
                    onClick = { period = candidate },
                    label = { Text(stringResource(candidate.labelRes)) }
                )
            }
        }
        HabitHeatmap(habit = current.habit, checkInsByDate = current.checkInsByDate, days = period.days)
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.padding(4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** Section 18: a simple not-done/partial/done heatmap over the chosen range. */
@Composable
private fun HabitHeatmap(habit: HabitEntity, checkInsByDate: Map<String, HabitCheckInEntity>, days: Int) {
    val today = LocalDate.now()
    val range = ((days - 1) downTo 0).map { today.minusDays(it.toLong()) }

    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.padding(top = 8.dp)) {
        items(range) { day ->
            val checkIn = checkInsByDate[day.toString()]
            val color = when {
                checkIn == null -> MaterialTheme.colorScheme.surfaceVariant
                isCheckInComplete(habit, checkIn) -> PriorityLowBg
                habit.targetType == HabitTargetType.QUANTITY && checkIn.value > 0 -> PriorityMediumBg
                else -> PriorityHighBg
            }
            Box(
                modifier = Modifier
                    .padding(1.dp)
                    .size(if (days > 100) 12.dp else 28.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}
