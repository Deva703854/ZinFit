package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MainRepository(private val database: AppDatabase) {

    private val workoutDao = database.workoutDao()
    private val meditationDao = database.meditationDao()
    private val habitDao = database.habitDao()
    private val userSettingsDao = database.userSettingsDao()

    // Workouts
    val allSessions: Flow<List<WorkoutSession>> = workoutDao.getAllSessions()
    val allTemplates: Flow<List<WorkoutTemplate>> = workoutDao.getAllTemplates()

    suspend fun insertSession(session: WorkoutSession) {
        workoutDao.insertSession(session)
    }

    suspend fun deleteSession(session: WorkoutSession) {
        workoutDao.deleteSession(session)
    }

    suspend fun insertTemplate(template: WorkoutTemplate) {
        workoutDao.insertTemplate(template)
    }

    suspend fun deleteTemplate(template: WorkoutTemplate) {
        workoutDao.deleteTemplate(template)
    }

    suspend fun populateDefaultTemplatesIfNeeded() {
        val templates = allTemplates.first()
        if (templates.isEmpty()) {
            val defaultTemplates = listOf(
                WorkoutTemplate(
                    name = "Strength Training",
                    category = "Strength",
                    isCustom = false,
                    exercises = listOf(
                        WorkoutExercise("Barbell Squat", "Legs", 3, 10, 60f),
                        WorkoutExercise("Flat Bench Press", "Chest", 3, 10, 50f),
                        WorkoutExercise("Dumbbell Row", "Back", 3, 12, 16f),
                        WorkoutExercise("Overhead Press", "Shoulders", 3, 10, 30f)
                    )
                ),
                WorkoutTemplate(
                    name = "Cardio Burn",
                    category = "Cardio",
                    isCustom = false,
                    exercises = listOf(
                        WorkoutExercise("Treadmill Run", "Cardio", 1, 1, 0f),
                        WorkoutExercise("Stationary Bike", "Cardio", 1, 1, 0f),
                        WorkoutExercise("Elliptical Training", "Cardio", 1, 1, 0f)
                    )
                ),
                WorkoutTemplate(
                    name = "Zen Yoga Flow",
                    category = "Yoga",
                    isCustom = false,
                    exercises = listOf(
                        WorkoutExercise("Sun Salutation", "Yoga", 5, 1, 0f),
                        WorkoutExercise("Warrior Pose II", "Yoga", 3, 1, 0f),
                        WorkoutExercise("Tree Pose Balance", "Yoga", 3, 1, 0f),
                        WorkoutExercise("Child's Pose Rest", "Yoga", 1, 1, 0f)
                    )
                ),
                WorkoutTemplate(
                    name = "HIIT Intensity",
                    category = "HIIT",
                    isCustom = false,
                    exercises = listOf(
                        WorkoutExercise("Jumping Jacks", "Full Body", 4, 30, 0f),
                        WorkoutExercise("Burpees", "Full Body", 4, 12, 0f),
                        WorkoutExercise("Mountain Climbers", "Core", 4, 25, 0f),
                        WorkoutExercise("Forearm Plank", "Core", 3, 1, 0f) // 1 minute
                    )
                )
            )
            workoutDao.insertTemplates(defaultTemplates)
        }
    }

    // Meditation
    val allMeditationLogs: Flow<List<MeditationLog>> = meditationDao.getAllLogs()

    suspend fun insertMeditationLog(log: MeditationLog) {
        meditationDao.insertLog(log)
    }

    // Habits
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allHabitLogs: Flow<List<HabitLog>> = habitDao.getAllLogs()

    suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    fun getLogsForHabit(habitId: Int): Flow<List<HabitLog>> {
        return habitDao.getLogsForHabit(habitId)
    }

    suspend fun logHabitCompletion(habitId: Int, dateStr: String) {
        habitDao.insertLog(HabitLog(habitId = habitId, dateStr = dateStr))
    }

    suspend fun removeHabitCompletion(habitId: Int, dateStr: String) {
        habitDao.deleteLogByHabitAndDate(habitId, dateStr)
    }

    // Settings
    val userSettings: Flow<UserSettings?> = userSettingsDao.getSettings()

    suspend fun updateUserSettings(settings: UserSettings) {
        userSettingsDao.insertSettings(settings)
    }
}
