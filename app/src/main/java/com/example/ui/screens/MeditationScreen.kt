package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MeditationScreen(viewModel: MainViewModel) {
    val scrollState = rememberScrollState()
    val activeMeditation by viewModel.activeMeditation.collectAsState()
    val logs by viewModel.meditationLogs.collectAsState()
    val stats by viewModel.meditationStats.collectAsState()

    var selectedMinutes by remember { mutableStateOf(5) }
    var selectedSound by remember { mutableStateOf("Rain") }
    val categories = listOf(
        "Sleep" to "🌙 Deep drift & slumber",
        "Stress" to "🍃 Grounding & relief",
        "Focus" to "💡 Clarity & presence",
        "Anxiety" to "🌬️ Breath release & ease"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (activeMeditation != null) {
            ActiveMeditationView(
                viewModel = viewModel,
                activeState = activeMeditation!!
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Calm & Stillness",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Breathe, slow down, and stay present",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Zen",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                // Stats Dashboard Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🔥 ${stats.currentStreak} Days",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Text(
                                text = "Meditation Streak",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "⏱️ ${stats.totalMinutes} Mins",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                            Text(
                                text = "Total Duration",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                // Guided Categories
                Text(
                    text = "Guided Journeys",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.take(2).forEach { (cat, desc) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.startMeditation(cat, selectedMinutes, selectedSound) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(cat, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.takeLast(2).forEach { (cat, desc) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.startMeditation(cat, selectedMinutes, selectedSound) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(cat, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
                            }
                        }
                    }
                }

                // Simple Timer Builder Card
                Text(
                    text = "Silent Timer Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Duration Selectors
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Timer Duration (Minutes)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1, 3, 5, 10, 15).forEach { min ->
                                    val isSelected = min == selectedMinutes
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { selectedMinutes = min }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${min}m",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Ambient Sound selectors
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Ambient Background Atmosphere", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Rain", "Ocean Waves", "White Noise", "None").forEach { snd ->
                                    val isSelected = snd == selectedSound
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { selectedSound = snd }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = snd,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                            ),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.startMeditation("Timer Mode", selectedMinutes, selectedSound) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_meditation_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                                Text("Begin Meditation Session", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                // Section: Session Logs
                Text(
                    text = "Past Sessions",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )

                if (logs.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🧘🏽‍♀️", fontSize = 28.sp)
                            Text("No mindfulness sessions completed", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "Choose a focus journey or set a timer above to ground yourself.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        logs.forEach { log ->
                            val readableDate = SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault()).format(Date(log.date))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(log.category, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                        Text(
                                            readableDate,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(imageVector = Icons.Default.Timer, contentDescription = "Duration", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "${log.durationSeconds / 60} min",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ActiveMeditationView(
    viewModel: MainViewModel,
    activeState: com.example.viewmodel.ActiveMeditationState
) {
    // 1. Infinite Transition for Breathing Visualizer Circle pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    // Animate scale size: 4 seconds inhale (expansion), 2 seconds hold, 4 seconds exhale (contraction), 2 seconds hold
    // Let's approximate with a smooth sinusoidal pulse
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteTransitionSpec(),
        label = "pulse_scale"
    )

    // Compute breath prompt text based on the scale fraction
    val breathPrompt = when {
        scale < 0.55f -> "Inhale..."
        scale > 0.88f -> "Hold..."
        else -> "Exhale..."
    }

    // Sound wave graphic animation
    val waveHeightMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteTransitionSpec(durationMillis = 1500),
        label = "sound_wave"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header Info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = activeState.category.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
            )
            Text(
                text = "Guided Sanctuary",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            )
        }

        // Center Breathing Visualizer Circle & Timer Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(36.dp),
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.weight(0.4f))

            // Animated Breathing circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Large soft pulsing outer circle
                Box(
                    modifier = Modifier
                        .fillMaxSize(scale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Inner core circle
                Box(
                    modifier = Modifier
                        .fillMaxSize(scale * 0.7f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = breathPrompt,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Real-Time Countdown Timer Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val mins = activeState.secondsRemaining / 60
                val secs = activeState.secondsRemaining % 60
                Text(
                    text = String.format("%02d:%02d", mins, secs),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                if (activeState.ambientSound != null && activeState.ambientSound != "None") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Sound track",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Sound: ${activeState.ambientSound}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )

                        // Draw simple animated sound waves in Canvas
                        Canvas(modifier = Modifier.size(32.dp, 16.dp)) {
                            val w = size.width
                            val h = size.height
                            val spacing = w / 4
                            for (i in 0..3) {
                                val waveH = (h * waveHeightMultiplier * (0.5f + i * 0.15f)).coerceAtMost(h)
                                drawLine(
                                    color = Color(0xFF43657C), // M3 blue secondary
                                    start = Offset(i * spacing + spacing / 2, h / 2 - waveH / 2),
                                    end = Offset(i * spacing + spacing / 2, h / 2 + waveH / 2),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom action controls: Pause & Discard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.cancelMeditation() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("End Session")
            }

            Button(
                onClick = { viewModel.toggleMeditationTimer() },
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("meditation_pause_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeState.isRunning) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (activeState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Control"
                    )
                    Text(if (activeState.isRunning) "Pause Timer" else "Resume Timer")
                }
            }
        }
    }
}

// Custom breathing animation sequence (breathe in / hold / breathe out)
@Composable
fun infiniteTransitionSpec(durationMillis: Int = 4000): InfiniteRepeatableSpec<Float> {
    return infiniteRepeatable(
        animation = tween(
            durationMillis = durationMillis,
            easing = LinearEasing
        ),
        repeatMode = RepeatMode.Reverse
    )
}
