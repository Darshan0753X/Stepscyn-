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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Challenge
import com.example.data.model.ChallengeStatus
import com.example.data.model.ChallengeType
import com.example.data.repository.SocialCompetitionRepository
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.SolarAmber
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(
    competitionRepository: SocialCompetitionRepository,
    modifier: Modifier = Modifier
) {
    val challenges by competitionRepository.challenges.collectAsState()
    val friends by competitionRepository.friends.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val tabs = listOf("Active (${challenges.count { it.status == ChallengeStatus.ACTIVE }})", "Upcoming", "Completed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Fitness Challenges",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CyberLime,
                contentColor = DarkBg,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("create_challenge_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Challenge")
            }
        },
        containerColor = DarkBg,
        modifier = modifier.testTag("challenges_screen")
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

            // Challenge List
            val displayedChallenges = when (selectedTab) {
                0 -> challenges.filter { it.status == ChallengeStatus.ACTIVE }
                1 -> challenges.filter { it.status == ChallengeStatus.UPCOMING }
                else -> challenges.filter { it.status == ChallengeStatus.COMPLETED }
            }

            if (displayedChallenges.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SportsScore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${tabs[selectedTab].substringBefore(" ")} Challenges",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to challenge a friend to a 1-on-1 step showdown!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayedChallenges) { challenge ->
                        ChallengeCard(challenge = challenge)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateChallengeDialog(
            friends = friends,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, desc, type, targetSteps, rivalUid ->
                competitionRepository.createChallenge(title, desc, type, targetSteps, rivalUid)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ChallengeCard(challenge: Challenge) {
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("challenge_card_${challenge.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(CyberLime.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = CyberLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = challenge.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = challenge.challengeType.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CyberLime,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Ends",
                        tint = SolarAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = challenge.endDate,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SolarAmber,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Participants Progress
            challenge.participantIds.forEach { uid ->
                val name = challenge.participantNames[uid] ?: "Participant"
                val steps = challenge.participantSteps[uid] ?: 0L
                val progress = (steps.toFloat() / challenge.targetSteps.toFloat()).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "${numberFormatter.format(steps)} / ${numberFormatter.format(challenge.targetSteps)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (steps >= challenge.targetSteps) MintSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (name.contains("Darshan") || name.contains("You")) CyberLime else ElectricTeal,
                    trackColor = Color(0xFF1B2230)
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Tie-breaking info banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B26), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tie-breaking: Highest total steps wins. Identical counts tie.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CreateChallengeDialog(
    friends: List<com.example.data.model.FriendProfile>,
    onDismiss: () -> Unit,
    onCreate: (title: String, desc: String, type: ChallengeType, targetSteps: Long, rivalUid: String) -> Unit
) {
    var title by remember { mutableStateOf("Showdown Sprint") }
    var desc by remember { mutableStateOf("Head-to-head sprint to reach the goal first!") }
    var selectedType by remember { mutableStateOf(ChallengeType.ONE_ON_ONE_DUEL) }
    var targetSteps by remember { mutableStateOf("5000") }
    var selectedRivalUid by remember { mutableStateOf(friends.firstOrNull()?.uid ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Fitness Challenge",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Challenge Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberLime,
                        focusedLabelColor = CyberLime
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("challenge_title_input")
                )

                OutlinedTextField(
                    value = targetSteps,
                    onValueChange = { targetSteps = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Target Steps") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberLime,
                        focusedLabelColor = CyberLime
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("challenge_target_steps_input")
                )

                Text(
                    text = "Choose Rival:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                friends.forEach { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedRivalUid == friend.uid) CyberLime.copy(alpha = 0.15f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { selectedRivalUid = friend.uid }) {
                            Text(
                                text = "${friend.displayName} (${friend.watchModel})",
                                color = if (selectedRivalUid == friend.uid) CyberLime else MaterialTheme.colorScheme.onBackground,
                                fontWeight = if (selectedRivalUid == friend.uid) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val steps = targetSteps.toLongOrNull() ?: 5000L
                    onCreate(title, desc, selectedType, steps, selectedRivalUid)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberLime,
                    contentColor = DarkBg
                ),
                modifier = Modifier.testTag("confirm_create_challenge_button")
            ) {
                Text("Create Challenge", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = DarkSurfaceElevated
    )
}
