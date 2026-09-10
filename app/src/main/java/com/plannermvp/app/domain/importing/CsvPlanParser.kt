package com.plannermvp.app.domain.importing

/**
 * Parses the CSV plan format from Section 6:
 *   Project,Task,Date,Duration,Priority,Recurring,Notes
 *
 * Column order is read from the header row (case-insensitive), so extra or
 * reordered columns are tolerated; only "Task" is required. Known gap:
 * this is a plain comma split — a comma inside a quoted cell will
 * mis-split. Fine for the MVP; worth swapping for a real CSV reader if
 * users hit that in practice.
 */
object CsvPlanParser {

    fun parse(rawText: String): List<ImportedItem> {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return emptyList()

        val header = lines.first().split(",").map { it.trim().lowercase() }
        fun colIndex(name: String) = header.indexOf(name)

        val projectIdx = colIndex("project")
        val taskIdx = colIndex("task")
        val dateIdx = colIndex("date")
        val durationIdx = colIndex("duration")
        val priorityIdx = colIndex("priority")
        val recurringIdx = colIndex("recurring")
        val notesIdx = colIndex("notes")

        if (taskIdx == -1) return emptyList() // no Task column, nothing usable to import

        return lines.drop(1).mapIndexedNotNull { rowIndex, rawRow ->
            val cells = rawRow.split(",").map { it.trim() }
            fun cell(idx: Int): String? = if (idx in cells.indices) cells[idx].takeIf { it.isNotBlank() } else null

            val task = cell(taskIdx) ?: return@mapIndexedNotNull null

            var date = cell(dateIdx)
            var recurring = cell(recurringIdx)
            if (date != null && date.equals("daily", ignoreCase = true)) {
                recurring = "daily"
                date = null
            }

            ImportedItem(
                project = cell(projectIdx),
                task = task,
                date = date,
                duration = cell(durationIdx),
                priority = cell(priorityIdx),
                recurring = recurring,
                notes = cell(notesIdx),
                sourceLine = rowIndex + 2 // header is line 1, rows are 1-based after it
            )
        }
    }
}
