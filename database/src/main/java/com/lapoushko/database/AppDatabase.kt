package com.lapoushko.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CachedStatisticsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedStatisticsDao(): CachedStatisticsDao

    companion object {
        const val DATABASE_NAME = "laser_biatlon.db"
    }
}
