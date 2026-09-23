package com.lapoushko.feature_experiment.impl.di

import androidx.lifecycle.ViewModel
import com.lapoushko.core_ui.di.ViewModelKey
import com.lapoushko.feature_experiment.api.domain.ExperimentRepository
import com.lapoushko.feature_experiment.impl.data.ExperimentRepositoryImpl
import com.lapoushko.feature_experiment.impl.presentation.ExperimentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
interface ExperimentModule {

    @Binds
    @Singleton
    fun bindExperimentRepository(impl: ExperimentRepositoryImpl): ExperimentRepository

    @Binds
    @IntoMap
    @ViewModelKey(ExperimentViewModel::class)
    fun bindExperimentViewModel(viewModel: ExperimentViewModel): ViewModel
}
