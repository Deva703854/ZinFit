package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class WorkoutExercise(
    val name: String,
    val category: String, // e.g. "Chest", "Legs", "Back", "Cardio"
    val sets: Int,
    val reps: Int,
    val weightKg: Float,
    val completed: Boolean = false
)

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // timestamp in ms
    val name: String,
    val exercises: List<WorkoutExercise>,
    val durationSeconds: Int = 0
)

@Entity(tableName = "workout_templates")
data class WorkoutTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Strength, Cardio, Yoga, HIIT
    val exercises: List<WorkoutExercise>,
    val isCustom: Boolean = false
)

@Entity(tableName = "meditation_logs")
data class MeditationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, // timestamp
    val category: String, // Sleep, Stress, Focus, Anxiety, Timer
    val durationSeconds: Int,
    val ambientSound: String? = null
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // Health, Productivity, Mindfulness, Custom
    val frequencyType: String, // DAILY, SPECIFIC_DAYS, TIMES_PER_WEEK
    val frequencyValue: String, // comma-separated days (1=Mon..7=Sun) or a number like "3"
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateStr: String // "YYYY-MM-DD"
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1, // Single row
    val completedOnboarding: Boolean = false,
    val goals: String = "", // Comma-separated goals
    val preferredReminderTimes: String = "" // Comma-separated times e.g. "08:00,20:00"
)
