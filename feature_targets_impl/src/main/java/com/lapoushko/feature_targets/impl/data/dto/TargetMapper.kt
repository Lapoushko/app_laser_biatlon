package com.lapoushko.feature_targets.impl.data.dto

import com.lapoushko.feature_targets.api.domain.Target
import com.lapoushko.feature_targets.api.domain.TargetHitSeries

/**
 * "targets" в ответе — агрегаты по мишени (заряд, число попаданий, список стрелков).
 * Состояние 5 зон мишени там не хранится — оно приходит per-стрелок в "results[].TARGET_HITS"
 * (py/match.py, parsing_vin_hit), т.к. физически строка "с железа" привязана к событию
 * попадания конкретного стрелка. Одну мишень может использовать несколько стрелков подряд
 * (например, в командной эстафете), поэтому собираем серии всех, у кого уже есть попадания.
 */
fun MatchDataDto.toDomain(): List<Target> = targets.map { (id, dto) ->
    Target(
        id = id.toInt(),
        batteryCharge = dto.charge,
        hitsCount = dto.numberHits,
        players = dto.listPlayer,
        hitSeries = results.values
            .mapNotNull { result ->
                val pattern = result.targetHits[id] ?: return@mapNotNull null
                if (pattern == Target.DEFAULT_HIT_PATTERN) return@mapNotNull null
                TargetHitSeries(shooterName = result.fio.ifBlank { "Стрелок" }, pattern = pattern)
            }
            .sortedBy { it.shooterName }
    )
}
