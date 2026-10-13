package com.example.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {

    val healthConnectClient by lazy {
        try {
            HealthConnectClient.getOrCreate(context)
        } catch (_: Exception) {
            null
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )

    fun checkAvailability(): AvailabilityStatus {
        val status = HealthConnectClient.getSdkStatus(context)
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> AvailabilityStatus.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> AvailabilityStatus.UPDATE_REQUIRED
            else -> AvailabilityStatus.UNAVAILABLE
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        val client = healthConnectClient ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.contains(HealthPermission.getReadPermission(StepsRecord::class))
        } catch (_: Exception) {
            false
        }
    }

    suspend fun readTodaySteps(): Long {
        val client = healthConnectClient ?: return 0L
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val startOfDay = today.atStartOfDay(zoneId).toInstant()
        val now = Instant.now()

        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    suspend fun readHourlyStepsForToday(): List<Int> {
        val client = healthConnectClient ?: return List(24) { 0 }
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
        } catch (_: Exception) {
            // Safe fallback if raw reading is restricted
        }

        return hourlyList
    }

    suspend fun readStepsForDate(date: LocalDate): Long {
        val client = healthConnectClient ?: return 0L
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()

        return try {
            val response = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
                )
            )
            response[StepsRecord.COUNT_TOTAL] ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    suspend fun insertManualStepsRecord(steps: Long, localDate: LocalDate): Boolean {
        val client = healthConnectClient ?: return false
        val zoneId = ZoneId.systemDefault()
        val startTime = localDate.atTime(12, 0).atZone(zoneId).toInstant()
        val endTime = startTime.plus(30, ChronoUnit.MINUTES)
        val zoneOffset = ZoneOffset.systemDefault().rules.getOffset(startTime)

        return try {
            val record = StepsRecord(
                count = steps,
                startTime = startTime,
                endTime = endTime,
                startZoneOffset = zoneOffset,
                endZoneOffset = zoneOffset
            )
            client.insertRecords(listOf(record))
            true
        } catch (_: Exception) {
            false
        }
    }

    enum class AvailabilityStatus {
        AVAILABLE,
        UPDATE_REQUIRED,
        UNAVAILABLE
    }
}
