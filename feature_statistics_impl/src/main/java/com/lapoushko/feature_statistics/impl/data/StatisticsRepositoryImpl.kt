package com.lapoushko.feature_statistics.impl.data

import com.lapoushko.core_common.DispatcherProvider
import com.lapoushko.core_network.RetrofitServiceFactory
import com.lapoushko.database.CachedStatisticsDao
import com.lapoushko.database.CachedStatisticsEntity
import com.lapoushko.feature_statistics.api.domain.MatchStatistics
import com.lapoushko.feature_statistics.api.domain.StatisticsRepository
import com.lapoushko.feature_statistics.impl.data.dto.MatchDataDto
import com.lapoushko.feature_statistics.impl.data.dto.toDomain
import com.lapoushko.feature_statistics.impl.data.dto.toDto
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl @Inject constructor(
    private val retrofitServiceFactory: RetrofitServiceFactory,
    private val cachedStatisticsDao: CachedStatisticsDao,
    private val dispatchers: DispatcherProvider
) : StatisticsRepository {

    override fun observeMatchStatistics(): Flow<MatchStatistics> = flow {
        while (currentCoroutineContext().isActive) {
            val fetched = runCatching {
                val response = retrofitServiceFactory.create(StatisticsApiService::class.java).getMatchData()
                val data = response.data ?: error(response.error ?: "Матч ещё не запускался")
                data.toDomain()
            }

            fetched.onSuccess { statistics ->
                cachedStatisticsDao.save(
                    CachedStatisticsEntity(
                        payloadJson = Json.encodeToString(MatchDataDto.serializer(), statistics.toDto()),
                        updatedAtEpochMillis = System.currentTimeMillis()
                    )
                )
                emit(statistics)
            }.onFailure {
                cachedStatisticsDao.getSnapshot()?.let { cached ->
                    val statistics = Json.decodeFromString(MatchDataDto.serializer(), cached.payloadJson).toDomain()
                    emit(statistics.copy(isStale = true))
                }
            }

            delay(POLL_INTERVAL_MS)
        }
    }.flowOn(dispatchers.io)

    private companion object {
        const val POLL_INTERVAL_MS = 1000L
    }
}
