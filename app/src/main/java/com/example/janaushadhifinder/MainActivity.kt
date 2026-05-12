package com.example.janaushadhifinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.janaushadhifinder.ui.theme.JanAushadhiFinderTheme
import com.google.firebase.auth.FirebaseAuth

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
    var userName by remember { mutableStateOf("") }

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
        showSplash -> SplashScreen(onFinished = { showSplash = false })
        !isLoggedIn -> LoginScreen(onLoginSuccess = { name ->
            userName = name
            isLoggedIn = true
        })
        else -> MainScreen(
            userName = userName,
            onLogout = {
                FirebaseAuth.getInstance().signOut()
                isLoggedIn = false
                userName = ""
            }
        )
    }
}

@Composable
fun MainScreen(
    userName: String,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var storeFocusRequest by remember { mutableStateOf(0) }
    var requestedMedicineName by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = null) },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text("Stores") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
                    label = { Text("Reminders") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                    label = { Text("Savings") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
                    label = { Text("AI") }
                )
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> MedicineSearchScreen(
                    onViewNearbyStore = { medicine ->
                        requestedMedicineName = medicine.brandName
                        storeFocusRequest += 1
                        selectedTab = 1
                    }
                )
                1 -> StoreLocatorScreen(
                    focusRequest = storeFocusRequest,
                    requestedMedicineName = requestedMedicineName
                )
                2 -> ReminderScreen()
                3 -> SavingsCalculatorScreen()
                4 -> AiAssistantScreen()
                5 -> ProfileScreen(userName = userName, onLogout = onLogout)
            }
        }
    }
}
