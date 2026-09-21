package com.lapoushko.feature_targets.impl.data

import com.lapoushko.feature_targets.impl.data.dto.GetMatchDataResponseDto
import com.lapoushko.feature_targets.impl.data.dto.StartMatchRequestDto
import com.lapoushko.feature_targets.impl.data.dto.StopMatchRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TargetsApiService {
    @GET("get_match_data")
    suspend fun getMatchData(): GetMatchDataResponseDto

    @POST("start_button")
    suspend fun startMatch(@Body body: StartMatchRequestDto)

    @POST("stop_button")
    suspend fun stopMatch(@Body body: StopMatchRequestDto)
}
