package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_logs", primaryKeys = ["habitId", "dateString"])
data class HabitLogEntity(
    val habitId: Long,
    val dateString: String, // format "yyyy-MM-dd"
    val isCompleted: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
