package com.plannermvp.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.plannermvp.app.MainActivity
import com.plannermvp.app.PlannerApp
import com.plannermvp.app.R
import com.plannermvp.app.data.local.TaskEntity
import com.plannermvp.app.data.local.TaskStatus
import com.plannermvp.app.domain.TodaySummary
import com.plannermvp.app.domain.buildTodaySummary
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val TASK_ID_KEY = ActionParameters.Key<String>("task_id")

/**
 * Section 22/30's Today widget: shows the same Top 3 the Today tab shows
 * (reusing buildTodaySummary() — Phase 3's domain logic — rather than
 * writing separate widget-only logic), lets you tap a task to mark it
 * complete, and refreshes whenever a task changes (see TodayWidgetUpdater,
 * called from TasksViewModel/TodayViewModel). RTL follows automatically
 * from Glance's layout direction handling.
 *
 * Known simplification (revised after a real CI build caught the original
 * approach not compiling): this doesn't use GlanceTheme/glance-material3
 * for adaptive light/dark colors — that dependency's exact API couldn't
 * be verified without a real Android build environment, and guessing
 * wrong cost a full CI cycle already. Text here uses Glance's default
 * colors (no explicit ColorProvider), which the widget host renders
 * correctly in both light and dark regardless; only the bold/strikethrough
 * styling is explicit, since the failed build's error output confirmed
 * those (FontWeight/TextDecoration/TextStyle) were never the problem —
 * only GlanceTheme was.
 */
class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as PlannerApp
        val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val tasks = app.database.taskDao().observeByDate(todayIso).first()
        val summary = buildTodaySummary(tasks)

        val todayLabel = context.getString(R.string.today_title)
        val emptyLabel = context.getString(R.string.today_empty)
        val progressLabel = context.getString(
            R.string.today_completed_format,
            summary.progress.completed,
            summary.progress.total
        )
        val openAppAction = actionStartActivity(Intent(context, MainActivity::class.java))

        provideContent {
            TodayWidgetContent(
                summary = summary,
                todayLabel = todayLabel,
                emptyLabel = emptyLabel,
                progressLabel = progressLabel,
                openAppAction = openAppAction
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun TodayWidgetContent(
    summary: TodaySummary,
    todayLabel: String,
    emptyLabel: String,
    progressLabel: String,
    openAppAction: Action
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text(
            text = todayLabel,
            style = TextStyle(fontWeight = FontWeight.Bold),
            modifier = GlanceModifier.clickable(openAppAction)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))

        if (summary.tasks.isEmpty()) {
            Text(text = emptyLabel)
        } else {
            val shown = summary.topThree.ifEmpty { summary.tasks.take(3) }
            shown.forEach { task -> WidgetTaskRow(task) }
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = progressLabel)
        }
    }
}

@androidx.compose.runtime.Composable
private fun WidgetTaskRow(task: TaskEntity) {
    val done = task.status == TaskStatus.COMPLETED
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(actionRunCallback<CompleteTaskAction>(actionParametersOf(TASK_ID_KEY to task.id))),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = (if (done) "\u2611 " else "\u2610 ") + task.title,
            style = TextStyle(
                textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None
            )
        )
    }
}

class CompleteTaskAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TASK_ID_KEY] ?: return
        val app = context.applicationContext as PlannerApp
        val task = app.database.taskDao().getById(taskId) ?: return
        app.taskRepository.toggleComplete(task)
        TodayWidget().update(context, glanceId)
    }
}
