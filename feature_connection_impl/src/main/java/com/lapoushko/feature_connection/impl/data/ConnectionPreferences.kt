package com.lapoushko.feature_connection.impl.data

import android.content.Context
import com.lapoushko.core_common.di.ApplicationContext
import javax.inject.Inject

class ConnectionPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastAddress: String?
        get() = prefs.getString(KEY_LAST_ADDRESS, null)
        set(value) = prefs.edit().putString(KEY_LAST_ADDRESS, value).apply()

    companion object {
        private const val PREFS_NAME = "connection_prefs"
        private const val KEY_LAST_ADDRESS = "last_address"
    }
}
