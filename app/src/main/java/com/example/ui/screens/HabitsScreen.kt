package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitsScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val habits by viewModel.habits.collectAsState()
    val logs by viewModel.habitLogs.collectAsState()

    var showCreateForm by remember { mutableStateOf(false) }

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Habit Sanctuary",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "Build positive rituals and daily streaks",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    )
                }
                IconButton(
                    onClick = { showCreateForm = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Habit")
                }
            }

            // Section 1: Habit Streak History Calendar Heatmap
            Text(
                text = "Consistency Heatmap",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            HabitCalendarHeatmap(habits = habits, logs = logs)

            // Section 2: Habit Checklist grouped by Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Checklist",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Check off done tasks",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                )
            }

            if (habits.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🌱", fontSize = 32.sp)
                        Text(
                            text = "Create your first habit",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Define daily practices, set weekly targets, or schedule specific days of the week to stay structured.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showCreateForm = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Add Habit")
                        }
                    }
                }
            } else {
                val categories = listOf("Mindfulness", "Health", "Productivity", "Custom")
                categories.forEach { cat ->
                    val catHabits = habits.filter { it.category == cat }
                    if (catHabits.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = cat.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                ),
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                            )

                            catHabits.forEach { habit ->
                                val isCompleted = logs.any { it.habitId == habit.id && it.dateStr == todayStr }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
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
                                                onClick = { viewModel.toggleHabitCompletion(habit, todayStr) }
                                            ) {
                                                Icon(
                                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                                                    contentDescription = "Toggle completion",
                                                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(28.dp)
                                                )
                                            }

                                            Column {
                                                Text(
                                                    text = habit.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                                val freqLabel = when (habit.frequencyType) {
                                                    "DAILY" -> "Every day"
                                                    "SPECIFIC_DAYS" -> "Days: " + habit.frequencyValue.split(",").joinToString { getDayName(it.toInt()) }
                                                    "TIMES_PER_WEEK" -> "${habit.frequencyValue} times a week"
                                                    else -> "Daily"
                                                }
                                                Text(
                                                    text = freqLabel,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                    )
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Streak fire indicator
                                            if (habit.currentStreak > 0) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier
                                                        .background(
                                                            MaterialTheme.colorScheme.tertiaryContainer,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("🔥", fontSize = 12.sp)
                                                    Text(
                                                        text = "${habit.currentStreak}d",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                                        )
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteHabit(habit) },
                                                colors = IconButtonDefaults.iconButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete Habit"
                                                )
                                            }
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

        // Create Habit Dialog/Form
        if (showCreateForm) {
            CreateHabitDialog(
                onDismiss = { showCreateForm = false },
                onSave = { name, category, freqType, freqValue ->
                    viewModel.createHabit(name, category, freqType, freqValue)
                    showCreateForm = false
                }
            )
        }
    }
}

@Composable
fun HabitCalendarHeatmap(habits: List<Habit>, logs: List<com.example.data.HabitLog>) {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Generate grid for the last 28 days (4 weeks)
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -27) // Back 27 days + today = 28 days

    // Map logs to date strings
    val logsGroupedByDate = logs.groupBy { it.dateStr }

    val days = remember(logs, habits) {
        (0..27).map { _ ->
            val dateStr = df.format(cal.time)
            val dayNum = cal.get(Calendar.DAY_OF_MONTH)
            val dateLogs = logsGroupedByDate[dateStr] ?: emptyList()
            
            // Calculate completion percentage
            // For general heatmap, completion percent is: completed logs / total active habits
            val activeHabitsCount = habits.size
            val completionPct = if (activeHabitsCount > 0) {
                dateLogs.size.toFloat() / activeHabitsCount
            } else {
                0f
            }
            
            cal.add(Calendar.DAY_OF_YEAR, 1)
            Pair(dayNum, completionPct)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Past 28 Days Completion Progress",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )

            // Render a grid 7 columns (days of week) by 4 rows (weeks)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (weekIndex in 0..3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (dayIndex in 0..6) {
                            val idx = weekIndex * 7 + dayIndex
                            val (dayNum, fraction) = days.getOrNull(idx) ?: Pair(1, 0f)

                            // Colour of heatmap block based on fraction
                            val blockColor = when {
                                habits.isEmpty() -> MaterialTheme.colorScheme.surfaceVariant
                                fraction == 0f -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                fraction < 0.33f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                fraction < 0.66f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(blockColor, shape = RoundedCornerShape(8.dp))
                                    .clip(RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (fraction > 0.66f) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Heatmap Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
                Spacer(modifier = Modifier.width(4.dp))
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    MaterialTheme.colorScheme.primary
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, shape = RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Spacer(modifier = Modifier.width(1.dp))
                Text("More", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, freqType: String, freqValue: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Health") }
    var freqType by remember { mutableStateOf("DAILY") }

    // Weekdays checklist state
    val selectedDays = remember { mutableStateListOf<Int>() }

    // Stepper for times per week
    var timesPerWeek by remember { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Form New Habit") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Meditate 10 mins") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Mindfulness", "Health", "Productivity", "Custom").forEach { cat ->
                            val isSel = cat == category
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { category = cat }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Frequency type selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Frequency Type", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("DAILY" to "Daily", "SPECIFIC_DAYS" to "Days", "TIMES_PER_WEEK" to "Weekly").forEach { (type, label) ->
                            val isSel = type == freqType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { freqType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Sub-selectors depending on selected frequency type
                if (freqType == "SPECIFIC_DAYS") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Select Applicable Days", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(1 to "M", 2 to "T", 3 to "W", 4 to "T", 5 to "F", 6 to "S", 7 to "S").forEach { (num, day) ->
                                val isSel = selectedDays.contains(num)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)
                                        .clickable {
                                            if (isSel) selectedDays.remove(num) else selectedDays.add(num)
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else if (freqType == "TIMES_PER_WEEK") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Times Per Week: $timesPerWeek", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        Slider(
                            value = timesPerWeek.toFloat(),
                            onValueChange = { timesPerWeek = it.toInt() },
                            valueRange = 1f..7f,
                            steps = 5
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        val value = when (freqType) {
                            "DAILY" -> ""
                            "SPECIFIC_DAYS" -> selectedDays.sorted().joinToString(",")
                            "TIMES_PER_WEEK" -> timesPerWeek.toString()
                            else -> ""
                        }
                        onSave(name, category, freqType, value)
                    }
                },
                enabled = name.isNotEmpty() && (freqType != "SPECIFIC_DAYS" || selectedDays.isNotEmpty()),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Lock Habit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getDayName(dayNum: Int): String {
    return when (dayNum) {
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        7 -> "Sun"
        else -> ""
    }
}
