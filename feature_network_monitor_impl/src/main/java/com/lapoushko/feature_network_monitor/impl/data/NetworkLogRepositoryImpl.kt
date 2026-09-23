package com.lapoushko.feature_network_monitor.impl.data

import com.lapoushko.database.NetworkLogDao
import com.lapoushko.feature_network_monitor.api.domain.NetworkLogEntry
import com.lapoushko.feature_network_monitor.api.domain.NetworkLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkLogRepositoryImpl @Inject constructor(
    private val dao: NetworkLogDao
) : NetworkLogRepository {

    override fun observeLogs(): Flow<List<NetworkLogEntry>> =
        dao.observeRecent().map { entities -> entities.map { it.toDomain() } }

    override suspend fun clear() = dao.clear()
}
