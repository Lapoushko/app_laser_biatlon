package com.lapoushko.feature_network_monitor.impl.di

import androidx.lifecycle.ViewModel
import com.lapoushko.core_network.NetworkTrafficRecorder
import com.lapoushko.core_ui.di.ViewModelKey
import com.lapoushko.feature_network_monitor.api.domain.NetworkLogRepository
import com.lapoushko.feature_network_monitor.impl.data.NetworkLogRepositoryImpl
import com.lapoushko.feature_network_monitor.impl.data.NetworkTrafficRecorderImpl
import com.lapoushko.feature_network_monitor.impl.presentation.NetworkMonitorViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
interface NetworkMonitorModule {

    @Binds
    @Singleton
    fun bindNetworkLogRepository(impl: NetworkLogRepositoryImpl): NetworkLogRepository

    @Binds
    @Singleton
    fun bindNetworkTrafficRecorder(impl: NetworkTrafficRecorderImpl): NetworkTrafficRecorder

    @Binds
    @IntoMap
    @ViewModelKey(NetworkMonitorViewModel::class)
    fun bindNetworkMonitorViewModel(viewModel: NetworkMonitorViewModel): ViewModel
}
