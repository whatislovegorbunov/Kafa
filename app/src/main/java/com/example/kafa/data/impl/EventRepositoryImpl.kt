package com.example.kafa.data.impl

import com.example.kafa.data.api.EventRepository
import com.example.kafa.data.local.EventDao
import com.example.kafa.data.local.EventEntity
import com.example.kafa.data.local.EventOccurrenceEntity
import com.example.kafa.data.local.toDomain
import com.example.kafa.domain.model.Event
import com.example.kafa.domain.model.ReminderInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(private val eventDao: EventDao) : EventRepository {
    override fun observeEvents(): Flow<List<Event>> = eventDao.observeAll().map { events -> events.map { it.toDomain() } }

    override suspend fun addEvent(title: String, content: String, interval: ReminderInterval): Result<Unit> = runCatching {
        eventDao.insert(EventEntity(UUID.randomUUID().toString(), title, content, System.currentTimeMillis(), (interval as? ReminderInterval.Fixed)?.days))
    }

    override suspend fun deleteEvent(id: String): Result<Unit> = runCatching { eventDao.deleteById(id) }

    override suspend fun addOccurrence(id: String, occurredAt: Long, comment: String?): Result<Unit> = runCatching {
        eventDao.insertOccurrence(EventOccurrenceEntity(UUID.randomUUID().toString(), id, occurredAt, comment?.trim()?.ifEmpty { null }))
    }
}
