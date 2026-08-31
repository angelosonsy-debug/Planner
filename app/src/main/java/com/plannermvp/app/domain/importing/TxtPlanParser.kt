package com.plannermvp.app.domain.importing

/**
 * Parses the TXT plan format from Section 5:
 *
 *   PROJECT: React
 *   TASK: Learn useEffect
 *   DATE: 2026-08-12
 *   DURATION: 60m
 *   PRIORITY: high
 *
 * Rules from Section 6 (Smart Import Parser):
 * - blank lines are ignored
 * - fields are optional except TASK
 * - "DATE: daily" means recurring daily with no fixed date
 * - unrecognized "KEY: value" lines are folded into notes instead of
 *   discarded, so nothing the user typed silently disappears
 * - never throws — worst case, a line just doesn't map to anything
 *
 * Task keywords (PROJECT/TASK/DATE/...) are the fixed English tokens from
 * the spec; the *values* after them can be any UTF-8 text, Arabic included.
 */
object TxtPlanParser {

    private val KNOWN_KEYS = setOf("PROJECT", "TASK", "DATE", "DURATION", "PRIORITY", "RECURRING", "NOTES")

    fun parse(rawText: String): List<ImportedItem> {
        val items = mutableListOf<ImportedItem>()
        var currentProject: String? = null
        var builder: MutableMap<String, String>? = null
        var taskLine = 0

        fun flush() {
            val b = builder ?: return
            val task = b["task"]?.trim().orEmpty()
            if (task.isNotBlank()) {
                var date = b["date"]?.trim()
                var recurring = b["recurring"]?.trim()
                if (date != null && date.equals("daily", ignoreCase = true)) {
                    recurring = "daily"
                    date = null
                }
                items += ImportedItem(
                    project = currentProject?.trim()?.takeIf { it.isNotBlank() },
                    task = task,
                    date = date?.takeIf { it.isNotBlank() },
                    duration = b["duration"]?.trim()?.takeIf { it.isNotBlank() },
                    priority = b["priority"]?.trim()?.takeIf { it.isNotBlank() },
                    recurring = recurring?.takeIf { it.isNotBlank() },
                    notes = b["notes"]?.trim()?.takeIf { it.isNotBlank() },
                    sourceLine = taskLine
                )
            }
            builder = null
        }

        rawText.lines().forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachIndexed

            val colonIndex = line.indexOf(':')
            if (colonIndex <= 0) return@forEachIndexed // no "KEY: value" shape, skip silently

            val key = line.substring(0, colonIndex).trim().uppercase()
            val value = line.substring(colonIndex + 1).trim()

            when (key) {
                "PROJECT" -> {
                    flush()
                    currentProject = value
                }
                "TASK" -> {
                    flush()
                    builder = mutableMapOf("task" to value)
                    taskLine = index + 1
                }
                in KNOWN_KEYS -> {
                    builder?.set(key.lowercase(), value)
                }
                else -> {
                    // Unknown field on an active task: keep it, don't drop it (Section 6).
                    builder?.let { b ->
                        val extra = "$key: $value"
                        b["notes"] = b["notes"]?.let { "$it; $extra" } ?: extra
                    }
                }
            }
        }
        flush()

        return items
    }
}
