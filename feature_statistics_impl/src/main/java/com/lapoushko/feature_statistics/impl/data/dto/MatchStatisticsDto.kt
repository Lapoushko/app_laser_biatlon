package com.lapoushko.feature_statistics.impl.data.dto

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
    val metadata: MetadataDto,
    val results: Map<String, PlayerDto> = emptyMap(),
    val targets: Map<String, TargetDto> = emptyMap()
)

@Serializable
data class MetadataDto(
    @SerialName("game_mode") val gameMode: String,
    @SerialName("start_time") val startTime: String,
    @SerialName("match_duration") val matchDuration: Int,
    @SerialName("is_infinite_mode") val isInfiniteMode: Boolean,
    @SerialName("remaining_time") val remainingTime: Int
)

@Serializable
data class PlayerDto(
    @SerialName("FIO_db") val fio: String,
    @SerialName("TEAMNAME_db") val teamName: String? = null,
    @SerialName("ID_TAR_db") val targetIds: List<Int> = emptyList(),
    @SerialName("VIN_CHARGE") val vinCharge: Int = 0,
    @SerialName("NUMBER_MAGAZINES") val numberMagazines: Int = 0,
    @SerialName("NUMBER_ROUNDS") val numberRounds: Int = 0,
    @SerialName("NUMBER_SHOTS") val numberShots: Int = 0,
    @SerialName("NUMBER_HITS") val numberHits: Int = 0,
    @SerialName("TARGET_HITS") val targetHits: Map<String, String> = emptyMap()
)

@Serializable
data class TargetDto(
    @SerialName("Number_hits") val numberHits: Int = 0,
    @SerialName("Charge") val charge: Int = 0,
    @SerialName("List_player") val listPlayer: Map<String, Int> = emptyMap()
)
