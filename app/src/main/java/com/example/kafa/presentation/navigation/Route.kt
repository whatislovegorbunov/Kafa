package com.example.kafa.presentation.navigation

sealed interface Route {
    val route: String

    data object Reminders : Route { override val route = "reminders" }
    data object Events : Route { override val route = "events" }
    data object AddEvent : Route { override val route = "add_event" }
}
