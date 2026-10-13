package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DailyStepDao
import com.example.data.model.DailyStepRecord
import com.example.data.model.StepSource
import com.example.data.model.SyncStatus
import com.example.health.HealthConnectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class FitnessDataRepository(
    private val context: Context,
    private val dailyStepDao: DailyStepDao = AppDatabase.getDatabase(context).dailyStepDao(),
    val healthConnectManager: HealthConnectManager = HealthConnectManager(context)
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _isDemoMode = MutableStateFlow(true)
    val isDemoMode: StateFlow<Boolean> = _isDemoMode.asStateFlow()

    private val _todayRecord = MutableStateFlow(createInitialDemoRecord())
    val todayRecord: StateFlow<DailyStepRecord> = _todayRecord.asStateFlow()

    private val _recentDays = MutableStateFlow<List<DailyStepRecord>>(emptyList())
    val recentDays: StateFlow<List<DailyStepRecord>> = _recentDays.asStateFlow()

    private val _syncStatus = MutableStateFlow(
        SyncStatus(
            isConnected = true,
            lastSyncedAt = System.currentTimeMillis(),
            source = StepSource.DEMO_SIMULATION,
            activeWatchName = "Samsung Galaxy Fit3 (Demo Mode)"
        )
    )
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    init {
        scope.launch {
            loadStoredOrDemoData()
        }
    }

    private suspend fun loadStoredOrDemoData() {
        val todayStr = LocalDate.now().format(dateFormatter)
        val existing = dailyStepDao.getStepRecordSync(todayStr)
        if (existing != null) {
            _todayRecord.value = existing
        } else {
            // Seed initial demo data for 7 days so charts and streaks are populated on first launch
            seedInitialDemoHistory()
        }
        observeHistory()
    }

    private suspend fun seedInitialDemoHistory() {
        val today = LocalDate.now()
        val seeded = mutableListOf<DailyStepRecord>()

        // 7 days history
        val sampleSteps = listOf(5420L, 6150L, 4900L, 5800L, 7200L, 6300L, 3850L)
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val dateStr = date.format(dateFormatter)
            val steps = sampleSteps[6 - i]
            val hourly = generateSampleHourlyDistribution(steps)
            val record = DailyStepRecord(
                date = dateStr,
                stepCount = steps,
                goal = 5000L,
                source = StepSource.DEMO_SIMULATION.name,
                lastSyncedAt = System.currentTimeMillis() - (i * 86400000L),
                hourlyStepsString = hourly.joinToString(",")
            )
            dailyStepDao.insertOrUpdate(record)
            seeded.add(record)
        }

        val todayRecord = seeded.last()
        _todayRecord.value = todayRecord
        _recentDays.value = seeded.reversed()
    }

    private fun observeHistory() {
        scope.launch {
            dailyStepDao.getRecentDays(14).collect { list ->
                if (list.isNotEmpty()) {
                    _recentDays.value = list
                    val todayStr = LocalDate.now().format(dateFormatter)
                    val today = list.find { it.date == todayStr }
                    if (today != null) {
                        _todayRecord.value = today
                    }
                }
            }
        }
    }

    suspend fun syncFromHealthConnect(): Boolean {
        val hasPerm = healthConnectManager.hasAllPermissions()
        if (!hasPerm) {
            _syncStatus.value = _syncStatus.value.copy(
                isConnected = false,
                errorMessage = "Health Connect permissions not granted"
            )
            return false
        }

        return try {
            val steps = healthConnectManager.readTodaySteps()
            val hourly = healthConnectManager.readHourlyStepsForToday()
            val todayStr = LocalDate.now().format(dateFormatter)

            val updated = _todayRecord.value.copy(
                stepCount = steps,
                source = StepSource.HEALTH_CONNECT.name,
                lastSyncedAt = System.currentTimeMillis(),
                hourlyStepsString = hourly.joinToString(",")
            )
            dailyStepDao.insertOrUpdate(updated)
            _todayRecord.value = updated

            _syncStatus.value = SyncStatus(
                isConnected = true,
                lastSyncedAt = System.currentTimeMillis(),
                source = StepSource.HEALTH_CONNECT,
                activeWatchName = "Health Connect (Live Synced)",
                errorMessage = null
            )
            _isDemoMode.value = false
            true
        } catch (e: Exception) {
            _syncStatus.value = _syncStatus.value.copy(
                isConnected = false,
                errorMessage = e.message ?: "Failed to sync Health Connect"
            )
            false
        }
    }

    suspend fun logManualSteps(steps: Long) {
        val todayStr = LocalDate.now().format(dateFormatter)
        val current = _todayRecord.value
        val newTotal = current.stepCount + steps
        val newHourly = current.getHourlySteps().toMutableList()
        val currentHour = java.time.LocalTime.now().hour
        newHourly[currentHour] = newHourly[currentHour] + steps.toInt()

        val updated = current.copy(
            stepCount = newTotal,
            source = StepSource.MANUAL_ENTRY.name,
            lastSyncedAt = System.currentTimeMillis(),
            hourlyStepsString = newHourly.joinToString(",")
        )

        dailyStepDao.insertOrUpdate(updated)
        _todayRecord.value = updated
        _syncStatus.value = SyncStatus(
            isConnected = true,
            lastSyncedAt = System.currentTimeMillis(),
            source = StepSource.MANUAL_ENTRY,
            activeWatchName = "Manual Step Log",
            errorMessage = null
        )
    }

    fun setDailyGoal(newGoal: Long) {
        scope.launch {
            val updated = _todayRecord.value.copy(goal = newGoal)
            dailyStepDao.insertOrUpdate(updated)
            _todayRecord.value = updated
        }
    }

    fun toggleDemoMode(enabled: Boolean) {
        _isDemoMode.value = enabled
        _syncStatus.value = _syncStatus.value.copy(
            source = if (enabled) StepSource.DEMO_SIMULATION else StepSource.HEALTH_CONNECT,
            activeWatchName = if (enabled) "Samsung Galaxy Fit3 (Demo Mode)" else "Health Connect"
        )
    }

    // Interactive tester for user preview: Simulate taking steps
    fun simulateStepsIncrement(additionalSteps: Long) {
        scope.launch {
            val current = _todayRecord.value
            val newTotal = current.stepCount + additionalSteps
            val hourly = current.getHourlySteps().toMutableList()
            val hour = java.time.LocalTime.now().hour
            hourly[hour] = hourly[hour] + additionalSteps.toInt()

            val updated = current.copy(
                stepCount = newTotal,
                hourlyStepsString = hourly.joinToString(",")
            )
            dailyStepDao.insertOrUpdate(updated)
            _todayRecord.value = updated
        }
    }

    private fun createInitialDemoRecord(): DailyStepRecord {
        val todayStr = LocalDate.now().format(dateFormatter)
        val hourly = generateSampleHourlyDistribution(3850L)
        return DailyStepRecord(
            date = todayStr,
            stepCount = 3850L,
            goal = 5000L,
            source = StepSource.DEMO_SIMULATION.name,
            lastSyncedAt = System.currentTimeMillis(),
            hourlyStepsString = hourly.joinToString(",")
        )
    }

    private fun generateSampleHourlyDistribution(total: Long): List<Int> {
        val hourly = MutableList(24) { 0 }
        val activeHours = listOf(7, 8, 9, 12, 13, 17, 18, 19, 20)
        var remaining = total

        for (h in activeHours) {
            val portion = (remaining * Random.nextDouble(0.15, 0.35)).toInt()
            hourly[h] = portion
            remaining = (remaining - portion).coerceAtLeast(0L)
        }
        hourly[18] = hourly[18] + remaining.toInt()
        return hourly
    }
}
