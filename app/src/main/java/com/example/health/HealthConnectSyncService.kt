package com.example.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.data.model.DailyStepRecord
import com.example.data.model.StepSource
import com.example.data.model.SyncStatus
import com.example.data.repository.FitnessDataRepository
import com.example.data.repository.SocialCompetitionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service to request, read and sync step count data from the Android Health Connect API.
 */
class HealthConnectSyncService(
    private val context: Context,
    private val fitnessRepository: FitnessDataRepository,
    private val competitionRepository: SocialCompetitionRepository? = null
) {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val isSyncing = AtomicBoolean(false)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            HealthConnectClient.getOrCreate(context.applicationContext ?: context)
        } catch (e: Exception) {
            Log.w(TAG, "Health Connect client unavailable: ${e.message}")
            null
        }
    }

    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )

    private val _syncState = MutableStateFlow<HealthSyncState>(HealthSyncState.Idle(System.currentTimeMillis()))
    val syncState: StateFlow<HealthSyncState> = _syncState.asStateFlow()

    private val _lastSyncedSteps = MutableStateFlow(0L)
    val lastSyncedSteps: StateFlow<Long> = _lastSyncedSteps.asStateFlow()

    /**
     * Checks whether Health Connect is available on this Android device.
     */
    fun checkSdkAvailability(): HealthConnectManager.AvailabilityStatus {
        return try {
            val status = HealthConnectClient.getSdkStatus(context)
            when (status) {
                HealthConnectClient.SDK_AVAILABLE -> HealthConnectManager.AvailabilityStatus.AVAILABLE
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectManager.AvailabilityStatus.UPDATE_REQUIRED
                else -> HealthConnectManager.AvailabilityStatus.UNAVAILABLE
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking SDK availability", e)
            HealthConnectManager.AvailabilityStatus.UNAVAILABLE
        }
    }

    /**
     * Checks if the app currently holds the required Health Connect permissions.
     */
    suspend fun hasRequiredPermissions(): Boolean = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext false
        try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.contains(HealthPermission.getReadPermission(StepsRecord::class))
        } catch (e: Exception) {
            Log.e(TAG, "Error checking granted permissions: ${e.message}", e)
            false
        }
    }

    /**
     * Reads aggregated step count for today from Health Connect.
     */
    suspend fun readTodaySteps(): Long = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext 0L
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toInstant()
        val now = Instant.now()

        try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error reading today's steps: ${e.message}", e)
            0L
        }
    }

    /**
     * Reads hourly step distribution for today from individual records.
     */
    suspend fun readHourlyStepsForToday(): List<Int> = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext List(24) { 0 }
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toInstant()
        val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant()

        val hourlyList = MutableList(24) { 0 }

        try {
            val response = client.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                )
            )

            for (record in response.records) {
                val hour = record.startTime.atZone(zoneId).hour
                if (hour in 0..23) {
                    hourlyList[hour] = hourlyList[hour] + record.count.toInt()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading raw hourly records: ${e.message}")
        }

        hourlyList
    }

    /**
     * Reads aggregated step count for a specific historical date.
     */
    suspend fun readStepsForDate(date: LocalDate): Long = withContext(Dispatchers.IO) {
        val client = healthConnectClient ?: return@withContext 0L
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading steps for date $date: ${e.message}")
            0L
        }
    }

    /**
     * Reads past 7 days of daily step aggregates directly from Health Connect.
     */
    suspend fun readPastWeekDailySteps(): List<Pair<LocalDate, Long>> = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val list = mutableListOf<Pair<LocalDate, Long>>()
        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val count = if (i == 0) readTodaySteps() else readStepsForDate(date)
            list.add(date to count)
        }
        list
    }

    /**
     * Synchronizes user activity from Health Connect into local repository and UI states.
     */
    suspend fun syncUserActivity(): Boolean = withContext(Dispatchers.IO) {
        if (!isSyncing.compareAndSet(false, true)) {
            Log.d(TAG, "Sync already in progress, skipping")
            return@withContext false
        }

        _syncState.value = HealthSyncState.Syncing

        try {
            val availability = checkSdkAvailability()
            if (availability != HealthConnectManager.AvailabilityStatus.AVAILABLE) {
                val errorMsg = if (availability == HealthConnectManager.AvailabilityStatus.UPDATE_REQUIRED) {
                    "Health Connect provider requires update"
                } else {
                    "Health Connect is unavailable on this device"
                }
                _syncState.value = HealthSyncState.HealthConnectUnavailable(errorMsg)
                isSyncing.set(false)
                return@withContext false
            }

            if (!hasRequiredPermissions()) {
                _syncState.value = HealthSyncState.PermissionRequired
                isSyncing.set(false)
                return@withContext false
            }

            // Read live step count data from Health Connect
            val todaySteps = readTodaySteps()
            val hourlyDistribution = readHourlyStepsForToday()
            val nowTime = System.currentTimeMillis()
            val todayStr = LocalDate.now().format(dateFormatter)

            _lastSyncedSteps.value = todaySteps

            // Also read weekly steps from Health Connect
            val weeklySteps = readPastWeekDailySteps()
            val currentGoal = fitnessRepository.todayRecord.value.goal

            val weeklyRecords = weeklySteps.map { (date, count) ->
                DailyStepRecord(
                    date = date.format(dateFormatter),
                    stepCount = count,
                    goal = currentGoal,
                    source = StepSource.HEALTH_CONNECT.name,
                    lastSyncedAt = nowTime,
                    hourlyStepsString = if (date == LocalDate.now()) hourlyDistribution.joinToString(",") else ""
                )
            }
            fitnessRepository.updateWeeklyHistory(weeklyRecords)

            // Update fitness repository state & persist to Room database
            fitnessRepository.logHealthConnectSyncResult(
                steps = todaySteps,
                hourly = hourlyDistribution,
                timestamp = nowTime
            )

            // Update social challenges with newly synced steps
            competitionRepository?.updateMyChallengeSteps(todaySteps)

            _syncState.value = HealthSyncState.Success(
                stepCount = todaySteps,
                lastSyncedAt = nowTime
            )
            Log.i(TAG, "Health Connect sync succeeded! Synced $todaySteps steps.")
            isSyncing.set(false)
            true
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unknown error while syncing with Health Connect"
            Log.e(TAG, "Sync failed: $msg", e)
            _syncState.value = HealthSyncState.Error(msg)
            isSyncing.set(false)
            false
        }
    }

    /**
     * Starts periodic sync in background coroutine scope.
     */
    fun startBackgroundSync(intervalMillis: Long = 60_000L * 15L) {
        serviceScope.launch {
            while (true) {
                if (hasRequiredPermissions()) {
                    syncUserActivity()
                }
                kotlinx.coroutines.delay(intervalMillis)
            }
        }
    }

    companion object {
        private const val TAG = "HealthConnectSyncService"
    }
}

/**
 * Observable UI state representing Health Connect synchronization status.
 */
sealed class HealthSyncState {
    data class Idle(val lastSyncedAt: Long) : HealthSyncState()
    object Syncing : HealthSyncState()
    data class Success(val stepCount: Long, val lastSyncedAt: Long) : HealthSyncState()
    object PermissionRequired : HealthSyncState()
    data class HealthConnectUnavailable(val reason: String) : HealthSyncState()
    data class Error(val message: String) : HealthSyncState()
}
