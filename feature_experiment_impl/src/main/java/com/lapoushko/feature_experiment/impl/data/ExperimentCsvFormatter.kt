package com.lapoushko.feature_experiment.impl.data

import com.lapoushko.feature_experiment.api.domain.ExperimentEvent
import com.lapoushko.feature_experiment.api.domain.ExperimentSegment
import com.lapoushko.feature_experiment.api.domain.ExperimentSession
import com.lapoushko.feature_experiment.api.domain.ExperimentSummary
import java.text.SimpleDateFormat
import java.util.Locale

private val csvTimestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

/**
 * Полный экспорт сессии эксперимента одним CSV-файлом из трёх готовых таблиц —
 * сырых событий (для графиков RTT/RSSI по времени), сводки по сегментам (для сравнения
 * меток между собой) и расчётов (корреляции, модель затухания сигнала). Секции разделены
 * пустой строкой и заголовком "=== ... ===", каждая читается как отдельная таблица в Excel.
 */
fun buildExperimentCsv(
    session: ExperimentSession,
    events: List<ExperimentEvent>,
    summary: ExperimentSummary
): String = buildString {
    appendLine("session_title,${session.title.csvEscape()}")
    appendLine("session_started_at,${csvTimestampFormat.format(session.startedAtEpochMillis)}")
    appendLine(
        "session_ended_at," +
            (session.endedAtEpochMillis?.let { csvTimestampFormat.format(it) } ?: "")
    )
    appendLine()

    appendLine("=== summary ===")
    appendLine("metric,value")
    appendLine("total_pings,${summary.totalPings}")
    appendLine("total_success,${summary.totalSuccess}")
    appendLine("overall_loss_percent,${summary.overallLossPercent}")
    appendLine("correlation_rssi_vs_rtt,${summary.correlation.rssiVsRtt.toCsvNumber()}")
    appendLine("correlation_rssi_vs_loss,${summary.correlation.rssiVsLoss.toCsvNumber()}")
    val model = summary.pathLossModel
    appendLine("path_loss_exponent_n,${model?.pathLossExponent.toCsvNumber()}")
    appendLine("path_loss_rssi_at_1m_dbm,${model?.rssiAt1mDbm.toCsvNumber()}")
    appendLine("path_loss_r_squared,${model?.rSquared.toCsvNumber()}")
    appendLine("path_loss_point_count,${model?.pointCount ?: ""}")
    appendLine()

    appendLine("=== segments ===")
    appendLine(
        "label,distance_m,started_at,ping_count,success_count,loss_percent," +
            "avg_rtt_ms,avg_rssi_dbm,min_rssi_dbm,max_rssi_dbm"
    )
    summary.segments.forEach { segment -> appendLine(segment.toCsvRow()) }
    appendLine()

    appendLine("=== raw_events ===")
    appendLine("timestamp,kind,label,distance_m,success,rtt_ms,rssi_dbm")
    events.forEach { event -> appendLine(event.toCsvRow()) }
}

private fun ExperimentSegment.toCsvRow(): String = listOf(
    label.csvEscape(),
    distanceMeters.toCsvNumber(),
    csvTimestampFormat.format(startedAtEpochMillis),
    pingCount.toString(),
    successCount.toString(),
    lossPercent.toString(),
    averageRttMillis?.toString() ?: "",
    averageRssiDbm?.toString() ?: "",
    minRssiDbm?.toString() ?: "",
    maxRssiDbm?.toString() ?: ""
).joinToString(",")

private fun ExperimentEvent.toCsvRow(): String {
    val (kind, label, distanceMeters, success, rttMillis, rssiDbm) = when (this) {
        is ExperimentEvent.Ping -> CsvRow("PING", "", "", success.toString(), rttMillis.toString(), rssiDbm)
        is ExperimentEvent.Checkpoint -> CsvRow(
            "CHECKPOINT", label, distanceMeters.toCsvNumber(), "", "", rssiDbm
        )
    }
    return listOf(
        csvTimestampFormat.format(timestampEpochMillis),
        kind,
        label.csvEscape(),
        distanceMeters,
        success,
        rttMillis,
        rssiDbm?.toString() ?: ""
    ).joinToString(",")
}

private data class CsvRow(
    val kind: String,
    val label: String,
    val distanceMeters: String,
    val success: String,
    val rttMillis: String,
    val rssiDbm: Int?
)

private fun Double?.toCsvNumber(): String = this?.let { String.format(Locale.US, "%.4f", it) } ?: ""

private fun String.csvEscape(): String =
    if (any { it == ',' || it == '"' || it == '\n' }) "\"${replace("\"", "\"\"")}\"" else this
