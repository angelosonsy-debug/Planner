package com.plannermvp.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

/**
 * Called from TasksViewModel/TodayViewModel after any mutation that could
 * change what "today" looks like (create/complete/delete/postpone/edit),
 * so the widget doesn't wait for its next periodic refresh to catch up.
 * A no-op if no widget instance is currently placed on a home screen.
 */
object TodayWidgetUpdater {
    suspend fun refresh(context: Context) {
        TodayWidget().updateAll(context)
    }
}
