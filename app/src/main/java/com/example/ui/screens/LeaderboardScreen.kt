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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.data.repository.FitnessDataRepository
import com.example.data.repository.SocialCompetitionRepository
import com.example.ui.components.LeaderboardRow
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricTeal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    fitnessRepository: FitnessDataRepository,
    competitionRepository: SocialCompetitionRepository,
    modifier: Modifier = Modifier
) {
    val todayRecord by fitnessRepository.todayRecord.collectAsState()
    val leaderboardEntries = remember(todayRecord.stepCount) {
        competitionRepository.getDailyLeaderboard(todayRecord.stepCount)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Daily", "Weekly", "All-Time")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg,
        modifier = modifier.testTag("leaderboard_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkBg,
                contentColor = CyberLime,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyberLime
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTab == index) CyberLime else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Podium Top 3 presentation
                item {
                    PodiumSection(entries = leaderboardEntries)
                }

                // Header & Info Note
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ALL PARTICIPANTS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyberLime,
                                letterSpacing = 1.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = " Data Freshness Verified",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                items(leaderboardEntries) { entry ->
                    LeaderboardRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(entries: List<LeaderboardEntry>) {
    if (entries.size < 2) return

    val first = entries.getOrNull(0) ?: return
    val second = entries.getOrNull(1)
    val third = entries.getOrNull(2)

    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_podium_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TODAY'S PODIUM",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = CyberLime,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Left)
                if (second != null) {
                    PodiumPillar(
                        rank = 2,
                        name = second.displayName.substringBefore(" "),
                        steps = numberFormatter.format(second.steps),
                        cupColor = Color(0xFFC0C0C0),
                        height = 80.dp
                    )
                }

                // 1st Place (Center - Tallest)
                PodiumPillar(
                    rank = 1,
                    name = first.displayName.substringBefore(" "),
                    steps = numberFormatter.format(first.steps),
                    cupColor = Color(0xFFFFD700),
                    height = 110.dp
                )

                // 3rd Place (Right)
                if (third != null) {
                    PodiumPillar(
                        rank = 3,
                        name = third.displayName.substringBefore(" "),
                        steps = numberFormatter.format(third.steps),
                        cupColor = Color(0xFFCD7F32),
                        height = 65.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumPillar(
    rank: Int,
    name: String,
    steps: String,
    cupColor: Color,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(if (rank == 1) 48.dp else 40.dp)
                .background(cupColor.copy(alpha = 0.15f), CircleShape)
                .border(1.5.dp, cupColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = cupColor,
                modifier = Modifier.size(if (rank == 1) 28.dp else 22.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Text(
            text = "$steps steps",
            style = MaterialTheme.typography.labelSmall.copy(
                color = cupColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Pillar block
        Box(
            modifier = Modifier
                .size(width = 80.dp, height = height)
                .background(
                    if (rank == 1) CyberLime.copy(alpha = 0.2f) else Color(0xFF1E2838),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .border(
                    1.dp,
                    if (rank == 1) CyberLime.copy(alpha = 0.5f) else Color(0xFF2E3D55),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = cupColor
                )
            )
        }
    }
}
