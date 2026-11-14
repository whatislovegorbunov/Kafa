package com.example.kafa.presentation.feature.addevent

import app.cash.turbine.test
import com.example.kafa.data.api.EventRepository
import com.example.kafa.domain.model.ReminderInterval
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddEventViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val eventRepository = mockk<EventRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_hasEmptyFieldsAndNotSaving() = runTest(testDispatcher) {
        val viewModel = AddEventViewModel(eventRepository)

        val state = viewModel.uiState.value
        assertEquals("", state.title)
        assertEquals("", state.content)
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
        assertNull(state.error)
    }

    @Test
    fun onTitleChange_updatesTitleInState() = runTest(testDispatcher) {
        val viewModel = AddEventViewModel(eventRepository)

        viewModel.uiState.test {
            assertEquals("", awaitItem().title)
            viewModel.onTitleChange("New Title")
            assertEquals("New Title", awaitItem().title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onContentChange_updatesContentInState() = runTest(testDispatcher) {
        val viewModel = AddEventViewModel(eventRepository)

        viewModel.uiState.test {
            assertEquals("", awaitItem().content)
            viewModel.onContentChange("New Content")
            assertEquals("New Content", awaitItem().content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun saveEvent_withNonBlankTitle_callsRepositoryAndSetsIsSaved() = runTest(testDispatcher) {
        coEvery { eventRepository.addEvent(any(), any(), any()) } returns Result.success(Unit)
        val viewModel = AddEventViewModel(eventRepository)
        viewModel.onTitleChange("Test Title")
        viewModel.onContentChange("Test Content")

        viewModel.saveEvent()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertTrue(state.isSaved)
        assertNull(state.error)

        coVerify(exactly = 1) { eventRepository.addEvent("Test Title", "Test Content", ReminderInterval.Automatic) }
    }

    @Test
    fun saveEvent_withBlankTitle_doesNotCallRepository() = runTest(testDispatcher) {
        val viewModel = AddEventViewModel(eventRepository)

        viewModel.saveEvent()
        advanceUntilIdle()

        coVerify(exactly = 0) { eventRepository.addEvent(any(), any(), any()) }
    }

    @Test
    fun saveEvent_whenRepositoryFails_setsErrorAndDoesNotSetSaved() = runTest(testDispatcher) {
        val errorMessage = "Save failed"
        coEvery { eventRepository.addEvent(any(), any(), any()) } returns Result.failure(RuntimeException(errorMessage))
        val viewModel = AddEventViewModel(eventRepository)
        viewModel.onTitleChange("Test")

        viewModel.saveEvent()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun saveEvent_setsIsSavingTrueDuringExecution_andFalseAfter() = runTest(testDispatcher) {
        val latch = CompletableDeferred<Unit>()
        coEvery { eventRepository.addEvent(any(), any(), any()) } coAnswers {
            latch.await()
            Result.success(Unit)
        }
        val viewModel = AddEventViewModel(eventRepository)
        viewModel.onTitleChange("Test")

        viewModel.uiState.test {
            awaitItem()
            viewModel.saveEvent()
            advanceUntilIdle()

            val savingState = awaitItem()
            assertTrue(savingState.isSaving)
            assertFalse(savingState.isSaved)

            latch.complete(Unit)
            advanceUntilIdle()

            val savedState = awaitItem()
            assertFalse(savedState.isSaving)
            assertTrue(savedState.isSaved)

            cancelAndIgnoreRemainingEvents()
        }
    }
    @Test
    fun fixedIntervalMustBePositiveAndIsPassedToRepository() = runTest(testDispatcher) {
        coEvery { eventRepository.addEvent(any(), any(), any()) } returns Result.success(Unit)
        val viewModel = AddEventViewModel(eventRepository)
        viewModel.onTitleChange("Event")
        viewModel.onAutomaticIntervalChange(false)
        viewModel.onIntervalDaysChange("0")
        viewModel.saveEvent()
        advanceUntilIdle()
        coVerify(exactly = 0) { eventRepository.addEvent(any(), any(), any()) }
        viewModel.onIntervalDaysChange("14")
        viewModel.saveEvent()
        advanceUntilIdle()
        coVerify(exactly = 1) { eventRepository.addEvent("Event", "", ReminderInterval.Fixed(14)) }
    }
}
