package com.lapoushko.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedStatisticsEntity::class,
        NetworkLogEntity::class,
        ExperimentSessionEntity::class,
        ExperimentEventEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedStatisticsDao(): CachedStatisticsDao
    abstract fun networkLogDao(): NetworkLogDao
    abstract fun experimentDao(): ExperimentDao

    companion object {
        const val DATABASE_NAME = "laser_biatlon.db"
    }
}
