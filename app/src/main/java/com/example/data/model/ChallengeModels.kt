package com.example.data.model

enum class ChallengeType {
    DAILY_SHOWDOWN,      // 1-day 1-on-1 battle
    ONE_ON_ONE_DUEL,     // Head to head direct race
    WEEKLY_TOTAL,        // 7-day cumulative steps
    FIRST_TO_GOAL,       // First player to reach target
    STREAK_7DAY          // 7 consecutive days reaching goal
}

enum class ChallengeStatus {
    UPCOMING,
    ACTIVE,
    COMPLETED
}

data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val challengeType: ChallengeType = ChallengeType.ONE_ON_ONE_DUEL,
    val targetSteps: Long = 5000L,
    val startDate: String = "",
    val endDate: String = "",
    val status: ChallengeStatus = ChallengeStatus.ACTIVE,
    val participantIds: List<String> = emptyList(),
    val participantSteps: Map<String, Long> = emptyMap(),
    val participantNames: Map<String, String> = emptyMap(),
    val winnerId: String? = null,
    val isDemoChallenge: Boolean = false
) {
    fun getLeader(): Pair<String, Long>? {
        if (participantSteps.isEmpty()) return null
        val top = participantSteps.maxByOrNull { it.value } ?: return null
        return Pair(top.key, top.value)
    }

    fun getDelta(): Long {
        if (participantSteps.size < 2) return 0L
        val sorted = participantSteps.values.sortedDescending()
        return sorted[0] - sorted[1]
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarIndex: Int,
    val steps: Long,
    val goal: Long,
    val streak: Int,
    val isCurrentUser: Boolean = false,
    val deltaToLeader: Long = 0L,
    val isStale: Boolean = false,
    val lastSyncedTimeText: String = "Just now"
)
