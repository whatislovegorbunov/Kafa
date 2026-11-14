package com.example.kafa.domain.reminder

import com.example.kafa.domain.model.Event

object ReminderPolicy {
    const val MILLIS_PER_DAY = 86_400_000L
    private const val REPEAT_INTERVAL = 7 * MILLIS_PER_DAY

    fun latestOccurrence(event: Event) = event.history.maxWithOrNull(compareBy({ it.occurredAt }, { it.id }))

    fun elapsedDays(event: Event, now: Long): Long =
        ((now - (latestOccurrence(event)?.occurredAt ?: event.createdAt)).coerceAtLeast(0L)) / MILLIS_PER_DAY

    fun isDue(event: Event, now: Long): Boolean = elapsedDays(event, now) >= event.reminderInterval.days

    fun shouldNotify(event: Event, now: Long, lastSentAt: Long?, sentForOccurrenceId: String?): Boolean {
        if (!isDue(event, now)) return false
        val currentOccurrenceId = latestOccurrence(event)?.id
        return lastSentAt == null || sentForOccurrenceId != currentOccurrenceId ||
            now - lastSentAt >= REPEAT_INTERVAL
    }
}
