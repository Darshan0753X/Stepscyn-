# StepSync: Premium Smartwatch Fitness Competition App

StepSync is a native Android fitness competition application designed to connect friends using different smartwatch ecosystems (including Samsung Galaxy Fit3 and Fastrack Reflex SW2). The app pairs Health Connect data aggregation with interactive head-to-head showdowns, private group leaderboards, streak tracking, offline Room caching, and secure Firebase synchronization.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices have been confirmed and incorporated into the blueprint:

- **Fitness Data & Smartwatch Sync**: Official Health Connect integration paired with an interactive Demo Mode. Health Connect provides verified, deduplicated step aggregation for compatible watches like the Samsung Galaxy Fit3 (via Samsung Health), while providing clear connection guidance, Google Fit bridge troubleshooting, and an honest manual-entry fallback for watches like the Fastrack Reflex SW2 that lack direct Health Connect drivers.
- **Social Competition & Challenge Format**: Emphasizes 1-on-1 head-to-head showdowns alongside private group leaderboards, supporting customizable goal targets, daily/weekly showdowns, and streak challenges with strict tie-breaking logic.
- **Backend & Persistence Architecture**: Google Sign-In via Jetpack `CredentialManager` backed by Cloud Firestore and robust local Room database caching. All user data is available offline, and cloud updates are authenticated and isolated under per-user security rules.

---

## 1. Overview & Core Concept

- **What It Does**: StepSync bridges the gap between fragmented smartwatch companion apps. Friends with different wearable brands can compete fairly in private daily and weekly step duels, monitor live progress through circular activity gauges, participate in group leaderboards, and celebrate consistency with unlockable achievement badges.
- **Target Audience / Persona**: Fitness rivals, workout buddies, and running clubs who want friendly social motivation without being locked into a single proprietary wearable hardware ecosystem.
- **Key Value**: Fair, transparent fitness data aggregation with zero fabricated records, seamless offline operation, and engaging gamification.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Onboarding & Permissions**:
   - Welcome Screen ("Make Every Step Count").
   - Smartwatch Selection & Guidance (Samsung Galaxy Fit3 setup guide, Fastrack Reflex Google Fit bridge & manual mode guidance).
   - Health Connect Permission Request with clear privacy rationale.
   - Profile Setup (Display name, avatar icon, daily step goal).
   - Mode Selection: Production Account or Interactive Demo Mode.

2. **Home Dashboard**:
   - **Hero Activity Section**: Dynamic circular step progress ring with live count-up animation, current steps, daily goal (default 5,000 steps), percentage, and remaining steps.
   - **Head-to-Head Showdown Duel Card**: Visual duel card displaying "Your Steps" vs "Rival's Steps", live step delta, and winning pace indicator.
   - **Hourly Breakdown & 7-Day Consistency**: Bar chart displaying today's hourly step distribution and 7-day completion streak.
   - **Data Source Banner**: Clear status indicator showing whether data is live from Health Connect, cached from Room, or labeled Demo Mode.

3. **Challenges & Showdowns**:
   - Active, Upcoming, and Completed challenge tabs.
   - Challenge Creator: 1-on-1 duel vs group league, daily goal showdown, 7-day endurance, or first-to-goal race.
   - Challenge Detail: Participant cards with live step comparisons, start/end countdowns, and completion celebrations.

4. **Leaderboards**:
   - Daily, Weekly, and All-Time private group rankings.
   - Highlighted user row with rank delta and gap to leader.
   - Data freshness indicators for every competitor.

5. **Friends & Private Groups**:
   - Unique 6-character group invite codes.
   - Friend management, pending invitations, and quick 1-on-1 duel triggers.

6. **Watch & Fitness Data Hub**:
   - Health Connect connectivity diagnostic test.
   - Step sync history, last sync timestamp, and manual sync refresh button.
   - Specific setup guides for Samsung Galaxy Fit3 (Samsung Health -> Health Connect) and Fastrack Reflex SW2 (Fastrack World -> Google Fit bridge).
   - Clearly labeled manual log entry with audit flag to preserve leaderboard integrity.

7. **Profile, Stats & Achievements**:
   - Lifetime stats: Total steps, best day, current streak, longest streak, goal completion rate.
   - 30-day interactive activity trend chart.
   - Achievement Gallery: First 5K, 3-Day Streak, Weekly Champion, Consistency Master, Step Legend.
   - Dark/Light theme toggle and Google Sign-Out / Account Deletion.

