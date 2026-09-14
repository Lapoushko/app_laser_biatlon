package com.lapoushko.core_ui.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Единая ViewModelProvider.Factory, собранная Dagger-мультибиндингом из @IntoMap
 * биндингов всех feature_*_impl модулей.
 */
@Singleton
class DaggerViewModelFactory @Inject constructor(
    private val creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val creator = creators[modelClass]
            ?: creators.entries.firstOrNull { modelClass.isAssignableFrom(it.key) }?.value
            ?: throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        @Suppress("UNCHECKED_CAST")
        return creator.get() as T
    }
}
