package com.lapoushko.feature_targets.impl.data

import com.lapoushko.core_common.DispatcherProvider
import com.lapoushko.core_network.RetrofitServiceFactory
import com.lapoushko.feature_targets.api.domain.Target
import com.lapoushko.feature_targets.api.domain.TargetsRepository
import com.lapoushko.feature_targets.impl.data.dto.toDomain
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TargetsRepositoryImpl @Inject constructor(
    private val retrofitServiceFactory: RetrofitServiceFactory,
    private val dispatchers: DispatcherProvider
) : TargetsRepository {

    override fun observeTargets(): Flow<List<Target>> = flow {
        while (currentCoroutineContext().isActive) {
            // Пока матч не запущен (или сервер недоступен), запрос падает с ошибкой —
            // всё равно эмитим (пустой список), иначе экран навсегда останется в isLoading.
            val targets = runCatching {
                retrofitServiceFactory.create(TargetsApiService::class.java)
                    .getMatchData()
                    .data
                    ?.toDomain()
                    .orEmpty()
            }.getOrDefault(emptyList())
            emit(targets)
            delay(POLL_INTERVAL_MS)
        }
    }.flowOn(dispatchers.io)

    private companion object {
        const val POLL_INTERVAL_MS = 2000L
    }
}
