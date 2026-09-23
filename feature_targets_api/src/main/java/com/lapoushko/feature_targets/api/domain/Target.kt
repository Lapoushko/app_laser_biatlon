package com.lapoushko.feature_targets.api.domain

data class Target(
    val id: Int,
    val batteryCharge: Int,
    val hitsCount: Int,
    /** ФИО участника -> число его попаданий в эту мишень. */
    val players: Map<String, Int>,
    /**
     * Одна мишень (физический щит с 5 механическими зонами) переиспользуется несколькими
     * стрелками — каждый следующий стрелок на этой мишени даёт свою серию попаданий.
     */
    val hitSeries: List<TargetHitSeries> = emptyList()
) {
    companion object {
        const val DEFAULT_HIT_PATTERN = "00000"
    }
}

/**
 * Серия попаданий одного стрелка по мишени: строка вида "01000" ("TARGET_HITS" с сервера),
 * '0' в позиции — зона поражена, '1' — нет.
 */
data class TargetHitSeries(
    val shooterName: String,
    val pattern: String
) {
    val hitsCount: Int
        get() = pattern.count { it == '0' }
}
