package com.example.kafa.presentation.feature.reminders

import app.cash.turbine.test
import com.example.kafa.data.api.EventRepository
import com.example.kafa.domain.model.Event
import com.example.kafa.domain.model.Recommendation
import com.example.kafa.domain.usecase.GetRecommendationsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<EventRepository>()
    private val getRecommendations = mockk<GetRecommendationsUseCase>()

    @Before fun setup() = Dispatchers.setMain(dispatcher)
    @After fun teardown() = Dispatchers.resetMain()

    @Test fun recommendationsAreShown() = runTest(dispatcher) {
        val recommendations = listOf(Recommendation(Event("1", "Event", "", 0), 1f, 5, "Reminder"))
        every { getRecommendations() } returns flowOf(recommendations)
        val model = RemindersViewModel(repository, getRecommendations)
        model.uiState.test {
            assertTrue(awaitItem().isLoading)
            advanceUntilIdle()
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(recommendations, state.recommendations)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun errorsAreExposed() = runTest(dispatcher) {
        every { getRecommendations() } returns flow { throw IllegalStateException("failed") }
        val model = RemindersViewModel(repository, getRecommendations)
        model.uiState.test {
            awaitItem()
            advanceUntilIdle()
            assertEquals("failed", awaitItem().error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun occurrenceIsSaved() = runTest(dispatcher) {
        every { getRecommendations() } returns flowOf(emptyList())
        coEvery { repository.addOccurrence(any(), any(), any()) } returns Result.success(Unit)
        val model = RemindersViewModel(repository, getRecommendations)
        model.addOccurrence("1", 123L, "comment")
        advanceUntilIdle()
        coVerify { repository.addOccurrence("1", 123L, "comment") }
    }
}
