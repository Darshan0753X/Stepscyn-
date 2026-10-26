package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.AchievementItem
import com.example.data.model.Challenge
import com.example.data.model.ChallengeStatus
import com.example.data.model.ChallengeType
import com.example.data.model.FriendProfile
import com.example.data.model.LeaderboardEntry
import com.example.data.model.SmartwatchSpec
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialCompetitionRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val auth by lazy {
        try {
            com.example.auth.FirebaseHelper.initialize(context)
            Firebase.auth
        } catch (_: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            com.example.auth.FirebaseHelper.initialize(context)
            val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
            if (resId != 0) {
                FirebaseFirestore.getInstance(context.getString(resId))
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (_: Exception) {
            null
        }
    }

    private val _currentUserProfile = MutableStateFlow(
        UserProfile(
            uid = "user_me",
            displayName = "Darshan",
            username = "@darshan_step",
            avatarIndex = 0,
            dailyGoal = 5000L,
            currentStreak = 4,
            longestStreak = 12,
            totalSteps = 184520L,
            bestDaySteps = 14320L,
            connectedWatch = "Samsung Galaxy Fit3"
        )
    )
    val currentUserProfile: StateFlow<UserProfile> = _currentUserProfile.asStateFlow()

    private val _friends = MutableStateFlow(createInitialFriends())
    val friends: StateFlow<List<FriendProfile>> = _friends.asStateFlow()

    private val _challenges = MutableStateFlow(createInitialChallenges())
    val challenges: StateFlow<List<Challenge>> = _challenges.asStateFlow()

    private val _achievements = MutableStateFlow(createInitialAchievements())
    val achievements: StateFlow<List<AchievementItem>> = _achievements.asStateFlow()

    val supportedWatches = listOf(
        SmartwatchSpec(
            id = "galaxy_fit3",
            brand = "Samsung",
            model = "Galaxy Fit3",
            isDirectHealthConnect = true,
            companionAppName = "Samsung Health",
            setupInstructions = listOf(
                "Open Samsung Health app on your Android phone.",
                "Go to Settings > Health Connect.",
                "Turn on 'Allow all' or grant permissions to share Steps data.",
                "StepSync will automatically aggregate verified steps without double-counting."
            ),
            note = "Fully compatible via Samsung Health official Health Connect integration."
        ),
        SmartwatchSpec(
            id = "fastrack_sw2",
            brand = "Fastrack",
            model = "Reflex SW2",
            isDirectHealthConnect = false,
            companionAppName = "Fastrack Reflex World",
            setupInstructions = listOf(
                "Open Fastrack Reflex World (or Fastrack Smart World) app.",
                "In Settings / Profile, link your account to Google Fit.",
                "Google Fit syncs to Health Connect, or use StepSync's Manual Step Log fallback.",
                "StepSync distinguishes between automatic sync and manual audit records."
            ),
            note = "Fastrack companion app does not write directly to Health Connect yet; bridges through Google Fit or manual logging."
        ),
        SmartwatchSpec(
            id = "pixel_watch",
            brand = "Google",
            model = "Pixel Watch 1 / 2 / 3",
            isDirectHealthConnect = true,
            companionAppName = "Fitbit / Health Connect",
            setupInstructions = listOf(
                "Pixel Watch syncs directly with Fitbit and Health Connect.",
                "Ensure Health Connect step permissions are enabled in Android Settings."
            ),
            note = "Native Android Health Connect platform integration."
        )
    )

    fun createChallenge(
        title: String,
        description: String,
        type: ChallengeType,
        targetSteps: Long,
        rivalUid: String
    ): Challenge {
        val rival = _friends.value.find { it.uid == rivalUid } ?: _friends.value.first()
        val newChallenge = Challenge(
            id = "chal_${System.currentTimeMillis()}",
            title = title,
            description = description,
            creatorId = _currentUserProfile.value.uid,
            creatorName = _currentUserProfile.value.displayName,
            challengeType = type,
            targetSteps = targetSteps,
            startDate = "Today",
            endDate = if (type == ChallengeType.WEEKLY_TOTAL) "In 7 days" else "Tonight 23:59",
            status = ChallengeStatus.ACTIVE,
            participantIds = listOf(_currentUserProfile.value.uid, rival.uid),
            participantSteps = mapOf(
                _currentUserProfile.value.uid to 3850L,
                rival.uid to rival.todaySteps
            ),
            participantNames = mapOf(
                _currentUserProfile.value.uid to _currentUserProfile.value.displayName,
                rival.uid to rival.displayName
            ),
            isDemoChallenge = true
        )

        _challenges.value = listOf(newChallenge) + _challenges.value
        return newChallenge
    }

    fun updateMyChallengeSteps(todaySteps: Long) {
        val currentUserId = _currentUserProfile.value.uid
        val updated = _challenges.value.map { chal ->
            if (chal.status == ChallengeStatus.ACTIVE && chal.participantIds.contains(currentUserId)) {
                val newSteps = chal.participantSteps.toMutableMap()
                newSteps[currentUserId] = todaySteps
                chal.copy(participantSteps = newSteps)
            } else {
                chal
            }
        }
        _challenges.value = updated
    }

    fun getDailyLeaderboard(mySteps: Long): List<LeaderboardEntry> {
        val myProfile = _currentUserProfile.value
        val list = mutableListOf<LeaderboardEntry>()

        // Current user
        list.add(
            LeaderboardEntry(
                rank = 1,
                userId = myProfile.uid,
                displayName = myProfile.displayName + " (You)",
                avatarIndex = myProfile.avatarIndex,
                steps = mySteps,
                goal = myProfile.dailyGoal,
                streak = myProfile.currentStreak,
                isCurrentUser = true
            )
        )

        // Friends
        for (friend in _friends.value) {
            list.add(
                LeaderboardEntry(
                    rank = 1,
                    userId = friend.uid,
                    displayName = friend.displayName,
                    avatarIndex = friend.avatarIndex,
                    steps = friend.todaySteps,
                    goal = friend.dailyGoal,
                    streak = friend.currentStreak,
                    isCurrentUser = false,
                    lastSyncedTimeText = friend.lastActive
                )
            )
        }

        // Sort by steps descending with tie breaking (identical steps tie)
        list.sortByDescending { it.steps }
        val leaderSteps = list.firstOrNull()?.steps ?: 0L

        return list.mapIndexed { index, entry ->
            entry.copy(
                rank = index + 1,
                deltaToLeader = (leaderSteps - entry.steps).coerceAtLeast(0L)
            )
        }
    }

    fun updateProfile(displayName: String, dailyGoal: Long, avatarIndex: Int) {
        val updated = _currentUserProfile.value.copy(
            displayName = displayName,
            dailyGoal = dailyGoal,
            avatarIndex = avatarIndex
        )
        _currentUserProfile.value = updated

        // Sync to cloud if user is signed in with Google
        val currentUid = auth?.currentUser?.uid
        val fs = firestore
        if (currentUid != null && fs != null) {
            scope.launch {
                try {
                    fs.collection("users").document(currentUid).set(
                        mapOf(
                            "userId" to currentUid,
                            "displayName" to displayName,
                            "dailyGoal" to dailyGoal,
                            "avatarIndex" to avatarIndex,
                            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )
                } catch (_: Exception) {}
            }
        }
    }

    private fun createInitialFriends(): List<FriendProfile> {
        return listOf(
            FriendProfile(
                uid = "friend_alex",
                displayName = "Alex Carter",
                username = "@alex_fit",
                avatarIndex = 1,
                todaySteps = 4200L,
                dailyGoal = 5000L,
                currentStreak = 6,
                watchModel = "Fastrack Reflex SW2",
                isRival = true,
                lastActive = "5m ago"
            ),
            FriendProfile(
                uid = "friend_priya",
                displayName = "Priya Sharma",
                username = "@priya_runs",
                avatarIndex = 2,
                todaySteps = 6350L,
                dailyGoal = 6000L,
                currentStreak = 14,
                watchModel = "Samsung Galaxy Fit3",
                isRival = false,
                lastActive = "Just now"
            ),
            FriendProfile(
                uid = "friend_marcus",
                displayName = "Marcus Vance",
                username = "@marcus_v",
                avatarIndex = 3,
                todaySteps = 2900L,
                dailyGoal = 5000L,
                currentStreak = 2,
                watchModel = "Google Pixel Watch 2",
                isRival = false,
                lastActive = "32m ago"
            )
        )
    }

    private fun createInitialChallenges(): List<Challenge> {
        return listOf(
            Challenge(
                id = "chal_duel_1",
                title = "Daily 5K Showdown",
                description = "Head-to-head daily step sprint! First to reach 5,000 steps claims the title.",
                creatorId = "user_me",
                creatorName = "Darshan",
                challengeType = ChallengeType.ONE_ON_ONE_DUEL,
                targetSteps = 5000L,
                startDate = "Today",
                endDate = "Tonight 23:59",
                status = ChallengeStatus.ACTIVE,
                participantIds = listOf("user_me", "friend_alex"),
                participantSteps = mapOf(
                    "user_me" to 3850L,
                    "friend_alex" to 4200L
                ),
                participantNames = mapOf(
                    "user_me" to "Darshan",
                    "friend_alex" to "Alex Carter"
                ),
                isDemoChallenge = true
            ),
            Challenge(
                id = "chal_weekly_1",
                title = "Weekend Warriors 35K",
                description = "Cumulative steps race across 7 days. Consistency is key!",
                creatorId = "friend_priya",
                creatorName = "Priya Sharma",
                challengeType = ChallengeType.WEEKLY_TOTAL,
                targetSteps = 35000L,
                startDate = "Monday",
                endDate = "Sunday 23:59",
                status = ChallengeStatus.ACTIVE,
                participantIds = listOf("user_me", "friend_alex", "friend_priya"),
                participantSteps = mapOf(
                    "user_me" to 24100L,
                    "friend_alex" to 22800L,
                    "friend_priya" to 27500L
                ),
                participantNames = mapOf(
                    "user_me" to "Darshan",
                    "friend_alex" to "Alex Carter",
                    "friend_priya" to "Priya Sharma"
                ),
                isDemoChallenge = true
            )
        )
    }

    private fun createInitialAchievements(): List<AchievementItem> {
        return listOf(
            AchievementItem(
                id = "first_5k",
                title = "First 5K",
                description = "Reach 5,000 steps in a single day.",
                iconName = "flag",
                isUnlocked = true,
                progress = 1.0f,
                unlockedDate = "Oct 10, 2026"
            ),
            AchievementItem(
                id = "three_day_streak",
                title = "Three-Day Streak",
                description = "Hit your daily goal for 3 consecutive days.",
                iconName = "flame",
                isUnlocked = true,
                progress = 1.0f,
                unlockedDate = "Oct 12, 2026"
            ),
            AchievementItem(
                id = "weekly_champion",
                title = "Weekly Champion",
                description = "Win a weekly challenge against friends.",
                iconName = "trophy",
                isUnlocked = false,
                progress = 0.65f
            ),
            AchievementItem(
                id = "consistency_master",
                title = "Consistency Master",
                description = "Hit your goal on 7 consecutive days.",
                iconName = "bolt",
                isUnlocked = false,
                progress = 0.57f
            ),
            AchievementItem(
                id = "friendly_rival",
                title = "Friendly Rival",
                description = "Complete your first head-to-head showdown with a friend.",
                iconName = "swords",
                isUnlocked = true,
                progress = 1.0f,
                unlockedDate = "Oct 11, 2026"
            ),
            AchievementItem(
                id = "step_legend",
                title = "Step Legend",
                description = "Record over 250,000 total lifetime steps.",
                iconName = "crown",
                isUnlocked = false,
                progress = 0.73f
            )
        )
    }
}
