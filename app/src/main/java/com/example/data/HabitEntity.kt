package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String,
    val category: String, // e.g., "Health", "Fitness", "Productivity", "Mindfulness", "Learning"
    val frequencyType: String, // "DAILY", "WEEKLY", "SPECIFIC_DAYS"
    val targetDaysJson: String = "", // comma-separated days for SPECIFIC_DAYS (e.g. "1,2,3,4,5")
    val reminderTime: String = "08:00", // "HH:mm"
    val isReminderEnabled: Boolean = true,
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
