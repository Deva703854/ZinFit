package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutExercise
import com.example.data.WorkoutSession
import com.example.data.WorkoutTemplate
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutsScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val activeWorkout by viewModel.activeWorkout.collectAsState()
    val templates by viewModel.workoutTemplates.collectAsState()
    val sessions by viewModel.workoutSessions.collectAsState()
    val progressData by viewModel.exerciseProgressData.collectAsState()

    // Screen-level state for custom workout builder modal/dialog
    var showBuilderDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (activeWorkout != null) {
            ActiveWorkoutView(
                viewModel = viewModel,
                activeState = activeWorkout!!
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Workouts Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Strength & Movement",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Track your routines and lift metrics",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        )
                    }
                    IconButton(
                        onClick = { showBuilderDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "New Session")
                    }
                }

                // Section 1: Past Workouts Calendar View
                WorkoutCalendarStrip(sessions = sessions)

                // Section 2: Workout Templates
                Text(
                    text = "Workout Templates",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    templates.forEach { template ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.startWorkoutFromTemplate(template) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = template.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${template.exercises.size} Exercises • ${template.category}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = template.exercises.joinToString { it.name },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        ),
                                        maxLines = 1
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // Section 3: Progress Charts (Native Canvas Drawing!)
                if (progressData.isNotEmpty()) {
                    Text(
                        text = "Weight Lifted Trends",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    WorkoutProgressChartCard(progressData = progressData)
                }

                // Section 4: History log
                Text(
                    text = "Activity Log",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 12.dp)
                )

                if (sessions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🏋️", fontSize = 28.sp)
                            Text(
                                text = "No workouts logged yet",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Launch a routine template above to track exercises, log sets completed, and build your physical trends.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        sessions.forEach { session ->
                            val readableDate = SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(session.date))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = session.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = readableDate,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "Duration",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            val mins = session.durationSeconds / 60
                                            val secs = session.durationSeconds % 60
                                            Text(
                                                text = String.format("%02d:%02d", mins, secs),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    session.exercises.forEach { exercise ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "• ${exercise.name}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "${exercise.sets} sets x ${exercise.reps} reps @ ${exercise.weightKg} kg",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            )
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

        // Custom Workout Builder Dialog
        if (showBuilderDialog) {
            CustomWorkoutBuilderDialog(
                onDismiss = { showBuilderDialog = false },
                onStart = { name, category ->
                    viewModel.startCustomWorkout(name, category)
                    showBuilderDialog = false
                }
            )
        }
    }
}

@Composable
fun WorkoutCalendarStrip(sessions: List<WorkoutSession>) {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val completedDates = sessions.map { df.format(Date(it.date)) }.toSet()

    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -6) // Last 7 days

    val daysList = remember(sessions) {
        (0..6).map { _ ->
            val date = cal.time
            val dateStr = df.format(date)
            val dayName = SimpleDateFormat("E", Locale.getDefault()).format(date)
            val dayNum = SimpleDateFormat("d", Locale.getDefault()).format(date)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            Triple(dayName, dayNum, completedDates.contains(dateStr))
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Past 7 Days",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysList.forEach { (name, num, isCompleted) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = num,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutProgressChartCard(progressData: Map<String, List<com.example.viewmodel.ExerciseProgressPoint>>) {
    val exercises = progressData.keys.toList()
    var selectedExercise by remember { mutableStateOf(exercises.firstOrNull() ?: "") }

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
                Text(
                    text = "Exercise Trends",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                // Micro Dropdown/Scroll row of exercise options
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    items(exercises) { exercise ->
                        val isSelected = exercise == selectedExercise
                        SuggestionChip(
                            onClick = { selectedExercise = exercise },
                            label = { Text(exercise, fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        )
                    }
                }
            }

            val points = progressData[selectedExercise] ?: emptyList()
            if (points.isNotEmpty()) {
                val maxWeight = points.maxOf { it.weightKg }.coerceAtLeast(10f)
                val minWeight = points.minOf { it.weightKg }.coerceAtMost(maxWeight - 10f).coerceAtLeast(0f)
                val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(vertical = 12.dp, horizontal = 12.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    // Draw grid/baseline
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, height),
                        end = Offset(width, height),
                        strokeWidth = 1.dp.toPx()
                    )

                    val stepX = width / (points.size - 1).coerceAtLeast(1)
                    val splinePath = Path()

                    points.forEachIndexed { index, pt ->
                        val x = index * stepX
                        // Normalize y coordinate: bottom is height, top is 0
                        val fraction = (pt.weightKg - minWeight) / weightRange
                        val y = height - (fraction * height)

                        if (index == 0) {
                            splinePath.moveTo(x, y)
                        } else {
                            splinePath.lineTo(x, y)
                        }

                        // Draw a small dot at each key node point
                        drawCircle(
                            color = Color(0xFF4C6A58), // Sage primary
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    // Draw spline
                    drawPath(
                        path = splinePath,
                        color = Color(0xFF4C6A58),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val df = SimpleDateFormat("MM/dd", Locale.getDefault())
                    Text(
                        text = "First log: ${df.format(Date(points.first().timestamp))}",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                    Text(
                        text = "Peak: ${maxWeight}kg",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWorkoutBuilderDialog(
    onDismiss: () -> Unit,
    onStart: (name: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Strength") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Build Custom Routine") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Workout Name") },
                    placeholder = { Text("e.g. Legs Hypertrophy") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Routine Category", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Strength", "Cardio", "Yoga", "HIIT").forEach { cat ->
                            val isSelected = cat == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStart(name, category) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start Tracking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ActiveWorkoutView(
    viewModel: MainViewModel,
    activeState: com.example.viewmodel.ActiveWorkoutState
) {
    val restTimerSeconds by viewModel.restTimerSeconds.collectAsState()

    var showAddExerciseRow by remember { mutableStateOf(false) }
    var exerciseName by remember { mutableStateOf("") }
    var exerciseCat by remember { mutableStateOf("Strength") }
    var sets by remember { mutableStateOf("3") }
    var reps by remember { mutableStateOf("10") }
    var weight by remember { mutableStateOf("50") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Workout Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LIVE SESSION",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = activeState.templateName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = "Timer", modifier = Modifier.size(16.dp))
                            val mins = activeState.elapsedSeconds / 60
                            val secs = activeState.elapsedSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Live rest timer indicator
        if (restTimerSeconds > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Timer, contentDescription = "Rest", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(
                            text = "Resting: $restTimerSeconds seconds remaining...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        )
                    }
                    TextButton(onClick = { viewModel.cancelRestTimer() }) {
                        Text("Skip", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Section: Live Exercises checklist
        Text(
            text = "Routine Checklist",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            activeState.exercises.forEachIndexed { index, exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (exercise.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleExerciseSetCompleted(index) }
                            ) {
                                Icon(
                                    imageVector = if (exercise.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Complete Exercise",
                                    tint = if (exercise.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (exercise.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "${exercise.sets} sets x ${exercise.reps} reps • ${exercise.weightKg} kg",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Inline custom builder for adding exercise mid-workout
            if (showAddExerciseRow) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Add Exercise Set", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                        OutlinedTextField(
                            value = exerciseName,
                            onValueChange = { exerciseName = it },
                            label = { Text("Exercise Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sets,
                                onValueChange = { sets = it },
                                label = { Text("Sets") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = reps,
                                onValueChange = { reps = it },
                                label = { Text("Reps") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text("Wt (kg)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddExerciseRow = false }) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (exerciseName.isNotEmpty()) {
                                        viewModel.addExerciseToActiveWorkout(
                                            name = exerciseName,
                                            category = exerciseCat,
                                            sets = sets.toIntOrNull() ?: 3,
                                            reps = reps.toIntOrNull() ?: 10,
                                            weight = weight.toFloatOrNull() ?: 0f
                                        )
                                        exerciseName = ""
                                        showAddExerciseRow = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }
            } else {
                TextButton(
                    onClick = { showAddExerciseRow = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise")
                        Text("Add Exercise")
                    }
                }
            }
        }

        // Bottom Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.cancelActiveWorkout() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Discard")
            }

            Button(
                onClick = { viewModel.saveActiveWorkout() },
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("save_workout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Finish Session")
            }
        }
    }
}
