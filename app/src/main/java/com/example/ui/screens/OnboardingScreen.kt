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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Composable
fun OnboardingScreen(
    onComplete: (goals: List<String>, reminderTimes: List<String>) -> Unit
) {
    val scrollState = rememberScrollState()

    val availableGoals = listOf(
        "Build Strength" to "Track custom gym workouts & see trends",
        "Calm & Mindfulness" to "Guided meditations with ambient tracks",
        "Healthy Habits" to "Build streaks with flexible checklists",
        "Better Sleep" to "Improve rest with wind-down sessions",
        "Productivity Flow" to "Organize routines and daily completions"
    )

    val reminderSlots = listOf(
        "Morning (08:00 AM)",
        "Mid-day (01:00 PM)",
        "Evening (06:00 PM)",
        "Night (09:00 PM)"
    )

    val selectedGoals = remember { mutableStateListOf<String>() }
    val selectedTimes = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Hero Logo or Icon
        Image(
            painter = painterResource(id = R.drawable.ic_brand_logo),
            contentDescription = "ZenFit Logo",
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to ZenFit",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Your personal sanctuary for body, mind, and habits.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Step 1: Goals
        Text(
            text = "What are your wellness goals?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Select all that apply",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        availableGoals.forEach { (goal, desc) ->
            val isSelected = selectedGoals.contains(goal)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        if (isSelected) selectedGoals.remove(goal) else selectedGoals.add(goal)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = "Selection State",
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goal,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Step 2: Reminders
        Text(
            text = "When would you like reminders?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Keep your habits and workouts consistent",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flexible grid for reminder slots
            Column(modifier = Modifier.weight(1f)) {
                reminderSlots.take(2).forEach { slot ->
                    val isSelected = selectedTimes.contains(slot)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedTimes.remove(slot) else selectedTimes.add(slot)
                        },
                        label = { Text(slot) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                reminderSlots.takeLast(2).forEach { slot ->
                    val isSelected = selectedTimes.contains(slot)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedTimes.remove(slot) else selectedTimes.add(slot)
                        },
                        label = { Text(slot) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                onComplete(selectedGoals.toList(), selectedTimes.toList())
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("get_started_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = selectedGoals.isNotEmpty()
        ) {
            Text(
                text = "Continue to Sanctuary",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
