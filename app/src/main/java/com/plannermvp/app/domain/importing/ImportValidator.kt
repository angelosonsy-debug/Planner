package com.plannermvp.app.domain.importing

import com.plannermvp.app.data.local.TaskPriority

enum class ImportIssueLevel { WARNING, ERROR }

data class ImportIssue(val level: ImportIssueLevel, val message: String)

/**
 * A parsed item plus everything the Preview screen (Section 7) needs to
 * show a ✅/⚠️/❌ next to it: resolved (typed) values, and the issues found
 * along the way. Nothing is dropped for being invalid — an unparseable
 * date just falls back to "no date" with a warning attached, per Section 6
 * ("يظهر الأخطاء قبل الحفظ" / "يحافظ على البيانات غير المعروفة").
 */
data class ValidatedImportItem(
    val raw: ImportedItem,
    val resolvedPriority: TaskPriority,
    val resolvedDate: String?,
    val issues: List<ImportIssue>
) {
    val hasError: Boolean get() = issues.any { it.level == ImportIssueLevel.ERROR }
    val hasWarning: Boolean get() = issues.any { it.level == ImportIssueLevel.WARNING }
}

object ImportValidator {

    private val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")

    fun validate(items: List<ImportedItem>): List<ValidatedImportItem> {
        val seenWithinBatch = mutableSetOf<Triple<String?, String, String?>>()

        return items.map { item ->
            val issues = mutableListOf<ImportIssue>()

            if (item.task.isBlank()) {
                issues += ImportIssue(ImportIssueLevel.ERROR, "Task title is required")
            }

            val resolvedDate: String? = when {
                item.date.isNullOrBlank() -> null
                DATE_REGEX.matches(item.date) -> item.date
                else -> {
                    issues += ImportIssue(ImportIssueLevel.WARNING, "Invalid date '${item.date}' — imported with no date")
                    null
                }
            }

            val resolvedPriority = when (item.priority?.trim()?.lowercase()) {
                null, "" -> TaskPriority.MEDIUM
                "high" -> TaskPriority.HIGH
                "medium" -> TaskPriority.MEDIUM
                "low" -> TaskPriority.LOW
                else -> {
                    issues += ImportIssue(ImportIssueLevel.WARNING, "Unknown priority '${item.priority}' — defaulted to medium")
                    TaskPriority.MEDIUM
                }
            }

            val dupKey = Triple(item.project?.trim()?.lowercase(), item.task.trim().lowercase(), resolvedDate)
            if (!seenWithinBatch.add(dupKey)) {
                issues += ImportIssue(ImportIssueLevel.WARNING, "Duplicate of another item in this file")
            }

            ValidatedImportItem(
                raw = item,
                resolvedPriority = resolvedPriority,
                resolvedDate = resolvedDate,
                issues = issues
            )
        }
    }
}
