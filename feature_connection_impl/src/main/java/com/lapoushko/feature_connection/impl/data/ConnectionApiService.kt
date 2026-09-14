package com.lapoushko.feature_connection.impl.data

import retrofit2.Response
import retrofit2.http.GET

/**
 * У Flask-бэкенда ("py/main.py") нет отдельного health-check эндпоинта,
 * поэтому подключение проверяется через GET /unique_teams/ — он всегда
 * отвечает 200 (пустым списком, если участников ещё нет), в отличие от
 * /get_match_data, который до первого матча падает с 500.
 */
interface ConnectionApiService {
    @GET("unique_teams/")
    suspend fun checkStatus(): Response<List<String>>
}
