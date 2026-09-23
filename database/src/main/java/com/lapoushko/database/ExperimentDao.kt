package com.lapoushko.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExperimentDao {

    @Insert
    suspend fun insertSession(session: ExperimentSessionEntity): Long

    @Query("UPDATE experiment_sessions SET endedAtEpochMillis = :endedAtEpochMillis WHERE id = :sessionId")
    suspend fun endSession(sessionId: Long, endedAtEpochMillis: Long)

    @Query("SELECT * FROM experiment_sessions ORDER BY id DESC")
    fun observeSessions(): Flow<List<ExperimentSessionEntity>>

    @Query("DELETE FROM experiment_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Insert
    suspend fun insertEvent(event: ExperimentEventEntity)

    @Query("SELECT * FROM experiment_events WHERE sessionId = :sessionId ORDER BY id ASC")
    fun observeEvents(sessionId: Long): Flow<List<ExperimentEventEntity>>

    @Query("DELETE FROM experiment_events WHERE sessionId = :sessionId")
    suspend fun deleteEventsForSession(sessionId: Long)
}
