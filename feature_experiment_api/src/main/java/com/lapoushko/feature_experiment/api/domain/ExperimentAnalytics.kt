package com.lapoushko.feature_experiment.api.domain

import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Модель затухания сигнала: RSSI(d) = rssiAt1mDbm - 10 * pathLossExponent * log10(d).
 * Оценена по методу наименьших квадратов на точках (сегмент с известным расстоянием, его средний RSSI).
 */
data class PathLossModel(
    val pathLossExponent: Double,
    val rssiAt1mDbm: Double,
    val rSquared: Double,
    val pointCount: Int
)

/** Коэффициенты корреляции Пирсона между RSSI сегмента и его же RTT/потерями — по всем сегментам сессии. */
data class ExperimentCorrelation(
    val rssiVsRtt: Double?,
    val rssiVsLoss: Double?
)

/** [xs] и [ys] должны быть одной длины; возвращает null при <2 точках или нулевой дисперсии. */
fun pearsonCorrelation(xs: List<Double>, ys: List<Double>): Double? {
    if (xs.size < 2 || xs.size != ys.size) return null
    val meanX = xs.average()
    val meanY = ys.average()
    var covariance = 0.0
    var varianceX = 0.0
    var varianceY = 0.0
    for (i in xs.indices) {
        val dx = xs[i] - meanX
        val dy = ys[i] - meanY
        covariance += dx * dy
        varianceX += dx * dx
        varianceY += dy * dy
    }
    if (varianceX == 0.0 || varianceY == 0.0) return null
    return covariance / sqrt(varianceX * varianceY)
}

/** [points] — пары (расстояние в метрах > 0, RSSI дБм). Возвращает null при <2 точках. */
fun fitPathLossModel(points: List<Pair<Double, Double>>): PathLossModel? {
    val valid = points.filter { (distance, _) -> distance > 0 }
    if (valid.size < 2) return null

    val xs = valid.map { (distance, _) -> log10(distance) }
    val ys = valid.map { (_, rssi) -> rssi }
    val meanX = xs.average()
    val meanY = ys.average()

    var sumXY = 0.0
    var sumXX = 0.0
    for (i in xs.indices) {
        sumXY += (xs[i] - meanX) * (ys[i] - meanY)
        sumXX += (xs[i] - meanX) * (xs[i] - meanX)
    }
    if (sumXX == 0.0) return null

    val slope = sumXY / sumXX
    val intercept = meanY - slope * meanX

    val ssTotal = ys.sumOf { y -> (y - meanY) * (y - meanY) }
    val ssResidual = xs.indices.sumOf { i ->
        val predicted = intercept + slope * xs[i]
        (ys[i] - predicted) * (ys[i] - predicted)
    }
    val rSquared = if (ssTotal == 0.0) 1.0 else 1.0 - ssResidual / ssTotal

    return PathLossModel(
        pathLossExponent = -slope / 10.0,
        rssiAt1mDbm = intercept,
        rSquared = rSquared,
        pointCount = valid.size
    )
}
