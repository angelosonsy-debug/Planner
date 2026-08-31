package com.plannermvp.app.data.local

import androidx.room.TypeConverter

/** Room stores our enums as their plain names; keeps schema readable. */
class Converters {

    @TypeConverter
    fun priorityToString(value: TaskPriority): String = value.name

    @TypeConverter
    fun stringToPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun taskStatusToString(value: TaskStatus): String = value.name

    @TypeConverter
    fun stringToTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun projectStatusToString(value: ProjectStatus): String = value.name

    @TypeConverter
    fun stringToProjectStatus(value: String): ProjectStatus = ProjectStatus.valueOf(value)

    @TypeConverter
    fun habitFrequencyTypeToString(value: HabitFrequencyType): String = value.name

    @TypeConverter
    fun stringToHabitFrequencyType(value: String): HabitFrequencyType = HabitFrequencyType.valueOf(value)

    @TypeConverter
    fun habitTargetTypeToString(value: HabitTargetType): String = value.name

    @TypeConverter
    fun stringToHabitTargetType(value: String): HabitTargetType = HabitTargetType.valueOf(value)
}
