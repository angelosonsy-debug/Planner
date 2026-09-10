package com.plannermvp.app.domain.importing

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Release 1.0: evaluates whether a parsed import date is plausible
 * relative to today's local date.
 *
 * Plausibility levels:
 *  VALID        — today or up to 730 days future (normal planning horizon)
 *  RECENT_PAST  — 1–30 days ago (carry-forward tasks; checked by default)
 *  OLD_PAST     — 31–365 days ago (suspicious; unchecked by default)
 *  VERY_OLD     — > 365 days ago (almost certainly wrong; unchecked)
 *  FAR_FUTURE   — > 730 days future (unusual; warn)
 *
 * Anomaly threshold: if ≥ 40% of dated items are suspicious, show a
 * bulk warning banner before import can proceed.
 */
object ImportDateSanityChecker {

    const val ANOMALY_THRESHOLD = 0.40

    fun evaluate(dateString: String): DatePlausibility {
        val date = runCatching { LocalDate.parse(dateString) }.getOrNull()
            ?: return DatePlausibility.VALID   // format errors handled by ImportValidator
        val today   = LocalDate.now(ZoneId.systemDefault())
        val daysAgo = ChronoUnit.DAYS.between(date, today)  // positive = past

        return when {
            date == today       -> DatePlausibility.VALID
            daysAgo in 1..30    -> DatePlausibility.RECENT_PAST
            daysAgo in 31..365  -> DatePlausibility.OLD_PAST
            daysAgo > 365       -> DatePlausibility.VERY_OLD
            -daysAgo in 1..730  -> DatePlausibility.VALID
            -daysAgo > 730      -> DatePlausibility.FAR_FUTURE
            else                -> DatePlausibility.VALID
        }
    }

    fun analyseAll(dateStrings: List<String?>): DateAnalysisSummary {
        val dated = dateStrings.filterNotNull().filter { it.isNotBlank() }
        if (dated.isEmpty()) return DateAnalysisSummary(0, 0, 0.0, false)
        val suspicious = dated.count {
            val p = evaluate(it)
            p == DatePlausibility.OLD_PAST || p == DatePlausibility.VERY_OLD || p == DatePlausibility.FAR_FUTURE
        }
        val ratio = suspicious.toDouble() / dated.size
        return DateAnalysisSummary(dated.size, suspicious, ratio, ratio >= ANOMALY_THRESHOLD)
    }
}

enum class DatePlausibility {
    VALID, RECENT_PAST, OLD_PAST, VERY_OLD, FAR_FUTURE;

    val isWarning: Boolean        get() = this != VALID
    val isDefaultChecked: Boolean get() = this == VALID || this == RECENT_PAST
    val warningText: String get() = when (this) {
        VALID        -> ""
        RECENT_PAST  -> "التاريخ في الماضي القريب"
        OLD_PAST     -> "⚠️ التاريخ قديم نسبيًا مقارنة بتاريخ اليوم"
        VERY_OLD     -> "⚠️ التاريخ قديم جدًا — قد يكون خطأ في الخطة"
        FAR_FUTURE   -> "⚠️ التاريخ بعيد جدًا في المستقبل"
    }
}

data class DateAnalysisSummary(
    val total: Int, val suspicious: Int,
    val ratio: Double, val anomalyDetected: Boolean
)
