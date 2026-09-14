package com.lapoushko.feature_targets.impl.data

import com.lapoushko.core_common.DispatcherProvider
import com.lapoushko.core_network.RetrofitServiceFactory
import com.lapoushko.feature_targets.api.domain.Target
import com.lapoushko.feature_targets.api.domain.TargetsRepository
import com.lapoushko.feature_targets.impl.data.dto.StartMatchRequestDto
import com.lapoushko.feature_targets.impl.data.dto.StopMatchRequestDto
import com.lapoushko.feature_targets.impl.data.dto.toDomain
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TargetsRepositoryImpl @Inject constructor(
    private val retrofitServiceFactory: RetrofitServiceFactory,
    private val dispatchers: DispatcherProvider
) : TargetsRepository {

    override fun observeTargets(): Flow<List<Target>> = flow {
        while (currentCoroutineContext().isActive) {
            runCatching {
                retrofitServiceFactory.create(TargetsApiService::class.java)
                    .getMatchData()
                    .data
                    ?.targets
                    ?.toDomain()
                    .orEmpty()
            }.onSuccess { emit(it) }
            delay(POLL_INTERVAL_MS)
        }
    }.flowOn(dispatchers.io)

    override suspend fun startMatch(isInfinite: Boolean, durationSeconds: Int): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching {
                retrofitServiceFactory.create(TargetsApiService::class.java)
                    .startMatch(StartMatchRequestDto(isInfinite, durationSeconds))
            }
        }

    override suspend fun stopMatch(remainingSeconds: Int): Result<Unit> =
        withContext(dispatchers.io) {
            runCatching {
                retrofitServiceFactory.create(TargetsApiService::class.java)
                    .stopMatch(StopMatchRequestDto(remainingSeconds))
            }
        }

    private companion object {
        const val POLL_INTERVAL_MS = 2000L
    }
}
