package com.lapoushko.feature_experiment.impl.data

import retrofit2.Response
import retrofit2.http.GET

/** Тот же лёгкий health-check, что и в feature_connection — GET всегда отвечает 200. */
interface ExperimentPingApiService {
    @GET("unique_teams/")
    suspend fun ping(): Response<List<String>>
}
