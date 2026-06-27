package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.NotificationHelper
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize notifications channel
        NotificationHelper.createNotificationChannel(this)

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel()
                val userSettings by viewModel.userSettings.collectAsState()
                
                // Permission requester for Android 13+ notifications
                val context = LocalContext.current
                var hasNotificationPermission by remember {
                    mutableStateOf(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        } else {
                            true
                        }
                    )
                }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasNotificationPermission = isGranted
                    if (isGranted) {
                        NotificationHelper.showNotification(
                            context,
                            "Welcome to ZenFit! 🌿",
                            "Your mindfulness notifications and reminders are active."
                        )
                    }
                }

                // Auto request on launch if not granted
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (userSettings == null || !userSettings!!.completedOnboarding) {
                        // Show onboarding if settings empty or not completed
                        OnboardingScreen(
                            onComplete = { goals, times ->
                                viewModel.completeOnboarding(goals, times)
                                // Trigger a welcoming local notification
                                NotificationHelper.showNotification(
                                    context,
                                    "Your Wellness Journey Begins! ✨",
                                    "ZenFit is configured with your custom goals: ${goals.joinToString { it }}."
                                )
                            }
                        )
                    } else {
                        // Main Tab Navigation Layout
                        MainNavigationFlow(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationFlow(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Tab 1: Home
                NavigationBarItem(
                    selected = currentTab == "Home",
                    onClick = { viewModel.selectTab("Home") },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == "Home") Icons.Default.Home else Icons.Default.Home,
                            contentDescription = "Home Tab"
                        )
                    },
                    label = { Text("Home") }
                )

                // Tab 2: Workouts
                NavigationBarItem(
                    selected = currentTab == "Workouts",
                    onClick = { viewModel.selectTab("Workouts") },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == "Workouts") Icons.Default.FitnessCenter else Icons.Default.FitnessCenter,
                            contentDescription = "Workouts Tab"
                        )
                    },
                    label = { Text("Workouts") }
                )

                // Tab 3: Meditate
                NavigationBarItem(
                    selected = currentTab == "Meditate",
                    onClick = { viewModel.selectTab("Meditate") },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == "Meditate") Icons.Default.SelfImprovement else Icons.Default.SelfImprovement,
                            contentDescription = "Meditate Tab"
                        )
                    },
                    label = { Text("Meditate") }
                )

                // Tab 4: Habits
                NavigationBarItem(
                    selected = currentTab == "Habits",
                    onClick = { viewModel.selectTab("Habits") },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == "Habits") Icons.Default.CheckCircle else Icons.Default.CheckCircle,
                            contentDescription = "Habits Tab"
                        )
                    },
                    label = { Text("Habits") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "Home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { tab -> viewModel.selectTab(tab) }
                )
                "Workouts" -> WorkoutsScreen(viewModel = viewModel)
                "Meditate" -> MeditationScreen(viewModel = viewModel)
                "Habits" -> HabitsScreen(viewModel = viewModel)
            }
        }
    }
}
