package com.lapoushko.feature_statistics.api.domain

data class Participant(
    val riflemanId: String,
    val fullName: String,
    val teamName: String?,
    val targetIds: List<Int>,
    val batteryCharge: Int,
    val magazinesLeft: Int,
    val roundsLeft: Int,
    val shots: Int,
    val hits: Int,
    /** Ключ — id мишени, значение — битовая маска зон попадания, например "10010". */
    val targetHitPatterns: Map<Int, String>
)
