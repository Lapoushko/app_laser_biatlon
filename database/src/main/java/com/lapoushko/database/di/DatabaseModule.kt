package com.lapoushko.database.di

import android.content.Context
import androidx.room.Room
import com.lapoushko.core_common.di.ApplicationContext
import com.lapoushko.database.AppDatabase
import com.lapoushko.database.CachedStatisticsDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideCachedStatisticsDao(database: AppDatabase): CachedStatisticsDao =
        database.cachedStatisticsDao()
}
