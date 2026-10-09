package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.local.BalajiDatabase
import com.example.data.local.UserPreferences
import com.example.data.repository.BalajiRepository
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyBookingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    SPLASH,
    HOME,
    MY_BOOKINGS,
    PROFILE,
    ADMIN
}

class MainActivity : ComponentActivity() {

    private lateinit var repository: BalajiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = BalajiDatabase.getDatabase(this, lifecycleScope)
        val userPreferences = UserPreferences(this)
        val firebaseManager = com.example.data.remote.FirebaseManager(this)
        repository = BalajiRepository(
            dao = database.balajiDao(),
            userPreferences = userPreferences,
            firebaseManager = firebaseManager
        )

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }

                AnimatedContent(
                    targetState = currentScreen,
                    label = "AppScreenTransition"
                ) { screen ->
                    when (screen) {
                        AppScreen.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.HOME -> {
                            HomeScreen(
                                repository = repository,
                                onNavigateToMyBookings = {
                                    currentScreen = AppScreen.MY_BOOKINGS
                                },
                                onNavigateToProfile = {
                                    currentScreen = AppScreen.PROFILE
                                },
                                onNavigateToAdmin = {
                                    currentScreen = AppScreen.ADMIN
                                }
                            )
                        }

                        AppScreen.MY_BOOKINGS -> {
                            MyBookingsScreen(
                                repository = repository,
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.PROFILE -> {
                            com.example.ui.screens.UserProfileScreen(
                                repository = repository,
                                onBack = {
                                    currentScreen = AppScreen.HOME
                                },
                                onNavigateToMyBookings = {
                                    currentScreen = AppScreen.MY_BOOKINGS
                                },
                                onLogout = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }

                        AppScreen.ADMIN -> {
                            AdminDashboardScreen(
                                repository = repository,
                                onBackToCustomerView = {
                                    currentScreen = AppScreen.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
