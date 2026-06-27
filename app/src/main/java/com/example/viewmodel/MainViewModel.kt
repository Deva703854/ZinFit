package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MainRepository
    private val context = application.applicationContext

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MainRepository(database)
        
        viewModelScope.launch {
            repository.populateDefaultTemplatesIfNeeded()
        }
    }

    // Navigation & Flow
    private val _currentTab = MutableStateFlow("Home")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    // User Settings & Onboarding
    val userSettings: StateFlow<UserSettings?> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun completeOnboarding(goals: List<String>, preferredReminderTimes: List<String>) {
        viewModelScope.launch {
            val settings = UserSettings(
                completedOnboarding = true,
                goals = goals.joinToString(","),
                preferredReminderTimes = preferredReminderTimes.joinToString(",")
            )
            repository.updateUserSettings(settings)
        }
    }

    // Workouts State
    val workoutSessions: StateFlow<List<WorkoutSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutTemplates: StateFlow<List<WorkoutTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Workout Tracker
    private val _activeWorkout = MutableStateFlow<ActiveWorkoutState?>(null)
    val activeWorkout: StateFlow<ActiveWorkoutState?> = _activeWorkout.asStateFlow()

    private var workoutTimerJob: Job? = null

    fun startWorkoutFromTemplate(template: WorkoutTemplate) {
        _activeWorkout.value = ActiveWorkoutState(
            templateName = template.name,
            category = template.category,
            exercises = template.exercises.map { it.copy(completed = false) },
            elapsedSeconds = 0
        )
        startWorkoutTimer()
    }

    fun startCustomWorkout(name: String, category: String) {
        _activeWorkout.value = ActiveWorkoutState(
            templateName = name.ifEmpty { "Custom Workout" },
            category = category.ifEmpty { "Strength" },
            exercises = emptyList(),
            elapsedSeconds = 0
        )
        startWorkoutTimer()
    }

    fun addExerciseToActiveWorkout(name: String, category: String, sets: Int, reps: Int, weight: Float) {
        val current = _activeWorkout.value ?: return
        val updatedExercises = current.exercises + WorkoutExercise(name, category, sets, reps, weight)
        _activeWorkout.value = current.copy(exercises = updatedExercises)
    }

    fun toggleExerciseSetCompleted(exerciseIndex: Int) {
        val current = _activeWorkout.value ?: return
        val updatedExercises = current.exercises.toMutableList()
        val exercise = updatedExercises[exerciseIndex]
        updatedExercises[exerciseIndex] = exercise.copy(completed = !exercise.completed)
        _activeWorkout.value = current.copy(exercises = updatedExercises)
        
        // Trigger rest timer on completing an exercise / set
        if (updatedExercises[exerciseIndex].completed) {
            startRestTimer(60) // default 60s rest
        }
    }

    fun updateExerciseDetails(exerciseIndex: Int, sets: Int, reps: Int, weight: Float) {
        val current = _activeWorkout.value ?: return
        val updatedExercises = current.exercises.toMutableList()
        val exercise = updatedExercises[exerciseIndex]
        updatedExercises[exerciseIndex] = exercise.copy(sets = sets, reps = reps, weightKg = weight)
        _activeWorkout.value = current.copy(exercises = updatedExercises)
    }

    fun saveActiveWorkout() {
        val current = _activeWorkout.value ?: return
        viewModelScope.launch {
            val session = WorkoutSession(
                date = System.currentTimeMillis(),
                name = current.templateName,
                exercises = current.exercises,
                durationSeconds = current.elapsedSeconds
            )
            repository.insertSession(session)
            cancelWorkoutAndTimer()
        }
    }

    fun cancelActiveWorkout() {
        cancelWorkoutAndTimer()
    }

    private fun startWorkoutTimer() {
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeWorkout.value ?: break
                _activeWorkout.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
            }
        }
    }

    private fun cancelWorkoutAndTimer() {
        workoutTimerJob?.cancel()
        workoutTimerJob = null
        _activeWorkout.value = null
        _restTimerSeconds.value = 0
        restTimerJob?.cancel()
        restTimerJob = null
    }

    // Rest Timer
    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()
    private var restTimerJob: Job? = null

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _restTimerSeconds.value = seconds
        restTimerJob = viewModelScope.launch {
            while (_restTimerSeconds.value > 0) {
                delay(1000)
                _restTimerSeconds.value -= 1
            }
            triggerVibration()
        }
    }

    fun cancelRestTimer() {
        restTimerJob?.cancel()
        restTimerJob = null
        _restTimerSeconds.value = 0
    }

    private fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Meditation State
    val meditationLogs: StateFlow<List<MeditationLog>> = repository.allMeditationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeMeditation = MutableStateFlow<ActiveMeditationState?>(null)
    val activeMeditation: StateFlow<ActiveMeditationState?> = _activeMeditation.asStateFlow()

    private var meditationTimerJob: Job? = null

    fun startMeditation(category: String, durationMinutes: Int, ambientSound: String?) {
        _activeMeditation.value = ActiveMeditationState(
            category = category,
            durationSeconds = durationMinutes * 60,
            secondsRemaining = durationMinutes * 60,
            ambientSound = ambientSound,
            isRunning = true
        )
        startMeditationTimer()
    }

    fun toggleMeditationTimer() {
        val current = _activeMeditation.value ?: return
        if (current.isRunning) {
            meditationTimerJob?.cancel()
            _activeMeditation.value = current.copy(isRunning = false)
        } else {
            _activeMeditation.value = current.copy(isRunning = true)
            startMeditationTimer()
        }
    }

    fun cancelMeditation() {
        meditationTimerJob?.cancel()
        meditationTimerJob = null
        _activeMeditation.value = null
    }

    private fun startMeditationTimer() {
        meditationTimerJob?.cancel()
        meditationTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeMeditation.value ?: break
                if (current.isRunning) {
                    if (current.secondsRemaining > 1) {
                        _activeMeditation.value = current.copy(secondsRemaining = current.secondsRemaining - 1)
                    } else {
                        // Completed!
                        saveMeditationLog(
                            MeditationLog(
                                date = System.currentTimeMillis(),
                                category = current.category,
                                durationSeconds = current.durationSeconds,
                                ambientSound = current.ambientSound
                            )
                        )
                        _activeMeditation.value = null
                        triggerVibration()
                        break
                    }
                }
            }
        }
    }

    private fun saveMeditationLog(log: MeditationLog) {
        viewModelScope.launch {
            repository.insertMeditationLog(log)
        }
    }

    // Meditation Analytics
    val meditationStats: StateFlow<MeditationStats> = meditationLogs.map { logs ->
        val totalMinutes = logs.sumOf { it.durationSeconds } / 60
        val streak = calculateMeditationStreak(logs)
        MeditationStats(totalMinutes, streak)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MeditationStats(0, 0))

    private fun calculateMeditationStreak(logs: List<MeditationLog>): Int {
        if (logs.isEmpty()) return 0
        val dates = logs.map {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.date
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            df.format(cal.time)
        }.distinct().sortedDescending()

        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = df.format(Date())
        val yesterdayStr = df.format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))

        if (dates.first() != todayStr && dates.first() != yesterdayStr) {
            return 0
        }

        var streak = 0
        var currentCheckDate = df.parse(dates.first()) ?: return 0

        for (dateStr in dates) {
            val logDate = df.parse(dateStr) ?: break
            val diffMs = currentCheckDate.time - logDate.time
            val diffDays = diffMs / (24 * 60 * 60 * 1000)

            if (diffDays == 0L) {
                streak++
                // Set check date to previous day
                currentCheckDate = Date(currentCheckDate.time - 24 * 60 * 60 * 1000)
            } else if (diffDays == 1L) {
                streak++
                currentCheckDate = Date(logDate.time - 24 * 60 * 60 * 1000)
            } else {
                break
            }
        }
        return streak
    }

    // Habits State
    val habits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitLogs: StateFlow<List<HabitLog>> = repository.allHabitLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createHabit(name: String, category: String, frequencyType: String, frequencyValue: String) {
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name,
                    category = category,
                    frequencyType = frequencyType,
                    frequencyValue = frequencyValue
                )
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabitCompletion(habit: Habit, dateStr: String) {
        viewModelScope.launch {
            val logs = repository.allHabitLogs.first()
            val isCompleted = logs.any { it.habitId == habit.id && it.dateStr == dateStr }

            if (isCompleted) {
                repository.removeHabitCompletion(habit.id, dateStr)
            } else {
                repository.logHabitCompletion(habit.id, dateStr)
            }

            // Recalculate streak stats for this habit after logging completion
            delay(100) // Small wait for db update
            val updatedLogs = repository.allHabitLogs.first().filter { it.habitId == habit.id }
            val (currentStreak, longestStreak) = calculateHabitStreakStats(updatedLogs)
            repository.updateHabit(habit.copy(currentStreak = currentStreak, longestStreak = longestStreak))
        }
    }

    private fun calculateHabitStreakStats(logs: List<HabitLog>): Pair<Int, Int> {
        if (logs.isEmpty()) return Pair(0, 0)
        
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = logs.mapNotNull {
            try { df.parse(it.dateStr) } catch (e: Exception) { null }
        }.distinct().sorted() // Ascending order

        if (dates.isEmpty()) return Pair(0, 0)

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        var previousDate: Date? = null

        for (date in dates) {
            if (previousDate == null) {
                tempStreak = 1
            } else {
                val diffMs = date.time - previousDate.time
                val diffDays = diffMs / (24 * 60 * 60 * 1000)

                if (diffDays <= 1L) {
                    tempStreak++
                } else {
                    if (tempStreak > longestStreak) {
                        longestStreak = tempStreak
                    }
                    tempStreak = 1
                }
            }
            previousDate = date
        }
        if (tempStreak > longestStreak) {
            longestStreak = tempStreak
        }

        // Calculate current streak
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        yesterday.set(Calendar.HOUR_OF_DAY, 0)
        yesterday.set(Calendar.MINUTE, 0)
        yesterday.set(Calendar.SECOND, 0)
        yesterday.set(Calendar.MILLISECOND, 0)

        val lastLogDate = dates.last()
        val diffToday = (today.time.time - lastLogDate.time) / (24 * 60 * 60 * 1000)

        currentStreak = if (diffToday <= 1L) {
            // Find continuous backward streak from lastLogDate
            var backwardStreak = 0
            var checkDate = lastLogDate
            var i = dates.size - 1
            
            while (i >= 0) {
                val d = dates[i]
                val diffCheck = (checkDate.time - d.time) / (24 * 60 * 60 * 1000)
                if (diffCheck == 0L) {
                    backwardStreak++
                    checkDate = Date(checkDate.time - 24 * 60 * 60 * 1000)
                } else if (diffCheck == 1L) {
                    backwardStreak++
                    checkDate = Date(d.time - 24 * 60 * 60 * 1000)
                } else if (diffCheck > 1L) {
                    break
                }
                i--
            }
            backwardStreak
        } else {
            0
        }

        return Pair(currentStreak, maxOf(longestStreak, currentStreak))
    }

    // Analytics: Workouts Progress
    val exerciseProgressData: StateFlow<Map<String, List<ExerciseProgressPoint>>> = workoutSessions.map { sessions ->
        val progressMap = mutableMapOf<String, MutableList<ExerciseProgressPoint>>()
        
        sessions.sortedBy { it.date }.forEach { session ->
            session.exercises.forEach { exercise ->
                if (exercise.completed || exercise.weightKg > 0) {
                    val points = progressMap.getOrPut(exercise.name) { mutableListOf() }
                    points.add(ExerciseProgressPoint(session.date, exercise.weightKg))
                }
            }
        }
        progressMap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<Nothing>().associateBy { "" })
}

data class ActiveWorkoutState(
    val templateName: String,
    val category: String,
    val exercises: List<WorkoutExercise>,
    val elapsedSeconds: Int
)

data class ActiveMeditationState(
    val category: String,
    val durationSeconds: Int,
    val secondsRemaining: Int,
    val ambientSound: String?,
    val isRunning: Boolean
)

data class MeditationStats(
    val totalMinutes: Int,
    val currentStreak: Int
)

data class ExerciseProgressPoint(
    val timestamp: Long,
    val weightKg: Float
)
