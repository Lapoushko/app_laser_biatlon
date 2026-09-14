package com.lapoushko.feature_targets.impl.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMatchDataResponseDto(
    val success: Boolean,
    val data: MatchDataDto? = null,
    val error: String? = null
)

@Serializable
data class MatchDataDto(
    val targets: Map<String, TargetDto> = emptyMap()
)

@Serializable
data class TargetDto(
    @SerialName("Number_hits") val numberHits: Int = 0,
    @SerialName("Charge") val charge: Int = 0,
    @SerialName("List_player") val listPlayer: Map<String, Int> = emptyMap()
)

@Serializable
data class StartMatchRequestDto(
    @SerialName("is_infinite") val isInfinite: Boolean,
    val duration: Int
)

@Serializable
data class StopMatchRequestDto(
    @SerialName("remaining_time") val remainingTime: Int
)
