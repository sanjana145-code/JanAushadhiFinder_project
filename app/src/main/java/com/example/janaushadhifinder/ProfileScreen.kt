package com.example.janaushadhifinder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

@Composable
fun ProfileScreen(
    userName: String,
    onLogout: () -> Unit
) {
    var showLogoutDialog    by remember { mutableStateOf(false) }
    var showEditDialog      by remember { mutableStateOf(false) }
    var showNotifDialog     by remember { mutableStateOf(false) }
    var showLocationDialog  by remember { mutableStateOf(false) }
    var showAboutDialog     by remember { mutableStateOf(false) }
    var displayName         by remember { mutableStateOf(userName) }
    var editNameTemp        by remember { mutableStateOf(userName) }
    var notifEnabled        by remember { mutableStateOf(true) }
    var reminderDays        by remember { mutableStateOf("3") }
    var searchRadius        by remember { mutableStateOf("10") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00897B))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Profile",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage your account",
                    color = Color(0xFFB2DFDB),
                    fontSize = 13.sp
                )
            }
        }

        // Profile card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF00897B), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.first().uppercaseChar().toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Jan-Aushadhi User",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Edit profile button
                OutlinedButton(
                    onClick = {
                        editNameTemp = displayName
                        showEditDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00897B)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF00897B)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Edit Profile", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(label = "Radius", value = "${searchRadius}km")
                    StatBox(label = "Reminders", value = "${reminderDays}d")
                    StatBox(label = "Notif", value = if (notifEnabled) "On" else "Off")
                }
            }
        }

        // Settings card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                ProfileOption(
                    icon = Icons.Default.Notifications,
                    title = "Notification Settings",
                    subtitle = if (notifEnabled) "Reminders enabled — $reminderDays days before"
                    else "Reminders disabled",
                    onClick = { showNotifDialog = true }
                )
                HorizontalDivider(color = Color(0xFFF5F5F5))
                ProfileOption(
                    icon = Icons.Default.LocationOn,
                    title = "Location Settings",
                    subtitle = "Search radius: ${searchRadius} km",
                    onClick = { showLocationDialog = true }
                )
                HorizontalDivider(color = Color(0xFFF5F5F5))
                ProfileOption(
                    icon = Icons.Default.Info,
                    title = "About Jan-Aushadhi",
                    subtitle = "PM Jan-Aushadhi Yojana info",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Logout",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    modifier = Modifier
                        .weight(1f)
                        .noRippleClickable { showLogoutDialog = true }
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Jan-Aushadhi Finder v1.0\nPM Jan-Aushadhi Yojana",
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    // ── Edit Profile Dialog ───────────────────────────────────────────────────
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Update your display name:", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editNameTemp,
                        onValueChange = { editNameTemp = it },
                        label = { Text("Display Name") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editNameTemp.isNotBlank()) {
                            displayName = editNameTemp.trim()
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Save", color = Color(0xFF00897B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // ── Notification Settings Dialog ──────────────────────────────────────────
    if (showNotifDialog) {
        AlertDialog(
            onDismissRequest = { showNotifDialog = false },
            title = { Text("Notification Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Reminders", fontSize = 14.sp)
                        Switch(
                            checked = notifEnabled,
                            onCheckedChange = { notifEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00897B)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Remind me before (days):", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reminderDays,
                        onValueChange = { reminderDays = it },
                        label = { Text("Days before") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("Save", color = Color(0xFF00897B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotifDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // ── Location Settings Dialog ──────────────────────────────────────────────
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Location Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Set store search radius (km):", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchRadius,
                        onValueChange = { searchRadius = it },
                        label = { Text("Radius in km") },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00897B)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Default is 10 km. Increase for rural areas.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Save", color = Color(0xFF00897B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // ── About Dialog ──────────────────────────────────────────────────────────
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Jan-Aushadhi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Pradhan Mantri Bhartiya Janaushadhi Pariyojana (PMBJP)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00897B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "A government initiative to provide quality medicines at affordable prices through dedicated outlets known as Janaushadhi Kendras.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 10,000+ stores across India\n• 50-90% cheaper than branded\n• Same quality as branded medicines\n• Approved by CDSCO",
                        fontSize = 13.sp,
                        color = Color(0xFF212121),
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = Color(0xFF00897B), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ── Logout Dialog ─────────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Logout", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF00897B))
                }
            }
        )
    }
}

@Composable
fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00897B)
        )
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun ProfileOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFE0F2F1), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00897B),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.LightGray
        )
    }
}
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = MutableInteractionSource(),
            onClick = onClick
        )
    )