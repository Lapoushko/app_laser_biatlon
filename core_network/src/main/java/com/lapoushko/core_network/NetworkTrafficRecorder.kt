package com.lapoushko.core_network

/**
 * Приёмник захваченного трафика. По умолчанию [RetrofitFactory] использует no-op-реализацию;
 * feature_network_monitor_impl подменяет её на запись в Room при старте приложения.
 */
fun interface NetworkTrafficRecorder {
    fun record(entry: NetworkTrafficEntry)
}
