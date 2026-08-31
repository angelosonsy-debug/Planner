package com.plannermvp.app.domain.importing

enum class ImportItemType { TASK, HABIT }

/**
 * Normalized Model from Section 6 of the product spec — every parser
 * (TXT, CSV, future formats) converts its raw input into this shape before
 * anything else touches it. Fields are kept as raw strings deliberately;
 * validation (and resolving them into real types) happens as a separate
 * step (Section 43: Parser -> Validation -> Normalized Model -> Preview).
 */
data class ImportedItem(
    val type: ImportItemType = ImportItemType.TASK,
    val project: String? = null,
    val task: String,
    val date: String? = null,
    val duration: String? = null,
    val priority: String? = null,
    val importance: Boolean? = null,
    val urgency: Boolean? = null,
    val recurring: String? = null,
    val habit: String? = null,
    val notes: String? = null,
    /** 1-based line where this item started, for error messages. */
    val sourceLine: Int = 0
)
