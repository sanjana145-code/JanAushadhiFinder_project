package com.example.janaushadhifinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.firebase.auth.FirebaseAuth
import com.example.janaushadhifinder.ui.theme.JanAushadhiFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JanAushadhiFinderTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var showSplash by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var userName   by remember { mutableStateOf("") }

    // Check if user already logged in
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            userName = currentUser.displayName
                ?: currentUser.email?.substringBefore("@")
                        ?: "User"
            isLoggedIn = true
        }
    }

    when {
        showSplash -> {
            SplashScreen(onFinished = { showSplash = false })
        }
        !isLoggedIn -> {
            LoginScreen(onLoginSuccess = { name ->
                userName = name
                isLoggedIn = true
            })
        }
        else -> {
            MainScreen(
                userName = userName,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    isLoggedIn = false
                    userName = ""
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    userName: String,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("💊") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("🗺️") },
                    label = { Text("Stores") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("🔔") },
                    label = { Text("Reminders") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Text("💰") },
                    label = { Text("Savings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Text("👤") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> MedicineSearchScreen()
                1 -> StoreLocatorScreen()
                2 -> ReminderScreen()
                3 -> SavingsCalculatorScreen()
                4 -> ProfileScreen(
                    userName = userName,
                    onLogout = onLogout
                )
            }
        }
    }
}