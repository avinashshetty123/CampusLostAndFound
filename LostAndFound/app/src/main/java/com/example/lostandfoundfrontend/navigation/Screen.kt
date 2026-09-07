package com.example.lostandfoundfrontend.navigation

sealed class Screen(val route: String) {
    object Entry : Screen("entry")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Report : Screen("report")
    object Profile : Screen("profile")
}