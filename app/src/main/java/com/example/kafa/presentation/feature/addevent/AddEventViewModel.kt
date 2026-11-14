package com.example.kafa.presentation.feature.addevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kafa.data.api.EventRepository
import com.example.kafa.domain.model.ReminderInterval
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class AddEventViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEventUiState())
    val uiState: StateFlow<AddEventUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onContentChange(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun onAutomaticIntervalChange(automatic: Boolean) {
        _uiState.update { it.copy(automaticInterval = automatic) }
    }

    fun onIntervalDaysChange(days: String) {
        _uiState.update { it.copy(intervalDays = days.filter(Char::isDigit)) }
    }

    fun saveEvent() {
        val state = _uiState.value
        if (state.title.isBlank() || state.isSaving) return
        val interval = if (state.automaticInterval) ReminderInterval.Automatic else {
            val days = state.intervalDays.toIntOrNull()?.takeIf { it > 0 } ?: return
            ReminderInterval.Fixed(days)
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            eventRepository.addEvent(state.title.trim(), state.content, interval)
                .onSuccess { _uiState.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { e -> _uiState.update { it.copy(isSaving = false, error = e.message) } }
        }
    }
}
