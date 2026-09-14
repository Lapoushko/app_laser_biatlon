package com.lapoushko.feature_targets.impl.data.dto

import com.lapoushko.feature_targets.api.domain.Target

fun Map<String, TargetDto>.toDomain(): List<Target> = map { (id, dto) ->
    Target(
        id = id.toInt(),
        batteryCharge = dto.charge,
        hitsCount = dto.numberHits,
        players = dto.listPlayer
    )
}
