package com.lapoushko.core_network.di

import com.lapoushko.core_network.ServerAddressRepository
import com.lapoushko.core_network.ServerAddressRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface NetworkModule {

    @Binds
    @Singleton
    fun bindServerAddressRepository(impl: ServerAddressRepositoryImpl): ServerAddressRepository
}
