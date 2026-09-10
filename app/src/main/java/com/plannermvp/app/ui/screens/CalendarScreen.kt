package com.plannermvp.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.ui.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Release 1.0: CalendarScreen was not opening because CALENDAR route was
 * not registered in AppNavHost. Both issues are now fixed:
 *   1. Route registered in AppNavHost.
 *   2. This screen renders a real month view using the same TaskRepository
 *      as TodayScreen — no separate data source.
 */
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        MonthHeader(
            yearMonth  = state.displayMonth,
            onPrevious = { viewModel.previousMonth() },
            onNext     = { viewModel.nextMonth() }
        )

        DayOfWeekHeader()

        CalendarGrid(
            yearMonth      = state.displayMonth,
            selectedDate   = state.selectedDate,
            today          = LocalDate.now(ZoneId.systemDefault()),
            datesWithTasks = state.datesWithTasks,
            onSelectDate   = { viewModel.selectDate(it) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        val fmt = DateTimeFormatter.ofPattern("EEEE، d MMMM", Locale("ar"))
        Text(
            text     = state.selectedDate.format(fmt),
            style    = MaterialTheme.typography.titleSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        if (state.tasksForSelectedDate.isEmpty()) {
            Text(
                text     = "لا توجد مهام في هذا اليوم",
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.tasksForSelectedDate, key = { it.id }) { task ->
                    TaskRow(task = task, onToggle = {}, onPostpone = { }, onDelete = {})
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(yearMonth: YearMonth, onPrevious: () -> Unit, onNext: () -> Unit) {
    val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ar"))
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronRight, contentDescription = "الشهر السابق")
        }
        Text(yearMonth.format(fmt), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "الشهر التالي")
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    val days = listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        days.forEach { dow ->
            Text(
                text      = dow.getDisplayName(TextStyle.NARROW, Locale("ar")),
                modifier  = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.labelSmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth, selectedDate: LocalDate, today: LocalDate,
    datesWithTasks: Set<LocalDate>, onSelectDate: (LocalDate) -> Unit
) {
    val firstDay    = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value % 7  // Sun=0
    val daysInMonth = yearMonth.lengthOfMonth()
    val rows        = (startOffset + daysInMonth + 6) / 7

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val dayIndex = row * 7 + col - startOffset
                    if (dayIndex < 0 || dayIndex >= daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date       = yearMonth.atDay(dayIndex + 1)
                        val isSelected = date == selectedDate
                        val isToday    = date == today
                        val hasTasks   = date in datesWithTasks
                        val bg = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday    -> MaterialTheme.colorScheme.primaryContainer
                            else       -> Color.Transparent
                        }
                        val fg = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday    -> MaterialTheme.colorScheme.primary
                            else       -> MaterialTheme.colorScheme.onSurface
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f).aspectRatio(1f).padding(2.dp)
                                .clip(CircleShape).background(bg)
                                .clickable { onSelectDate(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text       = (dayIndex + 1).toString(),
                                    style      = MaterialTheme.typography.labelMedium,
                                    color      = fg,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasTasks) {
                                    Box(modifier = Modifier.size(4.dp).background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary, CircleShape))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
