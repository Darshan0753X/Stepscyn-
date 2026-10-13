package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardEntry
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.SolarAmber
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LeaderboardRow(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier
) {
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    val rankColor = when (entry.rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val containerBg = if (entry.isCurrentUser) {
        CyberLime.copy(alpha = 0.08f)
    } else {
        DarkSurfaceElevated
    }

    val borderColor = if (entry.isCurrentUser) {
        CyberLime.copy(alpha = 0.35f)
    } else {
        DarkBorder
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_${entry.rank}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Rank badge + Avatar + Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank number or podium cup
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (entry.rank in 1..3) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Rank ${entry.rank}",
                            tint = rankColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "#${entry.rank}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = rankColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (entry.isCurrentUser) CyberLime.copy(alpha = 0.2f) else Color(0xFF263245),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (entry.isCurrentUser) CyberLime else Color(0xFF384B66),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = entry.displayName,
                        tint = if (entry.isCurrentUser) CyberLime else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (entry.isCurrentUser) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (entry.isCurrentUser) CyberLime else MaterialTheme.colorScheme.onBackground
                            )
                        )
                        if (entry.streak > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFF282012), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = SolarAmber,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "${entry.streak}d",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SolarAmber,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Text(
                        text = if (entry.rank == 1) "Leader • ${entry.lastSyncedTimeText}" else "-${numberFormatter.format(entry.deltaToLeader)} behind leader",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Right: Steps
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = numberFormatter.format(entry.steps),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (entry.rank == 1) Color(0xFFFFD700) else MaterialTheme.colorScheme.onBackground
                    )
                )
                Text(
                    text = "STEPS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}
