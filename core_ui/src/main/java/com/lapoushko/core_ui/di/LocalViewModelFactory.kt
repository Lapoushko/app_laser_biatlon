package com.lapoushko.core_ui.di

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider

/**
 * Предоставляется один раз в MainActivity (app-модуль) поверх DaggerViewModelFactory,
 * чтобы Compose-экраны в feature_*_impl модулях создавали ViewModel через DI без
 * прямой зависимости от app-модуля.
 */
val LocalViewModelFactory = staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("LocalViewModelFactory не предоставлен")
}
