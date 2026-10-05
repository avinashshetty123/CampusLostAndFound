package com.example.lostandfoundfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.data.SessionManager
import com.example.lostandfoundfrontend.data.TokenStore
import com.example.lostandfoundfrontend.ui.screens.*
import com.example.lostandfoundfrontend.ui.theme.Charcoal
import com.example.lostandfoundfrontend.ui.theme.LostAndFoundTheme
import com.example.lostandfoundfrontend.ui.theme.LostRed

// Bottom-nav tabs fade through instead of sliding sideways
private val tabEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(tween(260, delayMillis = 60)) + scaleIn(initialScale = 0.96f, animationSpec = tween(260, delayMillis = 60))
}
private val tabExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(tween(120))
}

/** Navigate to [route] and make it the only screen on the back stack. */
private fun NavHostController.navigateClearingStack(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

/** Switch between bottom-nav tabs without piling up copies on the back stack. */
private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo("home") { inclusive = route == "home" }
        launchSingleTop = true
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LostAndFoundTheme {
                val navController = rememberNavController()
                val viewModel: LostFoundViewModel = viewModel()
                val statsState by viewModel.statsState.collectAsState()

                val startDest = remember { if (TokenStore.isLoggedIn()) "home" else "entry" }

                // Token rejected by the server → back to login with an explanation
                val sessionExpired by SessionManager.expired.collectAsState()
                LaunchedEffect(sessionExpired) {
                    if (sessionExpired) {
                        viewModel.onSessionExpired()
                        SessionManager.consume()
                        navController.navigateClearingStack("auth")
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDest,
                    enterTransition = { slideInHorizontally(tween(280)) { it / 4 } + fadeIn(tween(280)) },
                    exitTransition = { slideOutHorizontally(tween(220)) { -it / 8 } + fadeOut(tween(200)) },
                    popEnterTransition = { slideInHorizontally(tween(220)) { -it / 8 } + fadeIn(tween(220)) },
                    popExitTransition = { slideOutHorizontally(tween(280)) { it / 4 } + fadeOut(tween(200)) }
                ) {

                    composable("entry", exitTransition = { fadeOut(tween(250)) }) {
                        EntryScreen(stats = statsState, onGetStarted = { navController.navigate("auth") })
                    }

                    composable("auth") {
                        AuthScreen(
                            viewModel = viewModel,
                            onLoginSuccess = { navController.navigateClearingStack("home") }
                        )
                    }

                    composable("home", enterTransition = tabEnter, popEnterTransition = tabEnter) {
                        HomeScreen(
                            viewModel = viewModel,
                            onItemClick = { item -> navController.navigate("details/${item.id}") },
                            onReportClick = { navController.navigateTab("report") },
                            onProfileClick = { navController.navigateTab("profile") }
                        )
                    }

                    composable("report", enterTransition = tabEnter, exitTransition = tabExit, popExitTransition = tabExit) {
                        ReportScreen(
                            viewModel = viewModel,
                            onItemReported = { navController.navigateTab("home") },
                            onHomeClick = { navController.navigateTab("home") },
                            onProfileClick = { navController.navigateTab("profile") }
                        )
                    }

                    composable("details/{itemId}") { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                        val detailState by viewModel.detailState.collectAsState()
                        LaunchedEffect(itemId) { viewModel.openItem(itemId) }
                        val item = detailState.item?.takeIf { it.id == itemId }
                        if (item != null) {
                            ItemDetailsScreen(
                                item = item,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (detailState.error != null) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(detailState.error ?: "Item not found", color = LostRed)
                                        Spacer(Modifier.height(8.dp))
                                        TextButton(onClick = { navController.popBackStack() }) { Text("Go back", color = Charcoal) }
                                    }
                                } else {
                                    CircularProgressIndicator(color = Charcoal)
                                }
                            }
                        }
                    }

                    composable("profile", enterTransition = tabEnter, exitTransition = tabExit, popEnterTransition = tabEnter, popExitTransition = tabExit) {
                        ProfileScreen(
                            viewModel = viewModel,
                            onLogout = {
                                viewModel.logout { navController.navigateClearingStack("auth") }
                            },
                            onMyReports = { navController.navigate("my_reports") },
                            onSavedItems = { navController.navigate("saved_items") },
                            onEditProfile = { navController.navigate("edit_profile") },
                            onHomeClick = { navController.navigateTab("home") },
                            onReportClick = { navController.navigateTab("report") }
                        )
                    }

                    composable("my_reports") {
                        MyReportsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onItemClick = { item -> navController.navigate("details/${item.id}") }
                        )
                    }

                    composable("saved_items") {
                        SavedItemsScreen(
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
                }
            }
        }
    }
}
