package com.example.kafa.presentation.feature.addevent

data class AddEventUiState(
    val title: String = "",
    val content: String = "",
    val automaticInterval: Boolean = true,
    val intervalDays: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)
