package com.example.data

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allLogs: Flow<List<HabitLogEntity>> = habitDao.getAllLogs()

    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>> =
        habitDao.getLogsForDate(dateString)

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(id: Long) = habitDao.deleteHabit(id)

    suspend fun setHabitCompleted(habitId: Long, dateString: String, isCompleted: Boolean) {
        if (isCompleted) {
            habitDao.insertOrUpdateLog(HabitLogEntity(habitId = habitId, dateString = dateString, isCompleted = true))
        } else {
            habitDao.deleteLog(habitId, dateString)
        }
    }
}
