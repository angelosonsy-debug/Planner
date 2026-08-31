package com.plannermvp.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.domain.CompletionProgress
import com.plannermvp.app.ui.viewmodel.TodayViewModel

@Composable
fun TodayScreen(viewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory)) {
    val summary by viewModel.summary.collectAsState()

    if (summary.tasks.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.today_title),
            message = stringResource(R.string.today_empty),
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = stringResource(R.string.today_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (summary.topThree.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.today_top3)) }
            items(summary.topThree, key = { "top-${it.id}" }) { task ->
                TaskRow(
                    task = task,
                    onToggle = { viewModel.toggleComplete(task) },
                    onPostpone = { viewModel.postponeToTomorrow(task) },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }

        item { SectionHeader(stringResource(R.string.today_tasks_section)) }
        items(summary.tasks, key = { it.id }) { task ->
            TaskRow(
                task = task,
                onToggle = { viewModel.toggleComplete(task) },
                onPostpone = { viewModel.postponeToTomorrow(task) },
                onDelete = { viewModel.deleteTask(task) }
            )
        }

        item { SectionHeader(stringResource(R.string.today_progress_section)) }
        item { DailyProgressCard(summary.progress) }
    }
}

@Composable
private fun DailyProgressCard(progress: CompletionProgress) {
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(stringResource(R.string.today_completed_label), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.today_completed_format, progress.completed, progress.total),
                style = MaterialTheme.typography.titleLarge
            )
            Text(stringResource(R.string.today_completion_label), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(R.string.today_completion_percent_format, progress.percent),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
