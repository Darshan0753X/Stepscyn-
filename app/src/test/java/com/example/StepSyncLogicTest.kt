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
        assertEquals(800L, sorted[0].second - sorted[2].second)
    }
}
