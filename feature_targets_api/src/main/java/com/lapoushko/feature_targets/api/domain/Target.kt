package com.lapoushko.feature_targets.api.domain

data class Target(
    val id: Int,
    val batteryCharge: Int,
    val hitsCount: Int,
    /** ФИО участника -> число его попаданий в эту мишень. */
    val players: Map<String, Int>
)
