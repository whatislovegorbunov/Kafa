package com.example.kafa.domain.reminder

import com.example.kafa.domain.model.Event
import com.example.kafa.domain.model.EventOccurrence
import com.example.kafa.domain.model.ReminderInterval
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPolicyTest {
    private val day = ReminderPolicy.MILLIS_PER_DAY
    private val now = 100 * day
    private fun event(createdAt: Long = now - 20 * day, history: List<EventOccurrence> = emptyList()) =
        Event("id", "Title", "", createdAt, ReminderInterval.Automatic, history)

    @Test
    fun firstNotificationIsDueAfterFiveDaysFromCreation() {
        assertFalse(ReminderPolicy.shouldNotify(event(now - 5 * day + 1), now, null, null))
        assertTrue(ReminderPolicy.shouldNotify(event(now - 5 * day), now, null, null))
    }

    @Test
    fun repeatedNotificationWaitsSevenDays() {
        assertFalse(ReminderPolicy.shouldNotify(event(), now, now - 7 * day + 1, null))
        assertTrue(ReminderPolicy.shouldNotify(event(), now, now - 7 * day, null))
    }

    @Test
    fun recentOccurrenceResetsIntervalAndOlderEntryDoesNotCauseExtraNotification() {
        val recent = EventOccurrence("recent", now - 2 * day)
        assertFalse(ReminderPolicy.shouldNotify(event(history = listOf(recent)), now, now - day, null))
        val latest = EventOccurrence("latest", now - 10 * day)
        val older = EventOccurrence("older", now - 15 * day)
        assertFalse(ReminderPolicy.shouldNotify(event(history = listOf(latest, older)), now, now - day, "latest"))
    }

    @Test
    fun changedLatestOccurrenceCanNotifyOnceItsIntervalHasElapsed() {
        val occurrence = EventOccurrence("new", now - 6 * day)
        assertTrue(ReminderPolicy.shouldNotify(event(history = listOf(occurrence)), now, now - day, "old"))
    }

    @Test
    fun fixedIntervalDeterminesWhenNotificationIsDue() {
        val fixed = event(createdAt = now - 6 * day).copy(reminderInterval = ReminderInterval.Fixed(10))
        assertFalse(ReminderPolicy.shouldNotify(fixed, now, null, null))
    }
}
