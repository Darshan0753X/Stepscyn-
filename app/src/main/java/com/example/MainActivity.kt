package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.data.repository.FitnessDataRepository
import com.example.data.repository.SocialCompetitionRepository
import com.example.ui.screens.ChallengesScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WatchSyncScreen
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.StepSyncTheme

enum class Screen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.DirectionsRun),
    CHALLENGES("Challenges", Icons.Default.SportsScore),
    LEADERBOARD("Board", Icons.Default.EmojiEvents),
    FRIENDS("Friends", Icons.Default.Group),
    WATCH_SYNC("Watch", Icons.Default.Watch),
    PROFILE("Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val fitnessRepository = FitnessDataRepository(applicationContext)
        val competitionRepository = SocialCompetitionRepository(applicationContext)
        val authManager = AuthManager(applicationContext)

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var isOnboardingCompleted by remember { mutableStateOf(true) } // Ready on launch for instant preview

            StepSyncTheme(darkTheme = isDarkTheme) {
                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onComplete = { isOnboardingCompleted = true }
                    )
                } else {
                    StepSyncMainApp(
                        fitnessRepository = fitnessRepository,
                        competitionRepository = competitionRepository,
                        authManager = authManager,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { isDarkTheme = it }
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
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit
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
        Screen.CHALLENGES,
        Screen.LEADERBOARD,
        Screen.FRIENDS,
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
                    onNavigateToChallenges = { currentScreen = Screen.CHALLENGES },
                    onNavigateToWatchSync = { currentScreen = Screen.WATCH_SYNC },
                    onNavigateToProfile = { currentScreen = Screen.PROFILE }
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
                    competitionRepository = competitionRepository
                )
                Screen.PROFILE -> ProfileScreen(
                    competitionRepository = competitionRepository,
                    fitnessRepository = fitnessRepository,
                    authManager = authManager,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = onToggleDarkTheme
                )
            }
        }
    }
}
