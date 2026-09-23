package com.lapoushko.feature_experiment.impl.data

import com.lapoushko.core_common.DispatcherProvider
import com.lapoushko.core_network.RetrofitServiceFactory
import com.lapoushko.database.ExperimentDao
import com.lapoushko.database.ExperimentEventEntity
import com.lapoushko.database.ExperimentSessionEntity
import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentRepository
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExperimentRepositoryImpl @Inject constructor(
    private val dao: ExperimentDao,
    private val retrofitServiceFactory: RetrofitServiceFactory,
    private val wifiSignalReader: WifiSignalReader,
    private val dispatchers: DispatcherProvider
) : ExperimentRepository {

    override fun observeSessions(): Flow<List<ExperimentSession>> =
        dao.observeSessions().map { entities -> entities.map { it.toDomain() } }

    override fun observeEvents(sessionId: Long): Flow<List<ExperimentEvent>> =
        dao.observeEvents(sessionId).map { entities -> entities.mapNotNull { it.toDomain() } }

    override suspend fun startSession(title: String): Long = withContext(dispatchers.io) {
        dao.insertSession(
            ExperimentSessionEntity(
                title = title.ifBlank { "Эксперимент" },
                startedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    override suspend fun endSession(sessionId: Long) = withContext(dispatchers.io) {
        dao.endSession(sessionId, System.currentTimeMillis())
    }

    override suspend fun deleteSession(sessionId: Long) = withContext(dispatchers.io) {
        dao.deleteEventsForSession(sessionId)
        dao.deleteSession(sessionId)
    }

    override suspend fun recordCheckpoint(sessionId: Long, label: String, distanceMeters: Double?) =
        withContext(dispatchers.io) {
            dao.insertEvent(
                ExperimentEventEntity(
                    sessionId = sessionId,
                    timestampEpochMillis = System.currentTimeMillis(),
                    kind = ExperimentEventEntity.KIND_CHECKPOINT,
                    label = label,
                    rssiDbm = wifiSignalReader.currentRssiDbm(),
                    distanceMeters = distanceMeters
                )
            )
        }

    override fun runPingLoop(sessionId: Long): Flow<Unit> = flow {
        while (currentCoroutineContext().isActive) {
            val startedAt = System.currentTimeMillis()
            val success = runCatching {
                retrofitServiceFactory.create(ExperimentPingApiService::class.java).ping().isSuccessful
            }.getOrDefault(false)
            val rtt = System.currentTimeMillis() - startedAt

            dao.insertEvent(
                ExperimentEventEntity(
                    sessionId = sessionId,
                    timestampEpochMillis = startedAt,
                    kind = ExperimentEventEntity.KIND_PING,
                    success = success,
                    rttMillis = rtt,
                    rssiDbm = wifiSignalReader.currentRssiDbm()
                )
            )
            emit(Unit)
            delay(PING_INTERVAL_MS)
        }
    }.flowOn(dispatchers.io)

    override fun currentRssiDbm(): Int? = wifiSignalReader.currentRssiDbm()

    private companion object {
        const val PING_INTERVAL_MS = 700L
    }
}
