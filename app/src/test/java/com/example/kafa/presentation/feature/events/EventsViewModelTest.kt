package com.example.kafa.presentation.feature.events

import app.cash.turbine.test
import com.example.kafa.domain.model.Event
import com.example.kafa.data.api.EventRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<EventRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): EventsViewModel {
        return EventsViewModel(repository)
    }

    @Test
    fun initialState_isLoadingWithEmptyEvents() = runTest(testDispatcher) {
        every { repository.observeEvents() } returns flowOf(emptyList())
        val viewModel = EventsViewModel(repository)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            assertTrue(initial.events.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadEvents_whenRepositoryReturnsData_emitsSuccessStateWithEvents() = runTest(testDispatcher) {
        val events = listOf(
            Event("1", "Title 1", "Content 1", 1000L),
            Event("2", "Title 2", "Content 2", 2000L)
        )
        every { repository.observeEvents() } returns flowOf(events)
        val viewModel = EventsViewModel(repository)

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            advanceUntilIdle()
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(events, loaded.events)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadEvents_whenRepositoryReturnsEmpty_emitsSuccessStateWithEmptyList() = runTest(testDispatcher) {
        every { repository.observeEvents() } returns flowOf(emptyList())
        val viewModel = EventsViewModel(repository)

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            advanceUntilIdle()
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertTrue(loaded.events.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteEvent_callsRepositoryDeleteEvent() = runTest(testDispatcher) {
        coEvery { repository.deleteEvent(any()) } returns Result.success(Unit)
        every { repository.observeEvents() } returns flowOf(emptyList())
        val viewModel = EventsViewModel(repository)
        viewModel.deleteEvent("event-id")
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.deleteEvent("event-id") }
    }

    @Test
    fun observeEvents_whenFlowEmitsError_setsErrorInUiState() = runTest(testDispatcher) {
        every { repository.observeEvents() } returns flow { throw RuntimeException("DB error") }
        val viewModel = EventsViewModel(repository)

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            advanceUntilIdle()
            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertNotNull(errorState.error)
            assertEquals("DB error", errorState.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteEvent_whenRepositoryFails_emitsDeleteError() = runTest(testDispatcher) {
        coEvery { repository.deleteEvent(any()) } returns Result.failure(RuntimeException("Delete failed"))
        every { repository.observeEvents() } returns flowOf(emptyList())
        val viewModel = EventsViewModel(repository)

        viewModel.errors.test {
            viewModel.deleteEvent("event-id")
            advanceUntilIdle()
            val error = awaitItem()
            assertEquals("Delete failed", error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun markAsDone_callsRepositoryMarkAsDone() = runTest(testDispatcher) {
        coEvery { repository.addOccurrence(any(), any(), any()) } returns Result.success(Unit)
        every { repository.observeEvents() } returns flowOf(emptyList())
        val viewModel = EventsViewModel(repository)
        viewModel.addOccurrence("event-id", 123L, null)
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.addOccurrence("event-id", 123L, null) }
    }
}
