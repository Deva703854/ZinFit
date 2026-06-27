package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToTab: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val habits by viewModel.habits.collectAsState()
    val habitLogs by viewModel.habitLogs.collectAsState()
    val workoutSessions by viewModel.workoutSessions.collectAsState()
    val meditationStats by viewModel.meditationStats.collectAsState()

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayReadable = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())

    // Calculations for status
    val workoutCompletedToday = workoutSessions.any {
        val sessionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.date))
        sessionDate == todayStr
    }

    // Filter habits due today
    val calendar = Calendar.getInstance()
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon ... 7=Sat
    // Convert to 1=Mon, 7=Sun
    val isoDayOfWeek = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1

    val todayHabits = habits.filter { habit ->
        when (habit.frequencyType) {
            "DAILY" -> true
            "SPECIFIC_DAYS" -> habit.frequencyValue.split(",").contains(isoDayOfWeek.toString())
            "TIMES_PER_WEEK" -> true // Show on home if we want to check it off
            else -> true
        }
    }

    val completedHabitsCount = todayHabits.count { habit ->
        habitLogs.any { log -> log.habitId == habit.id && log.dateStr == todayStr }
    }

    val totalHabitsCount = todayHabits.size
    val habitsCompletionPct = if (totalHabitsCount > 0) {
        (completedHabitsCount.toFloat() / totalHabitsCount * 100).toInt()
    } else {
        0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = todayReadable,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                )
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            Image(
                painter = painterResource(id = com.example.R.drawable.ic_brand_logo),
                contentDescription = "ZenFit Logo",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
            )
        }

        // Daily Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Today's Sanctuary Progress",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Habits progress
                    ProgressIndicatorItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Habits",
                        subtitle = if (totalHabitsCount > 0) "$completedHabitsCount/$totalHabitsCount" else "No habits",
                        progress = if (totalHabitsCount > 0) habitsCompletionPct / 100f else 0f
                    )

                    // Workouts progress
                    ProgressIndicatorItem(
                        icon = Icons.Default.FitnessCenter,
                        title = "Workout",
                        subtitle = if (workoutCompletedToday) "Completed" else "Pending",
                        progress = if (workoutCompletedToday) 1f else 0f
                    )

                    // Meditation progress
                    ProgressIndicatorItem(
                        icon = Icons.Default.SelfImprovement,
                        title = "Meditate",
                        subtitle = "${meditationStats.totalMinutes}m logged",
                        progress = if (meditationStats.currentStreak > 0) 1f else 0f
                    )
                }
            }
        }

        // Section: Today's Workout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = "Workout",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Today's Session",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    if (workoutCompletedToday) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Done ✨") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                if (workoutCompletedToday) {
                    Text(
                        text = "Great job! You completed a workout today. Rest up and refuel.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                } else {
                    Text(
                        text = "Build strength and burn energy. Kickstart today's session from pre-built templates or create your own custom set.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                    Button(
                        onClick = { onNavigateToTab("Workouts") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_start_workout_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Start Workout")
                    }
                }
            }
        }

        // Section: Guided Meditation Streak
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = "Meditation",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mindful Meditation",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Current Streak: ${meditationStats.currentStreak} Days",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = "Take a short breathing break to calm anxiety and stress.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                IconButton(
                    onClick = { onNavigateToTab("Meditate") },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Meditate Screen"
                    )
                }
            }
        }

        // Section: Today's Habit Checklist
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Habits",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Today's Checklist",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Text(
                    text = if (totalHabitsCount > 0) "$habitsCompletionPct% completed" else "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (todayHabits.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🌻", fontSize = 32.sp)
                        Text(
                            text = "No habits scheduled for today",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Create custom daily habits to structure your routines & build consistency.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            ),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { onNavigateToTab("Habits") },
                            modifier = Modifier.padding(top = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Setup Habits")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        todayHabits.forEach { habit ->
                            val isCompleted = habitLogs.any { it.habitId == habit.id && it.dateStr == todayStr }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.toggleHabitCompletion(habit, todayStr) }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                        contentDescription = "Toggle Complete",
                                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Column {
                                        Text(
                                            text = habit.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = habit.category,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                                if (habit.currentStreak > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🔥", fontSize = 14.sp)
                                        Text(
                                            text = "${habit.currentStreak}d",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ProgressIndicatorItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    progress: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(90.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                strokeWidth = 5.dp
            )
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
