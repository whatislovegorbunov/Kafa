package com.example.kafa.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

private val dateFormat = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm").withResolverStyle(ResolverStyle.STRICT)
fun formatOccurrenceDate(timestamp: Long): String = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(dateFormat)

@Composable
fun OccurrenceDialog(onDismiss: () -> Unit, onSave: (Long, String?) -> Unit) {
    var date by remember { mutableStateOf(formatOccurrenceDate(System.currentTimeMillis())) }
    var comment by remember { mutableStateOf("") }
    val timestamp = remember(date) {
        runCatching { LocalDateTime.parse(date, dateFormat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() }.getOrNull()
    }
    val valid = timestamp != null && timestamp <= System.currentTimeMillis()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Повторение события") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(date, { date = it }, label = { Text("Дата и время: дд.мм.гггг чч:мм") }, isError = !valid, singleLine = true)
                if (!valid) Text("Укажите корректные дату и время в прошлом", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(comment, { comment = it }, label = { Text("Комментарий (необязательно)") })
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = { onSave(timestamp!!, comment) }) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
