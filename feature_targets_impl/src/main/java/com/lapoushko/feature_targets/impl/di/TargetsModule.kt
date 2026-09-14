package com.lapoushko.feature_targets.impl.di

import androidx.lifecycle.ViewModel
import com.lapoushko.core_ui.di.ViewModelKey
import com.lapoushko.feature_targets.api.domain.TargetsRepository
import com.lapoushko.feature_targets.impl.data.TargetsRepositoryImpl
import com.lapoushko.feature_targets.impl.presentation.TargetsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
interface TargetsModule {

    @Binds
    @Singleton
    fun bindTargetsRepository(impl: TargetsRepositoryImpl): TargetsRepository

    @Binds
    @IntoMap
    @ViewModelKey(TargetsViewModel::class)
    fun bindTargetsViewModel(viewModel: TargetsViewModel): ViewModel
}
