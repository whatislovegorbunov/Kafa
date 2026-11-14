package com.example.kafa.presentation.feature.events

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kafa.domain.model.Event
import com.example.kafa.domain.model.ReminderInterval
import com.example.kafa.presentation.components.OccurrenceDialog
import com.example.kafa.presentation.components.formatOccurrenceDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(viewModelFactory: ViewModelProvider.Factory, onAddEventClick: () -> Unit, viewModel: EventsViewModel = viewModel(factory = viewModelFactory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    LaunchedEffect(viewModel) { viewModel.errors.collect { snackbar.showSnackbar(it) } }
    Scaffold(
        topBar = { TopAppBar(title = { Text("События") }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = { FloatingActionButton(onClick = onAddEventClick) { Icon(Icons.Default.Add, contentDescription = "Добавить событие") } }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(state.error.orEmpty(), Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                else -> EventsContent(state.events, onAdd = { selectedEvent = it }, onDelete = viewModel::deleteEvent)
            }
        }
    }
    selectedEvent?.let { event ->
        OccurrenceDialog(onDismiss = { selectedEvent = null }, onSave = { timestamp, comment ->
            viewModel.addOccurrence(event.id, timestamp, comment)
            selectedEvent = null
        })
    }
}

@Composable
private fun EventsContent(events: List<Event>, onAdd: (Event) -> Unit, onDelete: (String) -> Unit) {
    if (events.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Нет событий") }
    } else {
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(events, key = { it.id }) { event ->
                EventCard(event, onAdd = { onAdd(event) }, onDelete = { onDelete(event.id) })
            }
        }
    }
}

@Composable
private fun EventCard(event: Event, onAdd: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(event.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Удалить событие") }
            }
            if (event.content.isNotBlank()) Text(event.content)
            Text("Создано: ${formatOccurrenceDate(event.createdAt)}", style = MaterialTheme.typography.bodySmall)
            val interval = when (val value = event.reminderInterval) {
                ReminderInterval.Automatic -> "Автоматически (${value.days} дней)"
                is ReminderInterval.Fixed -> "${value.days} дней"
            }
            Text("Интервал: $interval", style = MaterialTheme.typography.bodySmall)
            if (event.history.isNotEmpty()) {
                Text("История повторений", style = MaterialTheme.typography.titleSmall)
                event.history.forEach { occurrence ->
                    Text(formatOccurrenceDate(occurrence.occurredAt) + (occurrence.comment?.let { " — $it" } ?: ""))
                }
            }
            TextButton(onClick = onAdd) { Text("Добавить повторение") }
        }
    }
}

