package com.example.kafa.data.local

import androidx.room.Embedded
import androidx.room.Relation
import com.example.kafa.domain.model.Event
import com.example.kafa.domain.model.EventOccurrence
import com.example.kafa.domain.model.ReminderInterval

data class EventWithOccurrences(
    @Embedded val event: EventEntity,
    @Relation(parentColumn = "id", entityColumn = "eventId") val occurrences: List<EventOccurrenceEntity>
)

fun EventWithOccurrences.toDomain(): Event = Event(
    id = event.id,
    title = event.title,
    content = event.content,
    createdAt = event.createdAt,
    reminderInterval = event.intervalDays?.let { ReminderInterval.Fixed(it) } ?: ReminderInterval.Automatic,
    history = occurrences.sortedByDescending { it.occurredAt }.map { EventOccurrence(it.id, it.occurredAt, it.comment) }
)
