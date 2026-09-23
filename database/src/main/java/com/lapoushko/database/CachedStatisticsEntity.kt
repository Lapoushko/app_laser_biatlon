package com.lapoushko.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Кэш последнего снимка статистики матча. Хранится как единственная строка (id = 0),
 * т.к. приложение отслеживает только один активный матч за раз.
 * payloadJson непрозрачен для этого модуля — сериализацию/десериализацию делает feature_statistics_impl,
 * чтобы database не зависел от доменной модели фичи.
 */
@Entity(tableName = "cached_statistics")
data class CachedStatisticsEntity(
    @PrimaryKey val id: Int = SINGLE_ROW_ID,
    val payloadJson: String,
    val updatedAtEpochMillis: Long
) {
    companion object {
        const val SINGLE_ROW_ID = 0
    }
}
