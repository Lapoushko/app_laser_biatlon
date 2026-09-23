package com.lapoushko.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CachedStatisticsDao {

    @Query("SELECT * FROM cached_statistics WHERE id = ${CachedStatisticsEntity.SINGLE_ROW_ID} LIMIT 1")
    suspend fun getSnapshot(): CachedStatisticsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: CachedStatisticsEntity)
}
