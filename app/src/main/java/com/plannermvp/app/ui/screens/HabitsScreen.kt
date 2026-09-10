package com.plannermvp.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.plannermvp.app.R
import com.plannermvp.app.data.local.HabitEntity
import com.plannermvp.app.data.local.HabitFrequencyType
import com.plannermvp.app.data.local.HabitTargetType
import com.plannermvp.app.ui.viewmodel.HabitUi
import com.plannermvp.app.ui.viewmodel.HabitsViewModel

private val WEEKDAY_CODES = listOf("MO", "TU", "WE", "TH", "FR", "SA", "SU")

@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = viewModel(factory = HabitsViewModel.Factory),
    onHabitClick: (String) -> Unit = {}
) {
    val habits by viewModel.habits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_habit))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = stringResource(R.string.habits_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            if (habits.isEmpty()) {
                EmptyState(message = stringResource(R.string.habits_empty), modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(habits, key = { it.habit.id }) { habitUi ->
                        HabitRow(
                            habitUi = habitUi,
                            onToggleBinary = { viewModel.toggleBinaryToday(habitUi.habit, habitUi.todayCheckIn != null) },
                            onAdjustQuantity = { delta -> viewModel.adjustQuantityToday(habitUi.habit, delta) },
                            onClick = { onHabitClick(habitUi.habit.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, freqType, freqTarget, weekdays, targetType, targetValue, unit, reminderHour, reminderMinute ->
                viewModel.addHabit(
                    name, freqType, freqTarget, weekdays, targetType, targetValue, unit, reminderHour, reminderMinute
                )
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun HabitRow(
    habitUi: HabitUi,
    onToggleBinary: () -> Unit,
    onAdjustQuantity: (Int) -> Unit,
    onClick: () -> Unit
) {
    val habit = habitUi.habit
    val frequencyLabel = when (habit.frequencyType) {
        HabitFrequencyType.DAILY -> stringResource(R.string.habit_frequency_daily_label)
        HabitFrequencyType.TIMES_PER_WEEK -> stringResource(R.string.habit_frequency_times_format, habit.frequencyTarget)
        HabitFrequencyType.SPECIFIC_WEEKDAYS -> stringResource(R.string.frequency_specific_weekdays)
        else -> stringResource(R.string.habit_frequency_daily_label)
    }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(habit.name) },
        supportingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(frequencyLabel, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.habit_streak_format, habitUi.stats.currentStreak),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        trailingContent = {
            when (habit.targetType) {
                HabitTargetType.BINARY -> Checkbox(
                    checked = habitUi.todayCheckIn != null,
                    onCheckedChange = { onToggleBinary() }
                )
                HabitTargetType.QUANTITY -> {
                    val current = habitUi.todayCheckIn?.value ?: 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onAdjustQuantity(-1) }) {
                            Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.stepper_decrease))
                        }
                        Text(
                            stringResource(
                                R.string.habit_quantity_format,
                                current,
                                habit.targetValue,
                                habit.targetUnit.orEmpty()
                            )
                        )
                        IconButton(onClick = { onAdjustQuantity(1) }) {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.stepper_increase))
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        frequencyType: HabitFrequencyType,
        frequencyTarget: Int,
        weekdays: String?,
        targetType: HabitTargetType,
        targetValue: Int,
        targetUnit: String?,
        reminderHour: Int?,
        reminderMinute: Int?
    ) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var frequencyType by remember { mutableStateOf(HabitFrequencyType.DAILY) }
    var frequencyTarget by remember { mutableStateOf(3) }
    var selectedWeekdays by remember { mutableStateOf(setOf<String>()) }
    var targetType by remember { mutableStateOf(HabitTargetType.BINARY) }
    var targetValue by remember { mutableStateOf(1) }
    var targetUnit by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableStateOf(9) }
    var reminderMinute by remember { mutableStateOf(0) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* if denied, the reminder is simply silent — habit still saves fine */ }

    LaunchedEffect(reminderEnabled) {
        if (reminderEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_habit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.habit_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(stringResource(R.string.habit_frequency_label))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = frequencyType == HabitFrequencyType.DAILY,
                        onClick = { frequencyType = HabitFrequencyType.DAILY },
                        label = { Text(stringResource(R.string.frequency_daily)) }
                    )
                    FilterChip(
                        selected = frequencyType == HabitFrequencyType.TIMES_PER_WEEK,
                        onClick = { frequencyType = HabitFrequencyType.TIMES_PER_WEEK },
                        label = { Text(stringResource(R.string.frequency_times_per_week)) }
                    )
                    FilterChip(
                        selected = frequencyType == HabitFrequencyType.SPECIFIC_WEEKDAYS,
                        onClick = { frequencyType = HabitFrequencyType.SPECIFIC_WEEKDAYS },
                        label = { Text(stringResource(R.string.frequency_specific_weekdays)) }
                    )
                }
                if (frequencyType == HabitFrequencyType.TIMES_PER_WEEK) {
                    Stepper(value = frequencyTarget, range = 1..7, onChange = { frequencyTarget = it })
                }
                if (frequencyType == HabitFrequencyType.SPECIFIC_WEEKDAYS) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        WEEKDAY_CODES.forEach { code ->
                            val labelRes = weekdayLabelRes(code)
                            FilterChip(
                                selected = code in selectedWeekdays,
                                onClick = {
                                    selectedWeekdays = if (code in selectedWeekdays) {
                                        selectedWeekdays - code
                                    } else {
                                        selectedWeekdays + code
                                    }
                                },
                                label = { Text(stringResource(labelRes)) }
                            )
                        }
                    }
                }

                Text(stringResource(R.string.habit_target_type_label))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetType == HabitTargetType.BINARY,
                        onClick = { targetType = HabitTargetType.BINARY },
                        label = { Text(stringResource(R.string.target_type_binary)) }
                    )
                    FilterChip(
                        selected = targetType == HabitTargetType.QUANTITY,
                        onClick = { targetType = HabitTargetType.QUANTITY },
                        label = { Text(stringResource(R.string.target_type_quantity)) }
                    )
                }
                if (targetType == HabitTargetType.QUANTITY) {
                    Text(stringResource(R.string.habit_target_value_label))
                    Stepper(value = targetValue, range = 1..999, onChange = { targetValue = it })
                    OutlinedTextField(
                        value = targetUnit,
                        onValueChange = { targetUnit = it },
                        label = { Text(stringResource(R.string.habit_target_unit_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.habit_reminder_label))
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
                if (reminderEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(stringResource(R.string.habit_reminder_hour_label), style = MaterialTheme.typography.labelSmall)
                            Stepper(value = reminderHour, range = 0..23, onChange = { reminderHour = it })
                        }
                        Column {
                            Text(stringResource(R.string.habit_reminder_minute_label), style = MaterialTheme.typography.labelSmall)
                            Stepper(value = reminderMinute, range = 0..59, onChange = { reminderMinute = it })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weekdaysCsv = selectedWeekdays.takeIf { it.isNotEmpty() }
                        ?.let { set -> WEEKDAY_CODES.filter { it in set }.joinToString(",") }
                    onConfirm(
                        name,
                        frequencyType,
                        frequencyTarget,
                        weekdaysCsv,
                        targetType,
                        if (targetType == HabitTargetType.QUANTITY) targetValue else 1,
                        targetUnit.ifBlank { null },
                        if (reminderEnabled) reminderHour else null,
                        if (reminderEnabled) reminderMinute else null
                    )
                },
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

private fun weekdayLabelRes(code: String): Int = when (code) {
    "MO" -> R.string.weekday_mo
    "TU" -> R.string.weekday_tu
    "WE" -> R.string.weekday_we
    "TH" -> R.string.weekday_th
    "FR" -> R.string.weekday_fr
    "SA" -> R.string.weekday_sa
    else -> R.string.weekday_su
}

@Composable
private fun Stepper(value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { if (value - 1 >= range.first) onChange(value - 1) }) {
            Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.stepper_decrease))
        }
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = { if (value + 1 <= range.last) onChange(value + 1) }) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.stepper_increase))
        }
    }
}
