package com.example.kafa.data.local

import com.example.kafa.domain.model.ReminderInterval
import org.junit.Assert.assertEquals
import org.junit.Test

class EventMapperTest {
    @Test
    fun mapsFixedIntervalAndHistoryInReverseChronologicalOrder() {
        val event = EventWithOccurrences(
            EventEntity("1", "Title", "Note", 1000L, 12),
            listOf(EventOccurrenceEntity("a", "1", 2000L, null), EventOccurrenceEntity("b", "1", 3000L, "Comment"))
        ).toDomain()
        assertEquals(ReminderInterval.Fixed(12), event.reminderInterval)
        assertEquals(listOf("b", "a"), event.history.map { it.id })
        assertEquals("Comment", event.history.first().comment)
    }

    @Test
    fun nullIntervalMeansAutomatic() {
        val event = EventWithOccurrences(EventEntity("1", "Title", "", 1000L, null), emptyList()).toDomain()
        assertEquals(ReminderInterval.Automatic, event.reminderInterval)
        assertEquals(5, event.reminderInterval.days)
    }
}
