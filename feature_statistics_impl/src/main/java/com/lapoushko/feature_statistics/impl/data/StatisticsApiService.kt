package com.lapoushko.feature_statistics.impl.data

import com.lapoushko.feature_statistics.impl.data.dto.GetMatchDataResponseDto
import retrofit2.http.GET

interface StatisticsApiService {
    @GET("get_match_data")
    suspend fun getMatchData(): GetMatchDataResponseDto
}
