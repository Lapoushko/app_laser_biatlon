package com.lapoushko.feature_experiment.impl.data

import android.content.Context
import android.net.wifi.WifiManager
import com.lapoushko.core_common.di.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Уровень принимаемого Wi-Fi сигнала на самом телефоне (RSSI, дБм) — та самая "радиотехническая"
 * величина для эксперимента на устойчивость связи. С Android 8.1 её чтение требует разрешения
 * на геолокацию (см. манифест и запрос в UI), иначе WifiManager отдаёт заведомо невалидное значение.
 */
@Singleton
class WifiSignalReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Suppress("DEPRECATION")
    fun currentRssiDbm(): Int? {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val rssi = runCatching { wifiManager.connectionInfo.rssi }.getOrNull() ?: return null
        return rssi.takeIf { it != INVALID_RSSI }
    }

    private companion object {
        const val INVALID_RSSI = Int.MIN_VALUE
    }
}
