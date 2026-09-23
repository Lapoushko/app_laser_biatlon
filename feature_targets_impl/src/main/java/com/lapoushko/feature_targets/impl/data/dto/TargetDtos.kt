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
    val targets: Map<String, TargetDto> = emptyMap(),
    val results: Map<String, ResultDto> = emptyMap()
)

@Serializable
data class TargetDto(
    @SerialName("Number_hits") val numberHits: Int = 0,
    @SerialName("Charge") val charge: Int = 0,
    @SerialName("List_player") val listPlayer: Map<String, Int> = emptyMap()
)

/**
 * Состояние 5 механических зон конкретной мишени с точки зрения стрелка, назначенного на неё
 * ("TARGET_HITS" в py/registration.py). Строка вида "01000" совпадает с именами файлов
 * в res/drawable/target_hits_*.png — сервер отдаёт её как есть с "железа".
 */
@Serializable
data class ResultDto(
    @SerialName("FIO_db") val fio: String = "",
    @SerialName("TARGET_HITS") val targetHits: Map<String, String> = emptyMap()
)
