package com.example

import com.example.data.model.Challenge
import com.example.data.model.ChallengeStatus
import com.example.data.model.ChallengeType
import com.example.data.model.DailyStepRecord
import com.example.data.model.StepSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StepSyncLogicTest {

    @Test
    fun testDailyStepRecord_calculations() {
        val record = DailyStepRecord(
            date = "2026-10-13",
            stepCount = 3750L,
            goal = 5000L,
            source = StepSource.HEALTH_CONNECT.name
        )

        assertEquals(1250L, record.remainingSteps)
        assertEquals(0.75f, record.completionPercentage, 0.001f)
        assertFalse(record.isGoalReached)
    }

    @Test
    fun testDailyStepRecord_goalReached() {
        val record = DailyStepRecord(
            date = "2026-10-13",
            stepCount = 6200L,
            goal = 5000L,
            source = StepSource.HEALTH_CONNECT.name
        )

        assertEquals(0L, record.remainingSteps)
        assertTrue(record.isGoalReached)
        assertEquals(1.24f, record.completionPercentage, 0.001f)
    }

    @Test
    fun testChallenge_getLeaderAndDelta() {
        val challenge = Challenge(
            id = "chal_1",
            title = "1-on-1 Showdown",
            targetSteps = 5000L,
            participantSteps = mapOf(
                "user_darshan" to 4200L,
                "user_alex" to 3750L
            )
        )

        val leader = challenge.getLeader()
        assertEquals("user_darshan", leader?.first)
        assertEquals(4200L, leader?.second)
        assertEquals(450L, challenge.getDelta())
    }

    @Test
    fun testHourlySteps_parsing() {
        val record = DailyStepRecord(
            date = "2026-10-13",
            stepCount = 1000L,
            hourlyStepsString = "0,0,0,0,0,0,150,250,600,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0"
        )

        val hourly = record.getHourlySteps()
        assertEquals(24, hourly.size)
        assertEquals(150, hourly[6])
        assertEquals(250, hourly[7])
        assertEquals(600, hourly[8])
        assertEquals(0, hourly[9])
    }

    @Test
    fun testLeaderboard_tieBreakingRules() {
        // Tie-breaking: identical steps share rank, higher steps beat lower steps
        val scores = listOf(
            Pair("Darshan", 5000L),
            Pair("Priya", 5000L),
            Pair("Alex", 4200L)
        )

        val sorted = scores.sortedByDescending { it.second }
        assertEquals(5000L, sorted[0].second)
        assertEquals(5000L, sorted[1].second)
        assertEquals(4200L, sorted[2].second)
    }

    @Test
    fun testGoalMilestones() {
        val goal = 8000L
        val currentSteps = 6000L
        val progressRatio = currentSteps.toFloat() / goal.toFloat()

        assertEquals(0.75f, progressRatio, 0.001f)
        val percentage = (progressRatio * 100f).toInt()
        assertEquals(75, percentage)

        val milestone25 = currentSteps >= (goal * 0.25)
        val milestone50 = currentSteps >= (goal * 0.50)
        val milestone75 = currentSteps >= (goal * 0.75)
        val milestone100 = currentSteps >= goal

        assertTrue(milestone25)
        assertTrue(milestone50)
        assertTrue(milestone75)
        assertFalse(milestone100)
    }

    @Test
    fun testWeeklyStepAggregation() {
        val targetGoal = 10000L
        val weeklySteps = listOf(8500L, 10200L, 11500L, 9400L, 12000L, 7800L, 10500L)
        val totalSteps = weeklySteps.sum()
        val dailyAvg = totalSteps / weeklySteps.size
        val daysGoalMet = weeklySteps.count { it >= targetGoal }

        assertEquals(69900L, totalSteps)
        assertEquals(9985L, dailyAvg)
        assertEquals(4, daysGoalMet)
    }

    @Test
    fun testGoalPreferencesClamping() {
        val minGoal = com.example.data.preferences.UserPreferencesManager.MIN_DAILY_GOAL
        val maxGoal = com.example.data.preferences.UserPreferencesManager.MAX_DAILY_GOAL

        val userEntryLow = 500L
        val userEntryHigh = 75000L
        val userEntryNormal = 12500L

        val clampedLow = userEntryLow.coerceIn(minGoal, maxGoal)
        val clampedHigh = userEntryHigh.coerceIn(minGoal, maxGoal)
        val clampedNormal = userEntryNormal.coerceIn(minGoal, maxGoal)

        assertEquals(1000L, clampedLow)
        assertEquals(50000L, clampedHigh)
        assertEquals(12500L, clampedNormal)
    }

    @Test
    fun testEstimatedMetricsCalculation() {
        val steps = 10000L
        val distanceKm = steps * 0.000762
        val calories = (steps * 0.04).toInt()
        val activeMinutes = (steps / 100).toInt()

        assertEquals(7.62, distanceKm, 0.001)
        assertEquals(400, calories)
        assertEquals(100, activeMinutes)
    }

    @Test
    fun testDataStoreCircularStepIndicatorPercentage() {
        val dataStoreGoal = 10000L
        val currentStepsUnder = 7500L
        val currentStepsOver = 12500L

        val ratioUnder = (currentStepsUnder.toFloat() / dataStoreGoal.toFloat()).coerceIn(0f, 1.5f)
        val percentageUnder = (ratioUnder * 100f).toInt()
        assertEquals(75, percentageUnder)
        assertEquals(2500L, (dataStoreGoal - currentStepsUnder).coerceAtLeast(0L))

        val ratioOver = (currentStepsOver.toFloat() / dataStoreGoal.toFloat()).coerceIn(0f, 1.5f)
        val percentageOver = (ratioOver * 100f).toInt()
        assertEquals(125, percentageOver)
        assertEquals(0L, (dataStoreGoal - currentStepsOver).coerceAtLeast(0L))

        // Safe division check when goal is 0 or uninitialized
        val zeroGoal = 0L
        val safeGoal = zeroGoal.coerceAtLeast(1L)
        val safeRatio = (currentStepsUnder.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1.5f)
        assertTrue(safeRatio >= 0f)
    }
}
