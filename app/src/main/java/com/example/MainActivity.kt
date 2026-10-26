package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import com.example.auth.AuthManager
import com.example.data.repository.FitnessDataRepository
import com.example.data.repository.SocialCompetitionRepository
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.GoalProgressScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WatchSyncScreen
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.StepSyncTheme
import kotlinx.coroutines.launch

// Health Connect Step Count Permissions Boilerplate
val HEALTH_PERMISSIONS: Set<String> = setOf(
    HealthPermission.getReadPermission(StepsRecord::class),
    HealthPermission.getWritePermission(StepsRecord::class)
)

enum class Screen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.DirectionsRun),
    GOAL_PROGRESS("Goal", Icons.Default.Adjust),
    CHALLENGES("Challenges", Icons.Default.SportsScore),
    LEADERBOARD("Board", Icons.Default.EmojiEvents),
    FRIENDS("Friends", Icons.Default.Group),
    WATCH_SYNC("Watch", Icons.Default.Watch),
    PROFILE("Profile", Icons.Default.Person),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    // Health Connect client reference
    val healthConnectClient: HealthConnectClient? by lazy {
        try {
            HealthConnectClient.getOrCreate(applicationContext)
        } catch (_: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            com.example.auth.FirebaseHelper.initialize(applicationContext)
        } catch (_: Exception) {}
        enableEdgeToEdge()

        val fitnessRepository = FitnessDataRepository(applicationContext)
        val competitionRepository = SocialCompetitionRepository(applicationContext)
        val authManager = AuthManager(applicationContext)
        val healthConnectSyncService = com.example.health.HealthConnectSyncService(applicationContext, fitnessRepository, competitionRepository)

        // Launch periodic background sync
        healthConnectSyncService.startBackgroundSync()

        setContent {
            val scope = rememberCoroutineScope()
            var isDarkTheme by remember { mutableStateOf(true) }
            var isOnboardingCompleted by remember { mutableStateOf(true) }

            // Health Connect Permissions Contract Launcher
            val requestPermissionsLauncher = rememberLauncherForActivityResult(
                contract = PermissionController.createRequestPermissionResultContract()
            ) { grantedPermissions ->
                if (grantedPermissions.contains(HealthPermission.getReadPermission(StepsRecord::class))) {
                    Toast.makeText(this@MainActivity, "Health Connect permissions granted! Syncing steps...", Toast.LENGTH_SHORT).show()
                    scope.launch {
                        healthConnectSyncService.syncUserActivity()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Health Connect permission denied. Using demo / manual mode.", Toast.LENGTH_SHORT).show()
                }
            }

            // Trigger function to check and request Health Connect permissions
            fun requestHealthConnectPermissions() {
                scope.launch {
                    val availability = healthConnectSyncService.checkSdkAvailability()
                    if (availability != com.example.health.HealthConnectManager.AvailabilityStatus.AVAILABLE) {
                        Toast.makeText(this@MainActivity, "Health Connect is not available on this device/emulator. Demo Mode active.", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    if (healthConnectSyncService.hasRequiredPermissions()) {
                        val ok = healthConnectSyncService.syncUserActivity()
                        Toast.makeText(this@MainActivity, if (ok) "Health Connect steps synced!" else "Health Connect sync completed.", Toast.LENGTH_SHORT).show()
                    } else {
                        requestPermissionsLauncher.launch(healthConnectSyncService.requiredPermissions)
                    }
                }
            }

            StepSyncTheme(darkTheme = isDarkTheme) {
                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onComplete = {
                            isOnboardingCompleted = true
                            requestHealthConnectPermissions()
                        }
                    )
                } else {
                    StepSyncMainApp(
                        fitnessRepository = fitnessRepository,
                        competitionRepository = competitionRepository,
                        authManager = authManager,
                        healthConnectSyncService = healthConnectSyncService,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { isDarkTheme = it },
                        onRequestHealthPermissions = { requestHealthConnectPermissions() }
                    )
                }
            }
        }
    }
}

@Composable
fun StepSyncMainApp(
    fitnessRepository: FitnessDataRepository,
    competitionRepository: SocialCompetitionRepository,
    authManager: AuthManager,
    healthConnectSyncService: com.example.health.HealthConnectSyncService? = null,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onRequestHealthPermissions: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }

    // Support device back button to return to Home from sub-screens
    if (currentScreen != Screen.HOME) {
        BackHandler {
            currentScreen = Screen.HOME
        }
    }

    val bottomNavScreens = listOf(
        Screen.HOME,
        Screen.GOAL_PROGRESS,
        Screen.CHALLENGES,
        Screen.LEADERBOARD,
        Screen.WATCH_SYNC
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                bottomNavScreens.forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBg,
                            selectedTextColor = CyberLime,
                            indicatorColor = CyberLime,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = DarkBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { targetScreen ->
            when (targetScreen) {
                Screen.HOME -> HomeScreen(
                    fitnessRepository = fitnessRepository,
                    competitionRepository = competitionRepository,
                    healthConnectSyncService = healthConnectSyncService,
                    onNavigateToChallenges = { currentScreen = Screen.CHALLENGES },
                    onNavigateToWatchSync = { currentScreen = Screen.WATCH_SYNC },
                    onNavigateToProfile = { currentScreen = Screen.PROFILE },
                    onNavigateToGoalProgress = { currentScreen = Screen.GOAL_PROGRESS },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                )
                Screen.GOAL_PROGRESS -> GoalProgressScreen(
                    fitnessRepository = fitnessRepository,
                    competitionRepository = competitionRepository,
                    healthConnectSyncService = healthConnectSyncService,
                    onNavigateBack = { currentScreen = Screen.HOME },
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                )
                Screen.CHALLENGES -> ChallengesScreen(
                    competitionRepository = competitionRepository
                )
                Screen.LEADERBOARD -> LeaderboardScreen(
                    fitnessRepository = fitnessRepository,
                    competitionRepository = competitionRepository
                )
                Screen.FRIENDS -> FriendsScreen(
                    competitionRepository = competitionRepository
                )
                Screen.WATCH_SYNC -> WatchSyncScreen(
                    fitnessRepository = fitnessRepository,
                    competitionRepository = competitionRepository,
                    healthConnectSyncService = healthConnectSyncService
                )
                Screen.PROFILE -> ProfileScreen(
                    competitionRepository = competitionRepository,
                    fitnessRepository = fitnessRepository,
                    authManager = authManager,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                )
                Screen.SETTINGS -> SettingsScreen(
                    fitnessRepository = fitnessRepository,
                    healthConnectSyncService = healthConnectSyncService,
                    onNavigateBack = { currentScreen = Screen.HOME }
                )
            }
        }
    }
}