### Visual Identity & Theme

- **Aesthetic Direction**: High-performance dark sports luxury with energetic athletic accents.
- **Color Palette**:
  - `Background`: Deep Night (`#0B0D12`)
  - `Surface`: Midnight Surface (`#121620`)
  - `Surface Elevated`: Obsidian Card (`#191F2B`)
  - `Primary Accent`: Cyber Lime (`#B8FF65`)
  - `Secondary Accent`: Electric Teal (`#72E7D2`)
  - `Text Primary`: Pure Ice (`#F5F7FA`)
  - `Text Secondary`: Muted Slate (`#A3ADBD`)
  - `Success`: Mint Green (`#65E6A6`)
  - `Warning`: Solar Amber (`#FFC86B`)
  - `Error / Rival`: Crimson Coral (`#FF747F`)
- **Typography & Hierarchy**:
  - Athletic numeric display styling with tabular figures (`tnum`) for animated step numbers to prevent layout jitter.
  - Crisp M3 typography hierarchy with high-contrast labels.
- **Component Styling**:
  - 16dp rounded cards with subtle 1dp borders (`#232B3C`).
  - Circular progress gauges with dual-tone gradient stroke.
  - Floating bottom navigation with active pill indicators and safe-area insets.
- **Motion & Accessibility**:
  - Spring-based progress ring animations and count-up interpolations.
  - Minimum 48dp touch targets on all interactive buttons.
  - Full support for Android edge-to-edge layout and TalkBack content descriptions.

---

## 3. Key Product Decisions & Trade-Offs

### 1. Smartwatch Integration Architecture
- **Decision**: Health Connect SDK as the central fitness engine, supplemented by explicit device-specific pairing guides and a labeled manual fallback.
- **Why**: Samsung Galaxy Fit3 officially syncs steps to Samsung Health, which writes directly to Health Connect. Fastrack Reflex SW2 companion apps sync to Google Fit but lack native Health Connect drivers. Health Connect avoids unstable reverse-engineered Bluetooth protocols, prevents battery drain, and respects Android security.
- **Alternatives Considered**: Proprietary Bluetooth GATT scraping was rejected due to stability risks, lack of public APIs, and frequent protocol changes across firmware updates.

### 2. Dual-Layer Persistence (Room + Firestore)
- **Decision**: Room for instant local-first offline storage and fast UI reads; Cloud Firestore for cross-user sync, private challenges, and shared leaderboards.
- **Why**: Fitness tracking requires zero latency on launch even in gym basements or during flight mode. Firestore syncs when online; Room guarantees cached dashboards are always available.

### 3. Data Integrity & Verification Flags
- **Decision**: Each step record carries a `sourceType` (`HEALTH_CONNECT`, `MANUAL_ENTRY`, `DEMO_SIMULATION`).
- **Why**: Protects social trust. In leaderboards and challenges, verified Health Connect records are distinguished from manual logs, and demo records never leave the local client.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│  ┌───────────────┐ ┌────────────────┐ ┌─────────────┐ ┌─────────────┐  │
│  │ HomeDashboard │ │ ChallengesView │ │ Leaderboard │ │ ProfileView │  │
│  └───────┬───────┘ └───────┬────────┘ └──────┬──────┘ └──────┬──────┘  │
│          └─────────────────┼─────────────────┼───────────────┘         │
└────────────────────────────┼─────────────────┼─────────────────────────┘
                             ▼                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                           UI State ViewModels                          │
│   DashboardViewModel   │   ChallengeViewModel   │   AuthViewModel     │
└────────────────────────────┬─────────────────┬─────────────────────────┘
                             ▼                 ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Domain & Repository Layer                       │
│                                                                        │
│   ┌───────────────────────────┐      ┌──────────────────────────────┐  │
│   │   FitnessDataRepository   │      │    CompetitionRepository     │  │
│   └─────┬───────────────┬─────┘      └──────┬────────────────┬──────┘  │
└─────────┼───────────────┼───────────────────┼────────────────┼─────────┘
          ▼               ▼                   ▼                ▼
