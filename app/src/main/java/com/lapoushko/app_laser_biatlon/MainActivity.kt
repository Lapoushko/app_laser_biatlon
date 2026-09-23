package com.lapoushko.app_laser_biatlon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.lapoushko.core_ui.di.LocalViewModelFactory
import com.lapoushko.core_ui.theme.LaserBiatlonTheme
import com.lapoushko.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appComponent = (application as LaserBiatlonApp).appComponent
        setContent {
            LaserBiatlonTheme {
                CompositionLocalProvider(LocalViewModelFactory provides appComponent.viewModelFactory()) {
                    AppNavHost()
                }
            }
        }
    }
}
