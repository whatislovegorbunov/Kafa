package com.example.kafa.presentation.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kafa.data.api.EventRepository
import com.example.kafa.domain.model.Event
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventsUiState(val events: List<Event> = emptyList(), val isLoading: Boolean = true, val error: String? = null)

class EventsViewModel @Inject constructor(private val repository: EventRepository) : ViewModel() {
    val uiState = repository.observeEvents().map { EventsUiState(events = it, isLoading = false) }
        .catch { emit(EventsUiState(isLoading = false, error = it.message ?: "Не удалось загрузить события")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EventsUiState())

    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    fun deleteEvent(id: String) {
        viewModelScope.launch { repository.deleteEvent(id).onFailure { _errors.emit(it.message ?: "Не удалось удалить событие") } }
    }

    fun addOccurrence(id: String, occurredAt: Long, comment: String?) {
        viewModelScope.launch { repository.addOccurrence(id, occurredAt, comment).onFailure { _errors.emit(it.message ?: "Не удалось добавить повторение") } }
    }
}
