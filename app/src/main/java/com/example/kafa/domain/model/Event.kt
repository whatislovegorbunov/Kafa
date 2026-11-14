package com.example.kafa.domain.model

data class Event(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val reminderInterval: ReminderInterval = ReminderInterval.Automatic,
    val history: List<EventOccurrence> = emptyList()
)

data class EventOccurrence(
    val id: String,
    val occurredAt: Long,
    val comment: String? = null
)

sealed interface ReminderInterval {
    val days: Int

    data object Automatic : ReminderInterval {
        override val days: Int = 5
    }

    data class Fixed(override val days: Int) : ReminderInterval {
        init { require(days > 0) }
    }
}
