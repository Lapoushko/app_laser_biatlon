package com.lapoushko.app_laser_biatlon

import android.app.Application
import com.lapoushko.app_laser_biatlon.di.AppComponent
import com.lapoushko.app_laser_biatlon.di.DaggerAppComponent

class LaserBiatlonApp : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }
}
