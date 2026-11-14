package com.example.kafa.presentation.feature.reminders

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kafa.domain.model.Event
import com.example.kafa.domain.model.Recommendation
import com.example.kafa.notifications.ReminderWorker
import com.example.kafa.presentation.components.OccurrenceDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModelFactory: ViewModelProvider.Factory, viewModel: RemindersViewModel = viewModel(factory = viewModelFactory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    LaunchedEffect(viewModel) { viewModel.errors.collect { snackbar.showSnackbar(it) } }
    Scaffold(topBar = { TopAppBar(title = { Text("Напоминания") }) }, snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> Text(state.error.orEmpty(), Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                else -> RemindersContent(state.recommendations) { selectedEvent = it }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemindersContent(recommendations: List<Recommendation>, onSelect: (Event) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        NotificationPermissionPrompt()
        if (recommendations.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Пока нет напоминаний") }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recommendations, key = { it.event.id }) { recommendation ->
                    Card(modifier = Modifier.fillMaxWidth(), onClick = { onSelect(recommendation.event) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(recommendation.event.title, fontWeight = FontWeight.Bold)
                            Text(recommendation.message)
                            Text("Отметить повторение", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun NotificationPermissionPrompt() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    fun notificationsEnabled(): Boolean {
        val manager = NotificationManagerCompat.from(context)
        val channel = manager.getNotificationChannel(ReminderWorker.CHANNEL_ID)
        return manager.areNotificationsEnabled() && channel?.importance != NotificationManager.IMPORTANCE_NONE
    }
    var notificationsEnabled by remember { mutableStateOf(notificationsEnabled()) }
    var requestDenied by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsEnabled = notificationsEnabled()
        requestDenied = !granted
    }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsEnabled = notificationsEnabled()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    if (!notificationsEnabled) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Включите уведомления, чтобы получать напоминания вне приложения")
            TextButton(onClick = {
                if (Build.VERSION.SDK_INT >= 33 && !requestDenied && !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                }
            }) { Text("Включить уведомления") }
        }
    }
}

