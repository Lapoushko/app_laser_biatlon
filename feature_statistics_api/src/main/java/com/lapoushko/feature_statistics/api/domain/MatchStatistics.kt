package com.lapoushko.feature_statistics.api.domain

import java.time.Duration
import java.time.LocalDateTime

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
    /**
     * Сервер не ведёт живой обратный отсчёт — это поле обновляется только при явной остановке
     * матча и может быть "хвостом" от предыдущего матча. Для отображения используй
     * [liveRemainingSeconds], который считает оставшееся время сам от [startTimeIso].
     */
    val remainingSeconds: Int,
    val startTimeIso: String,
    val participants: List<Participant>,
    val targets: List<TargetStatus>,
    val isStale: Boolean = false
) {
    val liveRemainingSeconds: Int
        get() {
            val elapsed = elapsedSecondsSinceStart()
                ?: return remainingSeconds.coerceIn(0, durationSeconds.coerceAtLeast(0))
            return (durationSeconds - elapsed).coerceIn(0, durationSeconds.coerceAtLeast(0))
        }

    private fun elapsedSecondsSinceStart(): Int? = runCatching {
        val startedAt = LocalDateTime.parse(startTimeIso)
        Duration.between(startedAt, LocalDateTime.now()).seconds.toInt()
    }.getOrNull()
}
