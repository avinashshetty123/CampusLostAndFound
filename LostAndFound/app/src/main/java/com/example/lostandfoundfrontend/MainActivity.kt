package com.example.lostandfoundfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.ui.screens.*
import com.example.lostandfoundfrontend.ui.theme.LostAndFoundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LostAndFoundTheme {
                val navController = rememberNavController()
                val viewModel: LostFoundViewModel = viewModel()

                val startDest = if (com.example.lostandfoundfrontend.data.TokenStore.isLoggedIn()) "home" else "entry"

                NavHost(navController = navController, startDestination = startDest) {

                    composable("entry") {
                        EntryScreen(onGetStarted = { navController.navigate("auth") })
                    }

                    composable("auth") {
                        AuthScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("entry") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onItemClick = { item -> navController.navigate("details/${item.id}") },
                            onReportClick = { navController.navigate("report") },
                            onProfileClick = { navController.navigate("profile") }
                        )
                    }

                    composable("report") {
                        ReportScreen(
                            viewModel = viewModel,
                            onItemReported = { navController.popBackStack() },
                            onHomeClick = {
                                navController.navigate("home") { popUpTo("home") { inclusive = true } }
                            },
                            onProfileClick = { navController.navigate("profile") }
                        )
                    }

                    composable("details/{itemId}") { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                        // Find item from current items state
                        val itemsState = viewModel.itemsState.value
                        val myItemsState = viewModel.myItemsState.value
                        val item = (itemsState.items + myItemsState.items).firstOrNull { it.id == itemId }
                        if (item != null) {
                            ItemDetailsScreen(
                                item = item,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }

                    composable("profile") {
                        ProfileScreen(
                            viewModel = viewModel,
                            onLogout = {
                                viewModel.logout {
                                    navController.navigate("auth") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            onMyReports = { navController.navigate("my_reports") },
                            onEditProfile = { navController.navigate("edit_profile") },
                            onDarkMode = { navController.navigate("dark_mode") }
                        )
                    }

                    composable("my_reports") {
                        MyReportsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onItemClick = { item -> navController.navigate("details/${item.id}") }
                        )
                    }

                    composable("edit_profile") {
                        EditProfileScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onSaved = { navController.popBackStack() }
                        )
                    }

                    composable("dark_mode") {
                        DarkModeScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
