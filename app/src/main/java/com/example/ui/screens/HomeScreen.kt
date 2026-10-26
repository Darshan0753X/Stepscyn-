package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FitnessDataRepository
import com.example.data.repository.SocialCompetitionRepository
import com.example.ui.components.ActivityBarChart
import com.example.ui.components.CircularStepRing
import com.example.ui.components.HeadToHeadCard
import com.example.ui.components.WatchStatusCard
import com.example.ui.components.WeeklyStepBarChart
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricTeal
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    fitnessRepository: FitnessDataRepository,
    competitionRepository: SocialCompetitionRepository,
    healthConnectSyncService: com.example.health.HealthConnectSyncService? = null,
    onNavigateToChallenges: () -> Unit,
    onNavigateToWatchSync: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGoalProgress: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val todayRecord by fitnessRepository.todayRecord.collectAsState()
    val recentDays by fitnessRepository.recentDays.collectAsState()
    val syncStatus by fitnessRepository.syncStatus.collectAsState()
    val isDemoMode by fitnessRepository.isDemoMode.collectAsState()
    val userProfile by competitionRepository.currentUserProfile.collectAsState()
    val challenges by competitionRepository.challenges.collectAsState()
    val rival = competitionRepository.friends.value.firstOrNull()
    val syncState by (healthConnectSyncService?.syncState?.collectAsState() ?: remember { mutableStateOf(null) })

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault()))
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good evening, ${userProfile.displayName}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DarkSurfaceElevated, CircleShape)
                                .border(1.dp, DarkBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = CyberLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToProfile,
                        modifier = Modifier.testTag("home_profile_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CyberLime.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, CyberLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = CyberLime,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBg
                )
            )
        },
        containerColor = DarkBg,
        modifier = modifier.testTag("home_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Smartwatch Status Banner
            WatchStatusCard(
                syncStatus = syncStatus,
                onSyncClicked = {
                    scope.launch {
                        val success = if (healthConnectSyncService != null) {
                            healthConnectSyncService.syncUserActivity()
                        } else {
                            fitnessRepository.syncFromHealthConnect()
                        }
                        if (success) {
                            snackbarHostState.showSnackbar("Steps synced successfully from Health Connect!")
                        } else {
                            snackbarHostState.showSnackbar("Health Connect sync checked. Using ${syncStatus.source.name} records.")
                        }
                    }
                },
                onManageWatchClicked = onNavigateToWatchSync
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Main Hero Circular Progress Ring (Tappable to view detailed progress indicators)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onNavigateToGoalProgress() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularStepRing(
                    currentSteps = todayRecord.stepCount,
                    goal = todayRecord.goal,
                    streak = userProfile.currentStreak
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Direct shortcut to the new Goal & Progress screen
            Surface(
                onClick = onNavigateToGoalProgress,
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberLime.copy(alpha = 0.5f)),
                modifier = Modifier.testTag("home_goal_progress_shortcut")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Adjust,
                        contentDescription = null,
                        tint = CyberLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Goal Analysis (Circle & Linear)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyberLime,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Step Simulator Chips (Easy instant testing for preview emulator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Test: ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = false,
                    onClick = {
                        fitnessRepository.simulateStepsIncrement(250L)
                        competitionRepository.updateMyChallengeSteps(todayRecord.stepCount + 250L)
                    },
                    label = { Text("+250", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurfaceElevated,
                        labelColor = CyberLime
                    ),
                    modifier = Modifier.testTag("add_250_steps_button")
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = false,
                    onClick = {
                        fitnessRepository.simulateStepsIncrement(500L)
                        competitionRepository.updateMyChallengeSteps(todayRecord.stepCount + 500L)
                    },
                    label = { Text("+500", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurfaceElevated,
                        labelColor = CyberLime
                    ),
                    modifier = Modifier.testTag("add_500_steps_button")
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = false,
                    onClick = {
                        fitnessRepository.simulateStepsIncrement(1000L)
                        competitionRepository.updateMyChallengeSteps(todayRecord.stepCount + 1000L)
                    },
                    label = { Text("+1,000", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurfaceElevated,
                        labelColor = CyberLime
                    ),
                    modifier = Modifier.testTag("add_1000_steps_button")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1-on-1 Duel Head-to-Head Card
            if (rival != null) {
                HeadToHeadCard(
                    mySteps = todayRecord.stepCount,
                    rivalName = rival.displayName,
                    rivalSteps = rival.todaySteps,
                    goal = todayRecord.goal,
                    isDemoData = isDemoMode
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Step History (Health Connect 7-Day Chart)
            WeeklyStepBarChart(
                weeklyRecords = recentDays,
                targetGoal = todayRecord.goal,
                isSyncing = syncState is com.example.health.HealthSyncState.Syncing,
                onSyncRequested = {
                    scope.launch {
                        healthConnectSyncService?.syncUserActivity()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 24-Hour Activity Timeline
            ActivityBarChart(
                hourlySteps = todayRecord.getHourlySteps()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Weekly Consistency Row
            WeeklyConsistencySection()

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToChallenges,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberLime,
                        contentColor = DarkBg
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("create_challenge_cta_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsScore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Challenges",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToWatchSync,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricTeal
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("watch_setup_cta_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Watch,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Watch Sync",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WeeklyConsistencySection() {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val completedDays = listOf(true, true, false, true, true, true, false)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated, RoundedCornerShape(20.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEEKLY CONSISTENCY",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = CyberLime,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "5 of 7 days reached",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEachIndexed { index, day ->
                val reached = completedDays[index]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (reached) CyberLime else Color(0xFF1E2838),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (reached) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Reached",
                                tint = DarkBg,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
