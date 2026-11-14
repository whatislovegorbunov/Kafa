package com.example.kafa.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kafa.presentation.feature.addevent.AddEventScreen
import com.example.kafa.presentation.feature.events.EventsScreen
import com.example.kafa.presentation.feature.reminders.RemindersScreen

@Composable
fun AppNavigation(viewModelFactory: ViewModelProvider.Factory, remindersRequest: Int = 0) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(remindersRequest) {
        if (remindersRequest > 0) navController.popBackStack(Route.Reminders.route, false)
    }

    Scaffold(bottomBar = {
        if (currentRoute == Route.Reminders.route || currentRoute == Route.Events.route) {
            NavigationBar {
                listOf(Route.Reminders to "Напоминания", Route.Events to "События").forEach { (destination, label) ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(Route.Reminders.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(if (destination == Route.Reminders) Icons.Default.Notifications else Icons.AutoMirrored.Filled.List, contentDescription = null) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = Route.Reminders.route, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable(Route.Reminders.route) { RemindersScreen(viewModelFactory) }
            composable(Route.Events.route) {
                EventsScreen(viewModelFactory, onAddEventClick = { navController.navigate(Route.AddEvent.route) })
            }
            composable(Route.AddEvent.route) {
                AddEventScreen(viewModelFactory, onBack = { navController.popBackStack() }, onSaved = { navController.popBackStack() })
            }
        }
    }
}
