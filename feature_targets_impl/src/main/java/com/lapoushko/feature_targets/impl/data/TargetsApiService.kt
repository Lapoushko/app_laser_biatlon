package com.lapoushko.feature_targets.impl.data

import com.lapoushko.feature_targets.impl.data.dto.GetMatchDataResponseDto
import retrofit2.http.GET

interface TargetsApiService {
    @GET("get_match_data")
    suspend fun getMatchData(): GetMatchDataResponseDto
}
