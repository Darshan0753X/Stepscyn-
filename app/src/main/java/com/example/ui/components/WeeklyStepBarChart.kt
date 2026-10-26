package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyStepRecord
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.SolarAmber
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class DayChartItem(
    val date: LocalDate,
    val dayLabel: String,
    val dateLabel: String,
    val steps: Long,
    val goal: Long,
    val isToday: Boolean,
    val source: String
) {
    val progressFraction: Float = if (goal > 0) (steps.toFloat() / goal.toFloat()) else 0f
    val isGoalMet: Boolean = steps >= goal
}

/**
 * Recharts-inspired athletic weekly bar chart displaying step history fetched from Health Connect.
 */
@Composable
fun WeeklyStepBarChart(
    weeklyRecords: List<DailyStepRecord>,
    targetGoal: Long,
    isSyncing: Boolean = false,
    onSyncRequested: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val today = remember { LocalDate.now() }

    // Normalize or construct 7 days of historical step data (from 6 days ago up to today)
    val chartDays = remember(weeklyRecords, targetGoal) {
        val recordMap = weeklyRecords.associateBy { it.date }
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val dateStr = date.format(formatter)
            val record = recordMap[dateStr]

            val steps = record?.stepCount ?: 0L
            val dayGoal = if ((record?.goal ?: 0L) > 0L) record!!.goal else targetGoal
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val shortDate = "${date.monthValue}/${date.dayOfMonth}"

            DayChartItem(
                date = date,
                dayLabel = dayName,
                dateLabel = shortDate,
                steps = steps,
                goal = dayGoal,
                isToday = (offset == 0),
                source = record?.source ?: "Health Connect"
            )
        }
    }

    // Selected day for interactive inspection
    var selectedDayIndex by remember { mutableStateOf(chartDays.lastIndex) }
    val selectedDay = chartDays.getOrNull(selectedDayIndex) ?: chartDays.last()

    // Aggregate statistics
    val totalSteps = remember(chartDays) { chartDays.sumOf { it.steps } }
    val averageSteps = remember(chartDays) { (totalSteps / 7.0).toLong() }
    val daysGoalMet = remember(chartDays) { chartDays.count { it.isGoalMet } }
    val maxChartSteps = remember(chartDays, targetGoal) {
        val peakSteps = chartDays.maxOfOrNull { it.steps } ?: 10000L
        (maxOf(peakSteps, targetGoal) * 1.15f).toLong().coerceAtLeast(1000L)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_step_bar_chart"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Chart Header with Live Sync Status & Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(CyberLime.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Weekly Progress Icon",
                            tint = CyberLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WEEKLY STEP HISTORY",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberLime,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = "Health Connect 7-Day Sync",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                if (onSyncRequested != null) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = CyberLime,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = onSyncRequested,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("refresh_weekly_chart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Health Connect",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Stat Summary KPI Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryKpiBadge(
                    label = "Total Steps",
                    value = numberFormat.format(totalSteps),
                    accentColor = CyberLime,
                    modifier = Modifier.weight(1f)
                )
                SummaryKpiBadge(
                    label = "Daily Avg",
                    value = numberFormat.format(averageSteps),
                    accentColor = ElectricTeal,
                    modifier = Modifier.weight(1f)
                )
                SummaryKpiBadge(
                    label = "Goals Met",
                    value = "$daysGoalMet / 7",
                    accentColor = if (daysGoalMet >= 5) MintSuccess else SolarAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Bar Chart Area (Recharts layout with benchmark goal line)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Goal Benchmark Reference Line
                val goalFraction = (targetGoal.toFloat() / maxChartSteps.toFloat()).coerceIn(0.05f, 0.95f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(bottom = (140.dp * goalFraction) + 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            CyberLime.copy(alpha = 0.5f),
                                            CyberLime.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Goal ${numberFormat.format(targetGoal)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberLime.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // 7 Weekly Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    chartDays.forEachIndexed { index, item ->
                        val isSelected = index == selectedDayIndex
                        val fraction = (item.steps.toFloat() / maxChartSteps.toFloat()).coerceIn(0.06f, 1f)
                        val animatedFraction by animateFloatAsState(
                            targetValue = fraction,
                            animationSpec = tween(durationMillis = 650, delayMillis = index * 40),
                            label = "bar_anim_$index"
                        )

                        val barBrush = remember(item.isGoalMet, isSelected) {
                            when {
                                item.isGoalMet -> Brush.verticalGradient(
                                    listOf(CyberLime, CyberLime.copy(alpha = 0.7f))
                                )
                                isSelected -> Brush.verticalGradient(
                                    listOf(ElectricTeal, ElectricTeal.copy(alpha = 0.6f))
                                )
                                else -> Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF3B4455),
                                        Color(0xFF232A36)
                                    )
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedDayIndex = index }
                                .padding(horizontal = 3.dp)
                                .testTag("weekly_bar_$index")
                        ) {
                            // Step Count Badge on top of bar if selected or met
                            if (isSelected || item.isGoalMet) {
                                Text(
                                    text = if (item.steps >= 1000) "${(item.steps / 1000.0).formatOneDecimal()}k" else "${item.steps}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (item.isGoalMet) CyberLime else ElectricTeal,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                            } else {
                                Spacer(modifier = Modifier.height(14.dp))
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Vertical Rounded Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp * animatedFraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                    .background(barBrush)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(1.5.dp, Color.White, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                        } else Modifier
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Day of Week Label
                            Text(
                                text = item.dayLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (item.isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (item.isToday) CyberLime else if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )

                            // Short Date or Today Dot
                            if (item.isToday) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .background(CyberLime, CircleShape)
                                )
                            } else {
                                Text(
                                    text = item.dateLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Day Detail Tooltip Card (Recharts-style active detail inspector)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_day_detail_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (selectedDay.isGoalMet) CyberLime.copy(alpha = 0.35f) else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedDay.dayLabel}, ${selectedDay.date.format(DateTimeFormatter.ofPattern("MMM d"))}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (selectedDay.isToday) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(CyberLime.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "TODAY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CyberLime,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = numberFormat.format(selectedDay.steps),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (selectedDay.isGoalMet) CyberLime else MaterialTheme.colorScheme.onBackground
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "steps",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    // Achievement / Metric pill
                    Column(horizontalAlignment = Alignment.End) {
                        val pct = (selectedDay.progressFraction * 100).toInt()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedDay.isGoalMet) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CyberLime,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "$pct% of goal",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = if (selectedDay.isGoalMet) CyberLime else ElectricTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Estimated Distance & Calories
                        val distanceKm = (selectedDay.steps * 0.000762).formatOneDecimal()
                        val calories = (selectedDay.steps * 0.04).toInt()
                        Text(
                            text = "~$distanceKm km • $calories kcal",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryKpiBadge(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor,
                    fontSize = 15.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
        }
    }
}

private fun Double.formatOneDecimal(): String {
    return String.format(Locale.US, "%.1f", this)
}
