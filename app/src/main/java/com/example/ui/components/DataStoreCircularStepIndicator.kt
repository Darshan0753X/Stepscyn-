package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.FitnessDataRepository
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.SolarAmber
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * A circular progress indicator that visualizes the current percentage toward
 * the user's daily step goal fetched from DataStore preferences.
 */
@Composable
fun DataStoreCircularStepIndicator(
    fitnessRepository: FitnessDataRepository,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    strokeWidth: Dp = 18.dp,
    streakDays: Int = 1,
    onClick: (() -> Unit)? = null
) {
    val preferencesManager = fitnessRepository.preferencesManager
    val persistedGoal by preferencesManager.dailyStepGoalFlow.collectAsState(
        initial = UserPreferencesManager.DEFAULT_DAILY_GOAL
    )
    val todayRecord by fitnessRepository.todayRecord.collectAsState()

    DataStoreCircularStepIndicator(
        currentSteps = todayRecord.stepCount,
        targetDailyGoal = persistedGoal,
        streakDays = streakDays,
        modifier = modifier,
        size = size,
        strokeWidth = strokeWidth,
        onClick = onClick
    )
}

/**
 * Overload accepting explicit currentSteps and targetDailyGoal values (from DataStore).
 */
@Composable
fun DataStoreCircularStepIndicator(
    currentSteps: Long,
    targetDailyGoal: Long,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp,
    strokeWidth: Dp = 18.dp,
    streakDays: Int = 1,
    onClick: (() -> Unit)? = null
) {
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    // Progress percentage calculations
    val safeGoal = targetDailyGoal.coerceAtLeast(1L)
    val progressRatio = (currentSteps.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1.5f)
    val percentage = (progressRatio * 100f).toInt()
    val isGoalAchieved = currentSteps >= targetDailyGoal
    val remainingSteps = (targetDailyGoal - currentSteps).coerceAtLeast(0L)

    // Animated sweep progress
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progressRatio) {
        animatedProgress.animateTo(
            targetValue = progressRatio.coerceAtMost(1f),
            animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("datastore_circular_step_indicator")
            .then(
                if (onClick != null) {
                    Modifier
                        .clip(CircleShape)
                        .clickable { onClick() }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Custom Canvas for the High-Precision Radial Progress Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(
                x = (this.size.width - diameter) / 2f,
                y = (this.size.height - diameter) / 2f
            )

            // 1. Background Inactive Circle Track (Full 360° or open arc)
            drawArc(
                color = Color(0xFF1B2332),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 2. Milestone Reference Ticks at 25%, 50%, 75%
            val milestones = listOf(0.25f, 0.50f, 0.75f)
            val radius = diameter / 2f
            val centerOffset = Offset(this.size.width / 2f, this.size.height / 2f)

            milestones.forEach { m ->
                val angleDeg = 135f + (m * 270f)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val innerR = radius - (strokePx * 0.45f)
                val outerR = radius + (strokePx * 0.45f)

                val startTick = Offset(
                    (centerOffset.x + innerR * cos(angleRad)).toFloat(),
                    (centerOffset.y + innerR * sin(angleRad)).toFloat()
                )
                val endTick = Offset(
                    (centerOffset.x + outerR * cos(angleRad)).toFloat(),
                    (centerOffset.y + outerR * sin(angleRad)).toFloat()
                )

                drawLine(
                    color = Color(0xFF0F141E).copy(alpha = 0.8f),
                    start = startTick,
                    end = endTick,
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 3. Foreground Progress Arc with Gradient
            val progressBrush = Brush.sweepGradient(
                colors = if (isGoalAchieved) {
                    listOf(MintSuccess, CyberLime, MintSuccess)
                } else {
                    listOf(CyberLime, ElectricTeal, CyberLime)
                }
            )

            val sweepAngle = (animatedProgress.value * 270f).coerceAtMost(270f)
            if (sweepAngle > 0f) {
                drawArc(
                    brush = progressBrush,
                    startAngle = 135f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Glowing Head Indicator Dot
                val endAngleRad = Math.toRadians((135f + sweepAngle).toDouble())
                val headX = (centerOffset.x + radius * cos(endAngleRad)).toFloat()
                val headY = (centerOffset.y + radius * sin(endAngleRad)).toFloat()

                drawCircle(
                    color = Color.White,
                    radius = strokePx * 0.28f,
                    center = Offset(headX, headY)
                )
            }
        }

        // Center Content: Prominent Percentage & DataStore Step Target
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Streak Pill or Milestone Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFF231C13), RoundedCornerShape(12.dp))
                    .border(1.dp, SolarAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = SolarAmber,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streakDays DAY STREAK",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = SolarAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Large Animated Completion Percentage Text
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$percentage",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = if (isGoalAchieved) MintSuccess else MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-1).sp
                    ),
                    modifier = Modifier.testTag("datastore_progress_percentage_text")
                )
                Text(
                    text = "%",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isGoalAchieved) MintSuccess else CyberLime
                    ),
                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                )
            }

            // Steps vs DataStore Goal
            Text(
                text = "${numberFormatter.format(currentSteps)} / ${numberFormatter.format(targetDailyGoal)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("datastore_progress_step_ratio")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // DataStore Target Pill / Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                    .border(1.dp, if (isGoalAchieved) MintSuccess.copy(alpha = 0.4f) else DarkBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(
                    imageVector = if (isGoalAchieved) Icons.Default.CheckCircle else Icons.Default.Storage,
                    contentDescription = null,
                    tint = if (isGoalAchieved) MintSuccess else ElectricTeal,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isGoalAchieved) "GOAL COMPLETED" else "${numberFormatter.format(remainingSteps)} LEFT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isGoalAchieved) MintSuccess else ElectricTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}
