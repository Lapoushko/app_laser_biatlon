package com.lapoushko.database.di

import android.content.Context
import androidx.room.Room
import com.lapoushko.core_common.di.ApplicationContext
import com.lapoushko.database.AppDatabase
import com.lapoushko.database.CachedStatisticsDao
import com.lapoushko.database.ExperimentDao
import com.lapoushko.database.NetworkLogDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            // Кэш статистики и лог трафика — не критичные для сохранности данные,
            // миграции для них избыточны на этом этапе проекта.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideCachedStatisticsDao(database: AppDatabase): CachedStatisticsDao =
        database.cachedStatisticsDao()

    @Provides
    @Singleton
    fun provideNetworkLogDao(database: AppDatabase): NetworkLogDao =
        database.networkLogDao()

    @Provides
    @Singleton
    fun provideExperimentDao(database: AppDatabase): ExperimentDao =
        database.experimentDao()
}
