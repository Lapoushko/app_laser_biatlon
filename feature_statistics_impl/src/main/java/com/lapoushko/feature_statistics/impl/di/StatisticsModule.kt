package com.lapoushko.feature_statistics.impl.di

import androidx.lifecycle.ViewModel
import com.lapoushko.core_ui.di.ViewModelKey
import com.lapoushko.feature_statistics.api.domain.StatisticsRepository
import com.lapoushko.feature_statistics.impl.data.StatisticsRepositoryImpl
import com.lapoushko.feature_statistics.impl.presentation.StatisticsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
interface StatisticsModule {

    @Binds
    @Singleton
    fun bindStatisticsRepository(impl: StatisticsRepositoryImpl): StatisticsRepository

    @Binds
    @IntoMap
    @ViewModelKey(StatisticsViewModel::class)
    fun bindStatisticsViewModel(viewModel: StatisticsViewModel): ViewModel
}
