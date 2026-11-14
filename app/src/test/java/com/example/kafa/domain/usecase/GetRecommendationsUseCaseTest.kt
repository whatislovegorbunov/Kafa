package com.example.kafa.domain.usecase

import app.cash.turbine.test
import com.example.kafa.data.api.EventRepository
import com.example.kafa.domain.model.ReminderInterval
import com.example.kafa.domain.model.EventOccurrence
import com.example.kafa.domain.model.Event
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRecommendationsUseCaseTest {

    private val repository = mockk<EventRepository>()
    private val useCase = GetRecommendationsUseCase(repository)

    private val fixedNow = System.currentTimeMillis()
    private val day = 86_400_000L

    private fun event(
        id: String = "1",
        title: String = "Title",
        content: String = "Content",
        createdAt: Long = fixedNow,
        lastDoneAt: Long? = null,
        frequency: ReminderInterval = ReminderInterval.Fixed(7)
    ) = Event(id, title, content, createdAt, frequency, lastDoneAt?.let { listOf(EventOccurrence("occurrence", it)) } ?: emptyList())

    @Test
    fun emptyEventsList_returnsEmptyRecommendations() = runTest {
        every { repository.observeEvents() } returns flowOf(emptyList())
        useCase().test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun allEventsInSchedule_returnsEmptyRecommendations() = runTest {
        val events = listOf(
            event(id = "1", lastDoneAt = fixedNow, frequency = ReminderInterval.Fixed(1)),
            event(id = "2", lastDoneAt = fixedNow - 3 * day, frequency = ReminderInterval.Fixed(30))
        )
        every { repository.observeEvents() } returns flowOf(events)
        useCase().test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun oneOverdueEvent_returnsSingleRecommendation() = runTest {
        val events = listOf(
            event(id = "1", lastDoneAt = fixedNow - 10 * day, frequency = ReminderInterval.Fixed(7))
        )
        every { repository.observeEvents() } returns flowOf(events)
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertTrue(result[0].urgencyScore >= 1.0f)
            assertEquals("1", result[0].event.id)
            awaitComplete()
        }
    }

    @Test
    fun neverDoneEvent_returnsRecommendationWithNeverDoneMessage() = runTest {
        val events = listOf(
            event(id = "1", lastDoneAt = null, createdAt = fixedNow - 10 * day, frequency = ReminderInterval.Fixed(7))
        )
        every { repository.observeEvents() } returns flowOf(events)
        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Ещё ни разу не отмечено", result[0].message)
            awaitComplete()
        }
    }

    @Test
    fun multipleOverdueEvents_returnsSortedByUrgencyScoreDescending() = runTest {
        val events = listOf(
            event(id = "daily", lastDoneAt = fixedNow - 10 * day, frequency = ReminderInterval.Fixed(1)),
            event(id = "weekly", lastDoneAt = fixedNow - 10 * day, frequency = ReminderInterval.Fixed(7)),
            event(id = "monthly", lastDoneAt = fixedNow - 30 * day, frequency = ReminderInterval.Fixed(30))
        )
        every { repository.observeEvents() } returns flowOf(events)
        useCase().test {
            val result = awaitItem()
            assertEquals(3, result.size)
            assertTrue(result[0].urgencyScore >= result[1].urgencyScore)
            assertTrue(result[1].urgencyScore >= result[2].urgencyScore)
            assertEquals("daily", result[0].event.id)
            assertEquals("weekly", result[1].event.id)
            assertEquals("monthly", result[2].event.id)
            awaitComplete()
        }
    }

    @Test
    fun sevenOverdueEvents_returnsAtMostFiveRecommendations() = runTest {
        val events = (1..7).map { i ->
            event(id = "$i", lastDoneAt = fixedNow - 100 * day, frequency = ReminderInterval.Fixed(1))
        }
        every { repository.observeEvents() } returns flowOf(events)
        useCase().test {
            val result = awaitItem()
            assertEquals(5, result.size)
            awaitComplete()
        }
    }

    @Test
    fun differentFrequencies_dailyOverdueRanksHigherThanYearly() = runTest {
        val events = listOf(
            event(id = "daily", lastDoneAt = fixedNow - 2 * day, frequency = ReminderInterval.Fixed(1)),
            event(id = "yearly", lastDoneAt = fixedNow - 400 * day, frequency = ReminderInterval.Fixed(365))
        )
        every { repository.observeEvents() } returns flowOf(events)
        useCase().test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertEquals("daily", result[0].event.id)
            assertEquals(2.0f, result[0].urgencyScore, 0.001f)
            assertEquals("yearly", result[1].event.id)
            assertEquals(400f / 365f, result[1].urgencyScore, 0.001f)
            awaitComplete()
        }
    }
    @Test
    fun automaticIntervalUsesCreationUntilFirstOccurrence() = runTest {
        val event = Event("1", "Title", "", fixedNow - 5 * day, ReminderInterval.Automatic)
        every { repository.observeEvents() } returns flowOf(listOf(event))
        useCase().test {
            assertEquals(1, awaitItem().size)
            awaitComplete()
        }
    }

    @Test
    fun latestOccurrenceResetsReminder() = runTest {
        val event = Event("1", "Title", "", fixedNow - 20 * day, ReminderInterval.Automatic,
            listOf(EventOccurrence("old", fixedNow - 10 * day), EventOccurrence("new", fixedNow - 2 * day)))
        every { repository.observeEvents() } returns flowOf(listOf(event))
        useCase().test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }
}
