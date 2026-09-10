package com.plannermvp.app.domain.importing

import com.plannermvp.app.data.local.TaskPriority

enum class ImportIssueLevel { WARNING, ERROR }

data class ImportIssue(val level: ImportIssueLevel, val message: String)

data class ValidatedImportItem(
    val raw: ImportedItem,
    val resolvedPriority: TaskPriority,
    val resolvedDate: String?,
    val issues: List<ImportIssue>,
    val datePlausibility: DatePlausibility = DatePlausibility.VALID
) {
    val hasError:   Boolean get() = issues.any { it.level == ImportIssueLevel.ERROR }
    val hasWarning: Boolean get() = issues.any { it.level == ImportIssueLevel.WARNING }
}

object ImportValidator {

    private val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")

    fun validate(items: List<ImportedItem>): List<ValidatedImportItem> {
        val seen = mutableSetOf<Triple<String?, String, String?>>()

        return items.map { item ->
            val issues = mutableListOf<ImportIssue>()

            if (item.task.isBlank()) {
                issues += ImportIssue(ImportIssueLevel.ERROR, "عنوان المهمة مطلوب")
            }

            val resolvedDate = when {
                item.date.isNullOrBlank()         -> null
                DATE_REGEX.matches(item.date)     -> item.date
                else -> {
                    issues += ImportIssue(
                        ImportIssueLevel.WARNING,
                        "تاريخ غير صالح '${item.date}' — سيتم الاستيراد بدون تاريخ"
                    )
                    null
                }
            }

            val resolvedPriority = when (item.priority?.trim()?.lowercase()) {
                null, "" -> TaskPriority.MEDIUM
                "high"   -> TaskPriority.HIGH
                "medium" -> TaskPriority.MEDIUM
                "low"    -> TaskPriority.LOW
                else -> {
                    issues += ImportIssue(
                        ImportIssueLevel.WARNING,
                        "أولوية غير معروفة '${item.priority}' — افتراضي: متوسطة"
                    )
                    TaskPriority.MEDIUM
                }
            }

            val dupKey = Triple(
                item.project?.trim()?.lowercase(),
                item.task.trim().lowercase(),
                resolvedDate
            )
            if (!seen.add(dupKey)) {
                issues += ImportIssue(ImportIssueLevel.WARNING, "مكررة في هذا الملف")
            }

            ValidatedImportItem(
                raw              = item,
                resolvedPriority = resolvedPriority,
                resolvedDate     = resolvedDate,
                issues           = issues,
                // datePlausibility computed for UI grouping only — does NOT add to issues
                datePlausibility = resolvedDate
                    ?.let { ImportDateSanityChecker.evaluate(it) }
                    ?: DatePlausibility.VALID
            )
        }
    }
}
