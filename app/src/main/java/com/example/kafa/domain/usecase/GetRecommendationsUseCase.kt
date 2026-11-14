package com.example.kafa.domain.usecase

import com.example.kafa.data.api.EventRepository
import com.example.kafa.domain.model.Recommendation
import com.example.kafa.domain.reminder.ReminderPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRecommendationsUseCase @Inject constructor(
    private val eventRepository: EventRepository
) {
    operator fun invoke(): Flow<List<Recommendation>> =
        eventRepository.observeEvents().map { events ->
            val now = System.currentTimeMillis()

            events
                .map { event ->
                    val overdueDays = ReminderPolicy.elapsedDays(event, now)
                    val expectedDays = event.reminderInterval.days.toLong()
                    val urgencyScore = if (expectedDays == 0L) 0f else overdueDays.toFloat() / expectedDays.toFloat()
                    val message = if (event.history.isEmpty()) {
                        "Ещё ни разу не отмечено"
                    } else {
                        "Дней с последнего повторения: $overdueDays"
                    }
                    Recommendation(event, urgencyScore, overdueDays, message)
                }
                .filter { it.urgencyScore >= 1.0f }
                .sortedByDescending { it.urgencyScore }
                .take(MAX_RECOMMENDATIONS)
        }

    private companion object {
        const val MAX_RECOMMENDATIONS = 5
    }
}
