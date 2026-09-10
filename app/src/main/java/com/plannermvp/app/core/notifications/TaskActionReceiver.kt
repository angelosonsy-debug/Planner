package com.plannermvp.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.plannermvp.app.PlannerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles the "Mark complete" action on a task-reminder notification
 * (Section 29: "notification actions where useful") without opening the
 * app. goAsync() + a background coroutine because BroadcastReceiver.onReceive
 * itself must return quickly, but the Room write is a suspend call.
 */
class TaskActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != NotificationHelper.ACTION_COMPLETE_TASK) return
        val taskId = intent.getStringExtra(NotificationHelper.EXTRA_TASK_ID) ?: return
        val app = context.applicationContext as? PlannerApp ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = app.database.taskDao().getById(taskId)
                if (task != null) {
                    app.taskRepository.toggleComplete(task)
                }
                NotificationHelper.dismiss(context, taskId.hashCode())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
