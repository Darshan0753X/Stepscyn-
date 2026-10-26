package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FitnessDataRepository
import com.example.data.repository.SocialCompetitionRepository
import com.example.health.HealthConnectSyncService
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.SolarAmber
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

enum class IndicatorStyle(val label: String) {
    CIRCULAR("Circle"),
    LINEAR("Linear"),
    SPLIT("Both")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalProgressScreen(
    fitnessRepository: FitnessDataRepository,
    competitionRepository: SocialCompetitionRepository? = null,
    healthConnectSyncService: HealthConnectSyncService? = null,
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val todayRecord by fitnessRepository.todayRecord.collectAsState()
    val syncStatus by fitnessRepository.syncStatus.collectAsState()

    var indicatorStyle by remember { mutableStateOf(IndicatorStyle.CIRCULAR) }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    BackHandler {
        onNavigateBack()
    }

    val currentSteps = todayRecord.stepCount
    val dailyGoal = todayRecord.goal
    val progressRatio = if (dailyGoal > 0) (currentSteps.toFloat() / dailyGoal.toFloat()) else 0f
    val percentageInt = (progressRatio * 100f).toInt()
    val remainingSteps = (dailyGoal - currentSteps).coerceAtLeast(0L)
    val isGoalAchieved = currentSteps >= dailyGoal

    // Estimated Metrics
    val distanceKm = String.format(Locale.US, "%.2f", currentSteps * 0.000762)
    val caloriesKcal = (currentSteps * 0.04).toInt()
    val activeMinutes = (currentSteps / 100).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Goal & Progress",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = if (isGoalAchieved) "Goal Reached! 🎉" else "$percentageInt% of daily goal completed",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isGoalAchieved) MintSuccess else CyberLime
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("goal_progress_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("goal_progress_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CyberLime
                        )
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                val success = if (healthConnectSyncService != null) {
                                    healthConnectSyncService.syncUserActivity()
                                } else {
                                    fitnessRepository.syncFromHealthConnect()
                                }
                                Toast.makeText(
                                    context,
                                    if (success) "Health Connect steps synced!" else "Synced from ${syncStatus.source.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.testTag("goal_progress_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg,
        modifier = modifier.testTag("goal_progress_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Indicator Style Switcher Selector
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IndicatorStyle.values().forEach { style ->
                        val isSelected = indicatorStyle == style
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) CyberLime else Color.Transparent
                                )
                                .clickable { indicatorStyle = style }
                                .testTag("indicator_tab_${style.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = style.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) DarkBg else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Hero Progress Display Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (indicatorStyle) {
                        IndicatorStyle.CIRCULAR -> {
                            StepProgressCircle(
                                currentSteps = currentSteps,
                                goal = dailyGoal,
                                progressRatio = progressRatio,
                                isGoalAchieved = isGoalAchieved
                            )
                        }
                        IndicatorStyle.LINEAR -> {
                            StepProgressLinear(
                                currentSteps = currentSteps,
                                goal = dailyGoal,
                                progressRatio = progressRatio,
                                isGoalAchieved = isGoalAchieved
                            )
                        }
                        IndicatorStyle.SPLIT -> {
                            StepProgressCircle(
                                currentSteps = currentSteps,
                                goal = dailyGoal,
                                progressRatio = progressRatio,
                                isGoalAchieved = isGoalAchieved,
                                size = 200.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            StepProgressLinear(
                                currentSteps = currentSteps,
                                goal = dailyGoal,
                                progressRatio = progressRatio,
                                isGoalAchieved = isGoalAchieved
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Steps Remaining / Over-achievement Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isGoalAchieved) MintSuccess.copy(alpha = 0.15f) else Color(0xFF1E2838),
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                if (isGoalAchieved) MintSuccess.copy(alpha = 0.4f) else DarkBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isGoalAchieved) Icons.Default.CheckCircle else Icons.Default.Adjust,
                                    contentDescription = null,
                                    tint = if (isGoalAchieved) MintSuccess else SolarAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isGoalAchieved) {
                                        "Goal achieved! +${numberFormatter.format(currentSteps - dailyGoal)} bonus steps"
                                    } else {
                                        "${numberFormatter.format(remainingSteps)} steps left to reach goal"
                                    },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (isGoalAchieved) MintSuccess else MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Text(
                                text = "$percentageInt%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = if (isGoalAchieved) MintSuccess else CyberLime,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Statistics Grid (Distance, Calories, Active Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricItemCard(
                    title = "Distance",
                    value = "$distanceKm km",
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    tint = CyberLime,
                    modifier = Modifier.weight(1f)
                )
                MetricItemCard(
                    title = "Burned",
                    value = "$caloriesKcal kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    tint = SolarAmber,
                    modifier = Modifier.weight(1f)
                )
                MetricItemCard(
                    title = "Active Time",
                    value = "$activeMinutes min",
                    icon = Icons.Default.Timer,
                    tint = ElectricTeal,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Milestone Checkpoints Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MILESTONES",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = CyberLime,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = CyberLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    MilestoneRow(
                        label = "25% Warmup",
                        steps = (dailyGoal * 0.25).toLong(),
                        isCompleted = currentSteps >= (dailyGoal * 0.25)
                    )
                    MilestoneRow(
                        label = "50% Halfway Point",
                        steps = (dailyGoal * 0.50).toLong(),
                        isCompleted = currentSteps >= (dailyGoal * 0.50)
                    )
                    MilestoneRow(
                        label = "75% The Final Push",
                        steps = (dailyGoal * 0.75).toLong(),
                        isCompleted = currentSteps >= (dailyGoal * 0.75)
                    )
                    MilestoneRow(
                        label = "100% Goal Champion",
                        steps = dailyGoal,
                        isCompleted = currentSteps >= dailyGoal,
                        isFinal = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Goal Adjustment Controls Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ADJUST DAILY GOAL",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = CyberLime,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                val next = (dailyGoal - 500L).coerceAtLeast(1000L)
                                fitnessRepository.setDailyGoal(next)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(DarkBg, CircleShape)
                                .border(1.dp, DarkBorder, CircleShape)
                                .testTag("decrease_goal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease goal",
                                tint = CyberLime
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${numberFormatter.format(dailyGoal)}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Text(
                                text = "steps per day",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        IconButton(
                            onClick = {
                                val next = dailyGoal + 500L
                                fitnessRepository.setDailyGoal(next)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(DarkBg, CircleShape)
                                .border(1.dp, DarkBorder, CircleShape)
                                .testTag("increase_goal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase goal",
                                tint = CyberLime
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset goal chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(4000L, 6000L, 8000L, 10000L, 12000L).forEach { preset ->
                            val isCurrent = dailyGoal == preset
                            FilterChip(
                                selected = isCurrent,
                                onClick = { fitnessRepository.setDailyGoal(preset) },
                                label = {
                                    Text(
                                        text = "${preset / 1000}k",
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberLime,
                                    selectedLabelColor = DarkBg,
                                    containerColor = DarkBg,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("preset_goal_${preset}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Step Simulator (Instant visual testing in emulator)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "TEST PROGRESS ANIMATIONS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = CyberLime,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Simulate step increments to watch the circular ring and linear bar dynamically animate:",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                fitnessRepository.simulateStepsIncrement(250L)
                                competitionRepository?.updateMyChallengeSteps(currentSteps + 250L)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBg, contentColor = CyberLime),
                            border = BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("progress_test_add_250")
                        ) {
                            Text("+250", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                fitnessRepository.simulateStepsIncrement(500L)
                                competitionRepository?.updateMyChallengeSteps(currentSteps + 500L)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBg, contentColor = CyberLime),
                            border = BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("progress_test_add_500")
                        ) {
                            Text("+500", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                fitnessRepository.simulateStepsIncrement(1000L)
                                competitionRepository?.updateMyChallengeSteps(currentSteps + 1000L)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBg, contentColor = CyberLime),
                            border = BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("progress_test_add_1000")
                        ) {
                            Text("+1k", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Animated Circular Progress Ring Indicator
 */
@Composable
fun StepProgressCircle(
    currentSteps: Long,
    goal: Long,
    progressRatio: Float,
    isGoalAchieved: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 230.dp,
    strokeWidth: Dp = 18.dp
) {
    val animatedProgress = remember { Animatable(0f) }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    LaunchedEffect(progressRatio) {
        animatedProgress.animateTo(
            targetValue = progressRatio.coerceIn(0f, 1.5f),
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("step_progress_circle_indicator")
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)

            // Background circular track
            drawArc(
                color = Color(0xFF1E2838),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Dynamic Gradient Arc
            val gradient = Brush.sweepGradient(
                colors = listOf(
                    CyberLime,
                    ElectricTeal,
                    if (isGoalAchieved) MintSuccess else CyberLime
                )
            )

            val sweepAngle = (animatedProgress.value * 270f).coerceAtMost(270f)
            if (sweepAngle > 0f) {
                drawArc(
                    brush = gradient,
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val percentage = (progressRatio * 100f).toInt()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        if (isGoalAchieved) MintSuccess.copy(alpha = 0.2f) else CyberLime.copy(alpha = 0.15f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$percentage% COMPLETED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isGoalAchieved) MintSuccess else CyberLime,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = numberFormatter.format(currentSteps),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "OF ${numberFormatter.format(goal)} STEPS",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )
            )
        }
    }
}

/**
 * Animated Linear Progress Bar Indicator with Milestones
 */
@Composable
fun StepProgressLinear(
    currentSteps: Long,
    goal: Long,
    progressRatio: Float,
    isGoalAchieved: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    LaunchedEffect(progressRatio) {
        animatedProgress.animateTo(
            targetValue = progressRatio.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("step_progress_linear_indicator")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = numberFormatter.format(currentSteps),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "Current steps",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = numberFormatter.format(goal),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberLime
                    )
                )
                Text(
                    text = "Daily Target",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Canvas for high-precision rounded segmented linear bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E2838))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val progressWidth = size.width * animatedProgress.value
                if (progressWidth > 0f) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                CyberLime,
                                ElectricTeal,
                                if (isGoalAchieved) MintSuccess else CyberLime
                            )
                        ),
                        size = Size(progressWidth, size.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                    )
                }

                // Milestone tick dividers at 25%, 50%, 75%
                listOf(0.25f, 0.50f, 0.75f).forEach { fraction ->
                    val xPos = size.width * fraction
                    drawLine(
                        color = DarkBg.copy(alpha = 0.6f),
                        start = Offset(xPos, 0f),
                        end = Offset(xPos, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Milestone Labels Below Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("25%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("50%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("75%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("100%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberLime)
        }
    }
}

@Composable
private fun MetricItemCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun MilestoneRow(
    label: String,
    steps: Long,
    isCompleted: Boolean,
    isFinal: Boolean = false
) {
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                contentDescription = null,
                tint = if (isCompleted) MintSuccess else Color(0xFF323F55),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCompleted) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Text(
            text = "${numberFormatter.format(steps)} steps",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) MintSuccess else MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}
