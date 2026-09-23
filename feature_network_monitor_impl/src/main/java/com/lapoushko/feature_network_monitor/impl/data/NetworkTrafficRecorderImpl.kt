package com.lapoushko.feature_network_monitor.impl.data

import com.lapoushko.core_common.DispatcherProvider
import com.lapoushko.core_network.NetworkTrafficEntry
import com.lapoushko.core_network.NetworkTrafficRecorder
import com.lapoushko.database.NetworkLogDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Живёт дольше ViewModel-ов экрана (ставится в RetrofitFactory при старте приложения),
 * поэтому пишет через свой собственный CoroutineScope, а не viewModelScope.
 */
@Singleton
class NetworkTrafficRecorderImpl @Inject constructor(
    private val dao: NetworkLogDao,
    private val dispatchers: DispatcherProvider
) : NetworkTrafficRecorder {

    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)

    override fun record(entry: NetworkTrafficEntry) {
        scope.launch {
            dao.insert(entry.toEntity())
            dao.trimToLimit()
        }
    }
}
