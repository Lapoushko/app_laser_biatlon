package com.lapoushko.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Событие в рамках сессии эксперимента: либо результат очередного пинга сервера (kind = "PING"),
 * либо метка, поставленная оператором вручную во время замера (kind = "CHECKPOINT",
 * например "5 м от точки доступа" или "глушение 10 dBm").
 */
@Entity(tableName = "experiment_events")
data class ExperimentEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val timestampEpochMillis: Long,
    val kind: String,
    val success: Boolean? = null,
    val rttMillis: Long? = null,
    val rssiDbm: Int? = null,
    val label: String? = null,
    /** Расстояние до точки доступа, указанное оператором при метке, м — для CHECKPOINT. */
    val distanceMeters: Double? = null
) {
    companion object {
        const val KIND_PING = "PING"
        const val KIND_CHECKPOINT = "CHECKPOINT"
    }
}
