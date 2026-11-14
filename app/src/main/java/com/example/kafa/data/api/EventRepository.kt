package com.example.kafa.data.api

import com.example.kafa.domain.model.Event
import com.example.kafa.domain.model.ReminderInterval
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeEvents(): Flow<List<Event>>
    suspend fun addEvent(title: String, content: String, interval: ReminderInterval): Result<Unit>
    suspend fun deleteEvent(id: String): Result<Unit>
    suspend fun addOccurrence(id: String, occurredAt: Long, comment: String?): Result<Unit>
}
