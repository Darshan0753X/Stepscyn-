package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.SolarAmber
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@Composable
fun HeadToHeadCard(
    mySteps: Long,
    rivalName: String,
    rivalSteps: Long,
    goal: Long = 5000L,
    isDemoData: Boolean = true,
    modifier: Modifier = Modifier
) {
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val delta = mySteps - rivalSteps
    val isAhead = delta >= 0
    val absDelta = abs(delta)

    val myProgress by animateFloatAsState(
        targetValue = (mySteps.toFloat() / goal.toFloat()).coerceIn(0f, 1f),
        label = "my_progress"
    )
    val rivalProgress by animateFloatAsState(
        targetValue = (rivalSteps.toFloat() / goal.toFloat()).coerceIn(0f, 1f),
        label = "rival_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("head_to_head_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Challenge Title & Demo Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsScore,
                        contentDescription = "Duel Icon",
                        tint = CyberLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DAILY 1-ON-1 SHOWDOWN",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = CyberLime,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                if (isDemoData) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2C2416), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DEMO RIVAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SolarAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Player 1: You
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyberLime.copy(alpha = 0.15f), CircleShape)
                            .border(1.5.dp, CyberLime, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "You",
                            tint = CyberLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "You (Darshan)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Galaxy Fit3",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Text(
                    text = "${numberFormatter.format(mySteps)} steps",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberLime
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { myProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = CyberLime,
                trackColor = Color(0xFF1E2838)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Player 2: Rival
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(ElectricTeal.copy(alpha = 0.15f), CircleShape)
                            .border(1.5.dp, ElectricTeal, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Rival",
                            tint = ElectricTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = rivalName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Fastrack Reflex SW2",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Text(
                    text = "${numberFormatter.format(rivalSteps)} steps",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricTeal
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { rivalProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ElectricTeal,
                trackColor = Color(0xFF1E2838)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Delta Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isAhead) CyberLime.copy(alpha = 0.10f) else Color(0xFF2C161D),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (isAhead) CyberLime.copy(alpha = 0.3f) else Color(0xFFFF747F).copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAhead) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = if (isAhead) CyberLime else Color(0xFFFF747F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAhead) "YOU ARE LEADING BY" else "$rivalName IS AHEAD BY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isAhead) CyberLime else Color(0xFFFF747F)
                            )
                        )
                    }

                    Text(
                        text = "${numberFormatter.format(absDelta)} STEPS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAhead) CyberLime else Color(0xFFFF747F)
                        )
                    )
                }
            }
        }
    }
}
