package com.example.kafa.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Transaction
    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EventWithOccurrences>>

    @Insert
    suspend fun insert(entity: EventEntity)

    @Insert
    suspend fun insertOccurrence(entity: EventOccurrenceEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteById(id: String)
}
