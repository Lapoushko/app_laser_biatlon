package com.lapoushko.feature_statistics.api.domain

enum class GameMode { SINGLE, TEAM }

data class TargetStatus(
    val id: Int,
    val batteryCharge: Int,
    val hitsCount: Int,
    /** ФИО участника -> число его попаданий в эту мишень. */
    val players: Map<String, Int>
)

data class MatchStatistics(
    val gameMode: GameMode,
    val isInfiniteMode: Boolean,
    val durationSeconds: Int,
    val remainingSeconds: Int,
    val startTimeIso: String,
    val participants: List<Participant>,
    val targets: List<TargetStatus>,
    val isStale: Boolean = false
)
