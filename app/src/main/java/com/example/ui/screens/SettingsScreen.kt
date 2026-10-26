package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesManager
import com.example.data.repository.FitnessDataRepository
import com.example.health.HealthConnectSyncService
import com.example.health.HealthSyncState
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    fitnessRepository: FitnessDataRepository,
    healthConnectSyncService: HealthConnectSyncService? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val numberFormatter = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    val preferencesManager = fitnessRepository.preferencesManager
    val persistedDailyGoal by preferencesManager.dailyStepGoalFlow.collectAsState(initial = UserPreferencesManager.DEFAULT_DAILY_GOAL)
    val notificationsEnabled by preferencesManager.notificationsEnabledFlow.collectAsState(initial = true)
    val syncState by (healthConnectSyncService?.syncState?.collectAsState() ?: remember { mutableStateOf(HealthSyncState.Idle(System.currentTimeMillis())) })

    // User input state for the daily step goal
    var inputGoalText by remember { mutableStateOf(persistedDailyGoal.toString()) }
    var saveSuccessVisible by remember { mutableStateOf(false) }

    // Keep input field updated when persisted value changes initially
    LaunchedEffect(persistedDailyGoal) {
        inputGoalText = persistedDailyGoal.toString()
    }

    val parsedGoal = inputGoalText.toLongOrNull() ?: 0L
    val isGoalValid = parsedGoal in UserPreferencesManager.MIN_DAILY_GOAL..UserPreferencesManager.MAX_DAILY_GOAL
    val hasUnsavedChanges = isGoalValid && (parsedGoal != persistedDailyGoal)

    val presetGoals = listOf(5000L, 8000L, 10000L, 12500L, 15000L)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberLime
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg,
        modifier = modifier.testTag("settings_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Banner: Active Target in DataStore
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberLime.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(CyberLime.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, CyberLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Adjust,
                                contentDescription = null,
                                tint = CyberLime,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CURRENT DAILY TARGET",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CyberLime,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = numberFormatter.format(persistedDailyGoal),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "steps / day",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = ElectricTeal,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Persisted in DataStore Preferences",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ElectricTeal,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Input Target Daily Step Goal Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "CUSTOMIZE STEP GOAL",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = CyberLime,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Enter your desired target daily step goal. Changes persist automatically in DataStore preferences.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Numeric Input Field with Stepper Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decrease Stepper Button
                            IconButton(
                                onClick = {
                                    val currentVal = inputGoalText.toLongOrNull() ?: persistedDailyGoal
                                    val newVal = (currentVal - 500L).coerceAtLeast(UserPreferencesManager.MIN_DAILY_GOAL)
                                    inputGoalText = newVal.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(DarkSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .testTag("decrease_step_goal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Goal by 500",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // OutlinedTextField for Direct Step Number Input
                            OutlinedTextField(
                                value = inputGoalText,
                                onValueChange = { newValue ->
                                    val filtered = newValue.filter { it.isDigit() }.take(5)
                                    inputGoalText = filtered
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("daily_step_goal_input"),
                                label = { Text("Target Daily Steps") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (isGoalValid) {
                                            fitnessRepository.setDailyGoal(parsedGoal)
                                            saveSuccessVisible = true
                                        }
                                    }
                                ),
                                isError = inputGoalText.isNotEmpty() && !isGoalValid,
                                supportingText = {
                                    if (inputGoalText.isNotEmpty() && !isGoalValid) {
                                        Text(
                                            text = "Goal must be between 1,000 and 50,000 steps",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        Text(
                                            text = "Recommended: 8,000 – 12,500 steps",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberLime,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedLabelColor = CyberLime,
                                    cursorColor = CyberLime,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            // Increase Stepper Button
                            IconButton(
                                onClick = {
                                    val currentVal = inputGoalText.toLongOrNull() ?: persistedDailyGoal
                                    val newVal = (currentVal + 500L).coerceAtMost(UserPreferencesManager.MAX_DAILY_GOAL)
                                    inputGoalText = newVal.toString()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(DarkSurface, RoundedCornerShape(12.dp))
                                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                                    .testTag("increase_step_goal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Goal by 500",
                                    tint = CyberLime
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Presets FlowRow
                        Text(
                            text = "Quick Presets:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presetGoals.forEach { preset ->
                                val isSelected = parsedGoal == preset
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        inputGoalText = preset.toString()
                                        focusManager.clearFocus()
                                    },
                                    label = {
                                        Text(
                                            text = "${numberFormatter.format(preset)}",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberLime,
                                        selectedLabelColor = DarkBg,
                                        containerColor = DarkSurface,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) CyberLime else DarkBorder
                                    ),
                                    modifier = Modifier.testTag("preset_chip_$preset")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Save Button (Explicit persistence action)
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (isGoalValid) {
                                    fitnessRepository.setDailyGoal(parsedGoal)
                                    saveSuccessVisible = true
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Target goal updated to ${numberFormatter.format(parsedGoal)} steps in DataStore!")
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter a valid step goal (1,000 - 50,000)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = isGoalValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberLime,
                                contentColor = DarkBg,
                                disabledContainerColor = CyberLime.copy(alpha = 0.3f),
                                disabledContentColor = DarkBg.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_step_goal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasUnsavedChanges) "Save to DataStore Preferences" else "Save Target Daily Goal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Save Confirmation Feedback Banner
                        AnimatedVisibility(
                            visible = saveSuccessVisible,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .background(MintSuccess.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, MintSuccess.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MintSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Saved in DataStore Preferences successfully!",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MintSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Real-Time Goal Impact Estimator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "PROJECTED DAILY IMPACT",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = ElectricTeal,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val displayGoal = if (isGoalValid) parsedGoal else persistedDailyGoal
                        val distanceKm = String.format(Locale.US, "%.1f", displayGoal * 0.000762)
                        val caloriesBurned = (displayGoal * 0.04).toInt()
                        val activeMinutes = (displayGoal / 115.0).toInt()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ImpactMetricBox(
                                icon = Icons.Default.DirectionsWalk,
                                value = "$distanceKm km",
                                label = "Est. Distance",
                                tint = CyberLime,
                                modifier = Modifier.weight(1f)
                            )
                            ImpactMetricBox(
                                icon = Icons.Default.LocalFireDepartment,
                                value = "$caloriesBurned kcal",
                                label = "Active Burn",
                                tint = SolarAmber,
                                modifier = Modifier.weight(1f)
                            )
                            ImpactMetricBox(
                                icon = Icons.Default.Timer,
                                value = "$activeMinutes min",
                                label = "Walking Time",
                                tint = ElectricTeal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Health Connect Sync Integration Section
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
                                    text = "HEALTH CONNECT INTEGRATION",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = CyberLime,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Bi-directional step sync with Google Health Connect",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    if (healthConnectSyncService != null) {
                                        val ok = healthConnectSyncService.syncUserActivity()
                                        Toast.makeText(
                                            context,
                                            if (ok) "Health Connect steps synced!" else "Health Connect sync attempted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(context, "Health Connect service ready", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurface,
                                contentColor = ElectricTeal
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricTeal.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("sync_health_connect_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sync Steps with Health Connect Now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Preferences & Defaults Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "PREFERENCES",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = CyberLime,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Goal Milestones & Alerts",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "Receive alerts at 25%, 50%, 75% and 100%",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { isChecked ->
                                    scope.launch {
                                        preferencesManager.setNotificationsEnabled(isChecked)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberLime,
                                    checkedTrackColor = CyberLime.copy(alpha = 0.4f),
                                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    uncheckedTrackColor = DarkSurface
                                ),
                                modifier = Modifier.testTag("notifications_toggle")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    preferencesManager.resetToDefaults()
                                    fitnessRepository.setDailyGoal(UserPreferencesManager.DEFAULT_DAILY_GOAL)
                                    inputGoalText = UserPreferencesManager.DEFAULT_DAILY_GOAL.toString()
                                    snackbarHostState.showSnackbar("Settings reset to defaults (10,000 steps)")
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("reset_defaults_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reset to Factory Defaults",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ImpactMetricBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(14.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
