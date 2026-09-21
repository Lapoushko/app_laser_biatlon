package com.lapoushko.app_laser_biatlon.di

import android.app.Application
import android.content.Context
import com.lapoushko.core_common.di.ApplicationContext
import dagger.Module
import dagger.Provides

@Module
object AppModule {

    @Provides
    @ApplicationContext
    fun provideApplicationContext(application: Application): Context = application
}
