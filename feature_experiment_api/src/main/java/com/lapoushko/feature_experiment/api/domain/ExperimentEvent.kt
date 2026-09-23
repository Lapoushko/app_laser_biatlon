package com.lapoushko.feature_experiment.api.domain

/**
 * Событие внутри сессии эксперимента: результат пинга сервера ("устойчивость приёма пакетов")
 * либо метка, поставленная оператором на месте (расстояние до точки доступа, уровень помехи и т.п.).
 */
sealed interface ExperimentEvent {
    val timestampEpochMillis: Long

    data class Ping(
        override val timestampEpochMillis: Long,
        val success: Boolean,
        val rttMillis: Long,
        /** Уровень принимаемого Wi-Fi сигнала на телефоне в момент пинга, дБм. Null — нет данных/разрешения. */
        val rssiDbm: Int?
    ) : ExperimentEvent

    data class Checkpoint(
        override val timestampEpochMillis: Long,
        val label: String,
        val rssiDbm: Int?
    ) : ExperimentEvent
}
