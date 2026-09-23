package com.lapoushko.core_network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private val json = Json { ignoreUnknownKeys = true }

private val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
}

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .addInterceptor(NetworkTrafficInterceptor { RetrofitFactory.recorder })
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .writeTimeout(5, TimeUnit.SECONDS)
    .build()

/**
 * ПАК "Лазерный биатлон" работает в локальной сети без TLS, поэтому baseUrl всегда http://.
 * android:usesCleartextTraffic для этого включён в манифесте app-модуля.
 */
object RetrofitFactory {

    /**
     * Заменяется на реальную реализацию (запись в Room) в [com.lapoushko.app_laser_biatlon.LaserBiatlonApp]
     * при старте приложения. До этого момента и в тестах трафик просто не сохраняется.
     */
    var recorder: NetworkTrafficRecorder = NetworkTrafficRecorder { }

    fun <T> create(baseUrl: String, serviceClass: Class<T>): T {
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
        return retrofit.create(serviceClass)
    }
}
