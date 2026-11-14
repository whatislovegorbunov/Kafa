package com.example.kafa.data.impl

import com.example.kafa.data.local.EventDao
import com.example.kafa.data.local.EventEntity
import com.example.kafa.data.local.EventOccurrenceEntity
import com.example.kafa.domain.model.ReminderInterval
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventRepositoryImplTest {
    private val dao = mockk<EventDao>(relaxed = true)
    private val repository = EventRepositoryImpl(dao)

    @Test
    fun savesAutomaticAndFixedIntervals() = runTest {
        val entities = mutableListOf<EventEntity>()
        coEvery { dao.insert(capture(entities)) } returns Unit
        repository.addEvent("First", "", ReminderInterval.Automatic)
        repository.addEvent("Second", "", ReminderInterval.Fixed(12))
        assertNull(entities[0].intervalDays)
        assertEquals(12, entities[1].intervalDays)
    }

    @Test
    fun addsOccurrenceWithCommentAndTimestamp() = runTest {
        val occurrence = slot<EventOccurrenceEntity>()
        coEvery { dao.insertOccurrence(capture(occurrence)) } returns Unit
        assertTrue(repository.addOccurrence("event", 1234L, " hello ").isSuccess)
        assertEquals("event", occurrence.captured.eventId)
        assertEquals(1234L, occurrence.captured.occurredAt)
        assertEquals("hello", occurrence.captured.comment)
    }

    @Test
    fun reportsDatabaseFailure() = runTest {
        coEvery { dao.deleteById(any()) } throws IllegalStateException("failed")
        assertTrue(repository.deleteEvent("event").isFailure)
        coVerify { dao.deleteById("event") }
    }
}