┌──────────────────┐ ┌──────────────┐  ┌───────────────┐ ┌──────────────┐
│  Health Connect  │ │  Local Room  │  │ Cloud Fire-   │ │ Firebase     │
│  Android Client  │ │  Database    │  │ store Backend │ │ Auth (Google)│
│  (Aggregated)    │ │  (Offline)   │  │ (Multi-User)  │ │ (CredMgr)    │
└──────────────────┘ └──────────────┘  └───────────────┘ └──────────────┘
```

### Data Models

```kotlin
// Room Entity & Domain Model: Daily Activity
data class DailyStepRecord(
    val date: String,             // "YYYY-MM-DD"
    val stepCount: Long,
    val goal: Long = 5000L,
    val source: StepSource,       // HEALTH_CONNECT, MANUAL, DEMO
    val lastSyncedAt: Long,
    val hourlyDistribution: List<Int> = emptyList()
)

// Firestore & Domain Model: Challenge
data class Challenge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val creatorId: String = "",
    val participantIds: List<String> = emptyList(),
    val challengeType: ChallengeType, // DAILY_GOAL, ONE_ON_ONE_DUEL, WEEKLY_TOTAL
    val targetSteps: Long = 5000L,
    val startDate: String = "",
    val endDate: String = "",
    val status: ChallengeStatus,      // UPCOMING, ACTIVE, COMPLETED
    val participantSteps: Map<String, Long> = emptyMap()
)

// Firestore & Domain Model: User Profile
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val username: String = "",
    val avatarUrl: String = "",
    val dailyGoal: Long = 5000L,
    val currentStreak: Int = 0,
    val totalSteps: Long = 0L,
    val isDemoUser: Boolean = false
)
```

### Component & State Mapping

- **`DashboardViewModel`**: Observes `FitnessDataRepository.todayStepsFlow` (backed by Room and Health Connect aggregation). Combines with `CompetitionRepository.activeDuelFlow` to compute the live rival delta and goal progress.
- **`ChallengeViewModel`**: Manages challenge creation, invitation acceptance, and calculates ranking order with tie-breaking rules (higher step total wins; identical totals tie).
- **`HealthConnectManager`**: Evaluates platform SDK availability (`SDK_AVAILABLE`, `SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED`, `SDK_UNAVAILABLE`), queries aggregated steps via `AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), TimeRangeFilter.between(...))` to eliminate duplicate counting across sensors, and safely falls back when permissions are missing.
- **`AuthViewModel`**: Implements Google Sign-In with Jetpack `CredentialManager`, manages session tokens, and toggles between live authenticated cloud state and isolated local demo mode.

---

## 5. Implementation Stages & Build Verification

1. **Step 1: Configuration & Platform Setup**:
   - Update `metadata.json` with the app name "StepSync" and description.
   - Configure Gradle dependencies: enable Firestore, Auth, Credential Manager, and Health Connect SDK in `app/build.gradle.kts`.
   - Update `AndroidManifest.xml` with Health Connect permissions, intent-filter hooks, and rationale activities.

2. **Step 2: Database & Backend Scaffolding**:
   - Provision Firestore via `set_up_firebase`.
   - Author `firebase-blueprint.json` and strict `firestore.rules` enforcing user-isolated write permissions and schema validation.
   - Implement local Room database entities, DAOs, and type converters for offline caching.

3. **Step 3: Fitness & Health Connect Data Layer**:
   - Build `HealthConnectManager` and `FitnessDataRepository` supporting aggregated steps and date-boundary queries.
   - Implement smartwatch setup guidance and manual logging fallback.
   - Implement `DemoModeManager` providing realistic sample rival data for testing without physical watches.

4. **Step 4: Jetpack Compose UI & Visual Design**:
   - Create custom M3 theme (`Theme.kt`, `Color.kt`, `Type.kt`) adhering to the dark athletic luxury aesthetic.
   - Implement reusable widgets: `CircularStepRing`, `HeadToHeadCard`, `ActivityBarChart`, `LeaderboardRow`, `AchievementBadge`.
   - Build five primary destinations: `HomeScreen`, `ChallengesScreen`, `LeaderboardScreen`, `FriendsScreen`, `ProfileScreen`, plus `WatchSyncScreen` and `OnboardingScreen`.

5. **Step 5: Verification & Testing**:
   - Write Robolectric and JUnit unit tests for daily step aggregation, streak tracking, challenge scoring, and tie-breaking.
   - Verify build compilation via `compile_applet`.
