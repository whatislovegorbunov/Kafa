package com.example.kafa.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val intervalDays: Int?
)

@Entity(
    tableName = "event_occurrences",
    foreignKeys = [ForeignKey(entity = EventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("eventId")]
)
data class EventOccurrenceEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val occurredAt: Long,
    val comment: String?
)

@Entity(
    tableName = "reminder_delivery",
    foreignKeys = [ForeignKey(entity = EventEntity::class, parentColumns = ["id"], childColumns = ["eventId"], onDelete = ForeignKey.CASCADE)]
)
data class ReminderDeliveryEntity(
    @PrimaryKey val eventId: String,
    val occurrenceId: String?,
    val sentAt: Long
)
