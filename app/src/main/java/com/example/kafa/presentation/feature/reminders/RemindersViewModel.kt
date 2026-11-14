package com.example.kafa.presentation.feature.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kafa.data.api.EventRepository
import com.example.kafa.domain.model.Recommendation
import com.example.kafa.domain.usecase.GetRecommendationsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RemindersUiState(val recommendations: List<Recommendation> = emptyList(), val isLoading: Boolean = true, val error: String? = null)

class RemindersViewModel @Inject constructor(
    private val repository: EventRepository,
    getRecommendations: GetRecommendationsUseCase
) : ViewModel() {
    val uiState = getRecommendations().map { RemindersUiState(recommendations = it, isLoading = false) }
        .catch { emit(RemindersUiState(isLoading = false, error = it.message ?: "Не удалось загрузить напоминания")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RemindersUiState())

    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    fun addOccurrence(id: String, occurredAt: Long, comment: String?) {
        viewModelScope.launch { repository.addOccurrence(id, occurredAt, comment).onFailure { _errors.emit(it.message ?: "Не удалось добавить повторение") } }
    }
}
