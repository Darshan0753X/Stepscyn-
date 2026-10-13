package com.example.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "Darshan",
    val username: String = "@darshan_step",
    val avatarIndex: Int = 0,
    val dailyGoal: Long = 5000L,
    val currentStreak: Int = 4,
    val longestStreak: Int = 12,
    val totalSteps: Long = 184520L,
    val bestDaySteps: Long = 14320L,
    val connectedWatch: String = "Samsung Galaxy Fit3",
    val isDemoUser: Boolean = false
)

data class FriendProfile(
    val uid: String,
    val displayName: String,
    val username: String,
    val avatarIndex: Int,
    val todaySteps: Long,
    val dailyGoal: Long,
    val currentStreak: Int,
    val watchModel: String,
    val isRival: Boolean = false,
    val lastActive: String = "10m ago"
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean,
    val progress: Float, // 0.0f to 1.0f
    val unlockedDate: String? = null
)

data class SmartwatchSpec(
    val id: String,
    val brand: String,
    val model: String,
    val isDirectHealthConnect: Boolean,
    val companionAppName: String,
    val setupInstructions: List<String>,
    val note: String
)
