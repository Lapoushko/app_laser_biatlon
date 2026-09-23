package com.lapoushko.feature_experiment.api.domain

import kotlinx.coroutines.flow.Flow

interface ExperimentRepository {
    fun observeSessions(): Flow<List<ExperimentSession>>
    fun observeEvents(sessionId: Long): Flow<List<ExperimentEvent>>

    suspend fun startSession(title: String): Long
    suspend fun endSession(sessionId: Long)
    suspend fun deleteSession(sessionId: Long)
    suspend fun recordCheckpoint(sessionId: Long, label: String, distanceMeters: Double?)

    /**
     * Пока кто-то подписан на этот Flow — раз в PING_INTERVAL опрашивает сервер и пишет
     * результат (успех/ошибка, RTT, RSSI) как событие сессии. Отмена подписки останавливает пинги.
     */
    fun runPingLoop(sessionId: Long): Flow<Unit>

    /** Текущий уровень Wi-Fi сигнала телефона, дБм, или null если нет разрешения/подключения. */
    fun currentRssiDbm(): Int?
}
