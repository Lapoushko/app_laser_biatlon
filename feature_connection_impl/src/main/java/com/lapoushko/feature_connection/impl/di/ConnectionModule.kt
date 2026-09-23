package com.lapoushko.feature_connection.impl.di

import androidx.lifecycle.ViewModel
import com.lapoushko.core_ui.di.ViewModelKey
import com.lapoushko.feature_connection.api.domain.ConnectionRepository
import com.lapoushko.feature_connection.impl.data.ConnectionRepositoryImpl
import com.lapoushko.feature_connection.impl.presentation.ConnectionViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
interface ConnectionModule {

    @Binds
    @Singleton
    fun bindConnectionRepository(impl: ConnectionRepositoryImpl): ConnectionRepository

    @Binds
    @IntoMap
    @ViewModelKey(ConnectionViewModel::class)
    fun bindConnectionViewModel(viewModel: ConnectionViewModel): ViewModel
}
