package com.lapoushko.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

private const val MAX_STORED_LOGS = 300

@Dao
interface NetworkLogDao {

    @Query("SELECT * FROM network_logs ORDER BY id DESC LIMIT $MAX_STORED_LOGS")
    fun observeRecent(): Flow<List<NetworkLogEntity>>

    @Insert
    suspend fun insert(entity: NetworkLogEntity)

    @Query(
        "DELETE FROM network_logs WHERE id NOT IN " +
            "(SELECT id FROM network_logs ORDER BY id DESC LIMIT $MAX_STORED_LOGS)"
    )
    suspend fun trimToLimit()

    @Query("DELETE FROM network_logs")
    suspend fun clear()
}
