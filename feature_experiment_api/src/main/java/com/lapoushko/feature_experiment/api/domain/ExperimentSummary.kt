package com.lapoushko.feature_experiment.api.domain

/**
 * Статистика по одному отрезку сессии — от одной метки оператора до следующей
 * (или от старта сессии до первой метки). Например: "5 м от точки доступа" -> потери 0%,
 * "10 м, за углом" -> потери 40%.
 */
data class ExperimentSegment(
    val label: String,
    val startedAtEpochMillis: Long,
    val pingCount: Int,
    val successCount: Int,
    val averageRttMillis: Long?,
    val averageRssiDbm: Int?,
    val minRssiDbm: Int?,
    val maxRssiDbm: Int?
) {
    val lossPercent: Int
        get() = if (pingCount == 0) 0 else (pingCount - successCount) * 100 / pingCount
}

data class ExperimentSummary(
    val segments: List<ExperimentSegment>,
    val totalPings: Int,
    val totalSuccess: Int
) {
    val overallLossPercent: Int
        get() = if (totalPings == 0) 0 else (totalPings - totalSuccess) * 100 / totalPings
}
