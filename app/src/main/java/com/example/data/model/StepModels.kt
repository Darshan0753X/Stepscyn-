package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StepSource {
    HEALTH_CONNECT,
    MANUAL_ENTRY,
    DEMO_SIMULATION
}

@Entity(tableName = "daily_steps")
data class DailyStepRecord(
    @PrimaryKey
    val date: String,             // Format: "YYYY-MM-DD"
    val stepCount: Long = 0L,
    val goal: Long = 5000L,
    val source: String = StepSource.DEMO_SIMULATION.name,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val hourlyStepsString: String = "", // Comma-separated 24 hourly values
    val isSyncedToCloud: Boolean = false
) {
    val completionPercentage: Float
        get() = if (goal > 0) (stepCount.toFloat() / goal.toFloat()).coerceIn(0f, 2f) else 0f

    val remainingSteps: Long
        get() = (goal - stepCount).coerceAtLeast(0L)

    val isGoalReached: Boolean
        get() = stepCount >= goal

    fun getHourlySteps(): List<Int> {
        if (hourlyStepsString.isBlank()) return List(24) { 0 }
        return try {
            hourlyStepsString.split(",").map { it.trim().toIntOrNull() ?: 0 }
        } catch (_: Exception) {
            List(24) { 0 }
        }
    }
}

data class SyncStatus(
    val isConnected: Boolean = true,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val source: StepSource = StepSource.DEMO_SIMULATION,
    val activeWatchName: String = "Samsung Galaxy Fit3 (Demo)",
    val errorMessage: String? = null
)
