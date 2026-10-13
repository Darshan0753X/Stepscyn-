package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.example.data.model.StepSource
import com.example.data.repository.FitnessDataRepository
import com.example.data.repository.SocialCompetitionRepository
import com.example.health.HealthConnectManager
import com.example.ui.theme.CyberLime
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.MintSuccess
import com.example.ui.theme.SolarAmber
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchSyncScreen(
    fitnessRepository: FitnessDataRepository,
    competitionRepository: SocialCompetitionRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncStatus by fitnessRepository.syncStatus.collectAsState()
    val isDemoMode by fitnessRepository.isDemoMode.collectAsState()
    val todayRecord by fitnessRepository.todayRecord.collectAsState()

    var showManualLogDialog by remember { mutableStateOf(false) }
    var hasHealthPermissions by remember { mutableStateOf(false) }

    // Health Connect Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        scope.launch {
            hasHealthPermissions = fitnessRepository.healthConnectManager.hasAllPermissions()
            if (hasHealthPermissions) {
                fitnessRepository.syncFromHealthConnect()
                Toast.makeText(context, "Health Connect permissions granted & synced!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permissions not granted. You can use manual logging or Demo Mode.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val availability = remember { fitnessRepository.healthConnectManager.checkAvailability() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smartwatch & Fitness Sync",
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
        modifier = modifier.testTag("watch_sync_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Health Connect Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = CyberLime,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "HEALTH CONNECT INTEGRATION",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        color = CyberLime,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        when (availability) {
                                            HealthConnectManager.AvailabilityStatus.AVAILABLE -> MintSuccess.copy(alpha = 0.2f)
                                            else -> SolarAmber.copy(alpha = 0.2f)
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = availability.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = when (availability) {
                                            HealthConnectManager.AvailabilityStatus.AVAILABLE -> MintSuccess
                                            else -> SolarAmber
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "StepSync uses Android's official Health Connect SDK to read aggregated step data from compatible smartwatches without double-counting records.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val client = fitnessRepository.healthConnectManager.healthConnectClient
                                    if (client != null) {
                                        permissionLauncher.launch(fitnessRepository.healthConnectManager.permissions)
                                    } else {
                                        Toast.makeText(context, "Health Connect is unavailable on this environment. Demo mode active.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberLime,
                                    contentColor = DarkBg
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("request_health_permissions_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Permissions", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val ok = fitnessRepository.syncFromHealthConnect()
                                        if (ok) {
                                            Toast.makeText(context, "Sync complete!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Sync attempt finished. Source: ${syncStatus.source.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricTeal),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("refresh_sync_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Demo Mode & Manual Entry Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Interactive Demo Mode",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                                Text(
                                    text = "Preview features without physical watch connection",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Switch(
                                checked = isDemoMode,
                                onCheckedChange = { fitnessRepository.toggleDemoMode(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DarkBg,
                                    checkedTrackColor = CyberLime
                                ),
                                modifier = Modifier.testTag("demo_mode_toggle_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showManualLogDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E2838),
                                contentColor = CyberLime
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_manual_log_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Steps Manually (Fallback)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Supported Smartwatch Ecosystem Guides
            item {
                Text(
                    text = "SMARTWATCH COMPATIBILITY GUIDES",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyberLime,
                        letterSpacing = 1.sp
                    )
                )
            }

            items(competitionRepository.supportedWatches) { watch ->
                SmartwatchGuideCard(watch = watch)
            }
        }
    }

    if (showManualLogDialog) {
        ManualLogDialog(
            onDismiss = { showManualLogDialog = false },
            onLogSteps = { steps ->
                scope.launch {
                    fitnessRepository.logManualSteps(steps)
                    competitionRepository.updateMyChallengeSteps(todayRecord.stepCount + steps)
                    Toast.makeText(context, "Logged $steps steps (Marked as Manual Entry)", Toast.LENGTH_SHORT).show()
                }
                showManualLogDialog = false
            }
        )
    }
}

@Composable
private fun SmartwatchGuideCard(watch: com.example.data.model.SmartwatchSpec) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("watch_guide_${watch.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E2838), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = null,
                            tint = if (watch.isDirectHealthConnect) CyberLime else SolarAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${watch.brand} ${watch.model}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = "Companion: ${watch.companionAppName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (watch.isDirectHealthConnect) MintSuccess.copy(alpha = 0.15f) else SolarAmber.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (watch.isDirectHealthConnect) "Direct Health Connect" else "Bridge / Manual",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (watch.isDirectHealthConnect) MintSuccess else SolarAmber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            watch.setupInstructions.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${index + 1}. ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyberLime
                        )
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Note: ${watch.note}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun ManualLogDialog(
    onDismiss: () -> Unit,
    onLogSteps: (Long) -> Unit
) {
    var stepsText by remember { mutableStateOf("1500") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Steps Manually",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "If your smartwatch companion app does not write to Health Connect directly, you can log steps manually. All manual records are labeled transparently.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                OutlinedTextField(
                    value = stepsText,
                    onValueChange = { stepsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Step Count") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberLime,
                        focusedLabelColor = CyberLime
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_step_count_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = stepsText.toLongOrNull() ?: 0L
                    if (s > 0) onLogSteps(s)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberLime, contentColor = DarkBg),
                modifier = Modifier.testTag("confirm_manual_step_button")
            ) {
                Text("Add Steps", fontWeight = FontWeight.Bold)
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
