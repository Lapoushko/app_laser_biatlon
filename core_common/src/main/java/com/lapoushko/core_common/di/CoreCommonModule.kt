package com.lapoushko.core_common.di

import com.lapoushko.core_common.DefaultDispatcherProvider
import com.lapoushko.core_common.DispatcherProvider
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface CoreCommonModule {

    @Binds
    @Singleton
    fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider
}
