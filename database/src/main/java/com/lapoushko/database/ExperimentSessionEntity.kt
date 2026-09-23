package com.lapoushko.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Один прогон эксперимента на устойчивость связи (замер по коридору, сессия с глушением и т.п.). */
@Entity(tableName = "experiment_sessions")
data class ExperimentSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long? = null
)
