package com.example.janaushadhifinder

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {

    var isLoginMode    by remember { mutableStateOf(true) }
    var name           by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var showPassword   by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf("") }
    var isLoading      by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val auth = remember { FirebaseAuth.getInstance() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00897B))
    ) {
        // Top logo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "💊", fontSize = 52.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Jan-Aushadhi Finder",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Affordable medicines for everyone",
                fontSize = 13.sp,
                color = Color(0xFFB2DFDB)
            )
        }

        // Bottom card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Tab switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isLoginMode) Color(0xFF00897B) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                isLoginMode = true
                                errorMessage = ""
                                successMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            color = if (isLoginMode) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (!isLoginMode) Color(0xFF00897B) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                isLoginMode = false
                                errorMessage = ""
                                successMessage = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            color = if (!isLoginMode) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Name field — register only
                if (!isLoginMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF00897B)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B),
                            unfocusedBorderColor = Color.LightGray
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF00897B)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00897B),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF00897B)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword)
                                    Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00897B),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                // Forgot password — login mode only
                if (isLoginMode) {
                    TextButton(
                        onClick = {
                            if (email.isBlank() || !email.contains("@")) {
                                errorMessage = "Enter your email first to reset password"
                            } else {
                                auth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener {
                                        successMessage = "Password reset email sent to $email"
                                        errorMessage = ""
                                    }
                                    .addOnFailureListener {
                                        errorMessage = "Failed to send reset email"
                                    }
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFF00897B),
                            fontSize = 12.sp
                        )
                    }
                }

                // Error message
                if (errorMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFC62828),
                            fontSize = 12.sp
                        )
                    }
                }

                // Success message
                if (successMessage.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successMessage,
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Main button
                Button(
                    onClick = {
                        errorMessage = ""
                        successMessage = ""

                        // Validation
                        when {
                            email.isBlank() -> {
                                errorMessage = "Please enter your email"
                                return@Button
                            }
                            !email.contains("@") -> {
                                errorMessage = "Please enter a valid email"
                                return@Button
                            }
                            password.length < 6 -> {
                                errorMessage = "Password must be at least 6 characters"
                                return@Button
                            }
                            !isLoginMode && name.isBlank() -> {
                                errorMessage = "Please enter your full name"
                                return@Button
                            }
                        }

                        isLoading = true

                        if (isLoginMode) {
                            // ── REAL LOGIN via Firebase Auth ──────────────
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnSuccessListener { result ->
                                    val user = result.user
                                    val displayName = user?.displayName
                                        ?: user?.email?.substringBefore("@")
                                        ?: "User"
                                    isLoading = false
                                    onLoginSuccess(displayName)
                                }
                                .addOnFailureListener { exception ->
                                    isLoading = false
                                    errorMessage = when {
                                        exception.message?.contains("no user") == true ->
                                            "No account found. Please register first."
                                        exception.message?.contains("password") == true ->
                                            "Incorrect password. Please try again."
                                        exception.message?.contains("badly formatted") == true ->
                                            "Please enter a valid email address."
                                        exception.message?.contains("network") == true ->
                                            "No internet connection. Check your network."
                                        else ->
                                            "Login failed. Please try again."
                                    }
                                }
                        } else {
                            // ── REAL REGISTER via Firebase Auth ───────────
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnSuccessListener { result ->
                                    // Save display name to Firebase profile
                                    val profileUpdate = UserProfileChangeRequest.Builder()
                                        .setDisplayName(name.trim())
                                        .build()
                                    result.user?.updateProfile(profileUpdate)
                                        ?.addOnSuccessListener {
                                            isLoading = false
                                            onLoginSuccess(name.trim())
                                        }
                                        ?.addOnFailureListener {
                                            isLoading = false
                                            onLoginSuccess(name.trim())
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    isLoading = false
                                    errorMessage = when {
                                        exception.message?.contains("already in use") == true ->
                                            "This email is already registered. Please login."
                                        exception.message?.contains("badly formatted") == true ->
                                            "Please enter a valid email address."
                                        exception.message?.contains("weak") == true ->
                                            "Password is too weak. Use at least 6 characters."
                                        exception.message?.contains("network") == true ->
                                            "No internet connection. Check your network."
                                        else ->
                                            "Registration failed. Please try again."
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (isLoginMode) "Login" else "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Guest option
                TextButton(onClick = { onLoginSuccess("Guest") }) {
                    Text(
                        text = "Continue as Guest",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}