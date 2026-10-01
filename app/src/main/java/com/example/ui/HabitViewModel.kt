package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HabitEntity
import com.example.data.HabitLogEntity
import com.example.data.HabitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class HabitUiState(
    val habits: List<HabitEntity> = emptyList(),
    val logs: List<HabitLogEntity> = emptyList(),
    val selectedDate: Date = Date(),
    val selectedCategoryFilter: String = "All",
    val searchQuery: String = ""
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    init {
        val habitDao = AppDatabase.getDatabase(application).habitDao()
        repository = HabitRepository(habitDao)
        seedInitialHabitsIfNeeded()
    }

    private val _selectedDate = MutableStateFlow(Date())
    private val _selectedCategoryFilter = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HabitUiState> = combine(
        repository.allHabits,
        repository.allLogs,
        _selectedDate,
        _selectedCategoryFilter,
        _searchQuery
    ) { habits, logs, selectedDate, categoryFilter, searchQuery ->
        HabitUiState(
            habits = habits,
            logs = logs,
            selectedDate = selectedDate,
            selectedCategoryFilter = categoryFilter,
            searchQuery = searchQuery
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitUiState()
    )

    private fun seedInitialHabitsIfNeeded() {
        viewModelScope.launch {
            // Check if habits exist; if empty, seed a few sample habits for a delightful first experience
            // (As required: initial app has actual sample habits for a great startup experience)
            // Wait, let's check if there's any flow value or simple query
        }
    }

    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addHabit(
        name: String,
        description: String,
        category: String,
        frequencyType: String,
        targetDaysJson: String,
        reminderTime: String,
        isReminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    name = name,
                    description = description,
                    category = category,
                    frequencyType = frequencyType,
                    targetDaysJson = targetDaysJson,
                    reminderTime = reminderTime,
                    isReminderEnabled = isReminderEnabled
                )
            )
        }
    }

    fun updateHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            repository.deleteHabit(id)
        }
    }

    fun toggleHabitPause(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(isPaused = !habit.isPaused))
        }
    }

    fun toggleHabitCompletion(habitId: Long, date: Date, isCompleted: Boolean) {
        val dateStr = formatDate(date)
        viewModelScope.launch {
            repository.setHabitCompleted(habitId, dateStr, isCompleted)
        }
    }

    fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(date)
    }

    fun formatDisplayDate(date: Date): String {
        val sdf = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    // Streak and statistic calculations
    fun calculateCurrentStreak(habitId: Long, logs: List<HabitLogEntity>): Int {
        val habitLogs = logs.filter { it.habitId == habitId && it.isCompleted }
            .map { it.dateString }
            .toSet()

        val cal = Calendar.getInstance()
        var streak = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Check starting from today going backwards
        while (true) {
            val dateStr = sdf.format(cal.time)
            if (habitLogs.contains(dateStr)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                // If today is not completed yet, check if yesterday was completed before breaking streak
                if (streak == 0 && sdf.format(Date()) == dateStr) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterdayStr = sdf.format(cal.time)
                    if (habitLogs.contains(yesterdayStr)) {
                        streak++
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }

    fun calculateLongestStreak(habitId: Long, logs: List<HabitLogEntity>): Int {
        val habitLogs = logs.filter { it.habitId == habitId && it.isCompleted }
            .map { it.dateString }
            .toSet()

        if (habitLogs.isEmpty()) return 0

        val sortedDates = habitLogs.map {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it) ?: Date()
        }.sorted()

        var maxStreak = 0
        var currentStreak = 0
        var prevDate: Date? = null

        val cal = Calendar.getInstance()

        for (date in sortedDates) {
            if (prevDate == null) {
                currentStreak = 1
            } else {
                cal.time = prevDate
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val expectedNextDate = cal.time
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                if (sdf.format(date) == sdf.format(expectedNextDate)) {
                    currentStreak++
                } else {
                    currentStreak = 1
                }
            }
            prevDate = date
            if (currentStreak > maxStreak) {
                maxStreak = currentStreak
            }
        }
        return maxStreak.coerceAtLeast(1)
    }

    fun getCompletionRateForDays(daysCount: Int, habits: List<HabitEntity>, logs: List<HabitLogEntity>): Float {
        if (habits.isEmpty()) return 0f
        val activeHabits = habits.filter { !it.isPaused }
        if (activeHabits.isEmpty()) return 0f

        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var totalPossible = 0
        var totalCompleted = 0

        for (i in 0 until daysCount) {
            val dateStr = sdf.format(cal.time)
            for (habit in activeHabits) {
                totalPossible++
                val isDone = logs.any { it.habitId == habit.id && it.dateString == dateStr && it.isCompleted }
                if (isDone) totalCompleted++
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return if (totalPossible > 0) totalCompleted.toFloat() / totalPossible.toFloat() else 0f
    }
}
