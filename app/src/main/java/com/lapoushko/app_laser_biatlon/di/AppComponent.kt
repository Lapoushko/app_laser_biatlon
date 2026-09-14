package com.lapoushko.app_laser_biatlon.di

import android.app.Application
import com.lapoushko.core_common.di.CoreCommonModule
import com.lapoushko.core_network.di.NetworkModule
import com.lapoushko.core_ui.di.DaggerViewModelFactory
import com.lapoushko.database.di.DatabaseModule
import com.lapoushko.feature_connection.impl.di.ConnectionModule
import com.lapoushko.feature_statistics.impl.di.StatisticsModule
import com.lapoushko.feature_targets.impl.di.TargetsModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        CoreCommonModule::class,
        NetworkModule::class,
        DatabaseModule::class,
        ConnectionModule::class,
        StatisticsModule::class,
        TargetsModule::class
    ]
)
interface AppComponent {

    fun viewModelFactory(): DaggerViewModelFactory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }
}
