package com.example.janaushadhifinder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

data class MedicineReminder(
    val id: Int,
    val medicineName: String,
    val daysSupply: Int
)

@Composable
fun ReminderScreen() {
    val context = LocalContext.current
    var medicineName by remember { mutableStateOf("") }
    var daysSupply by remember { mutableStateOf("") }
    var reminders by remember { mutableStateOf(listOf<MedicineReminder>()) }
    var nextId by remember { mutableStateOf(1) }
    var showSuccess by remember { mutableStateOf(false) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notifPermission = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00897B))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Refill Reminders",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Never run out of medicines",
                color = Color(0xFFB2DFDB),
                fontSize = 13.sp
            )
        }

        // Input Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Add New Reminder",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Medicine name input
                OutlinedTextField(
                    value = medicineName,
                    onValueChange = { medicineName = it },
                    label = { Text("Medicine Name") },
                    placeholder = { Text("e.g. Metformin") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00897B),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Days supply input
                OutlinedTextField(
                    value = daysSupply,
                    onValueChange = { daysSupply = it },
                    label = { Text("Days of Supply") },
                    placeholder = { Text("e.g. 30") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00897B),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Add button
                Button(
                    onClick = {
                        val days = daysSupply.toIntOrNull()
                        if (medicineName.isNotBlank() && days != null && days > 0) {
                            val reminder = MedicineReminder(
                                id = nextId,
                                medicineName = medicineName.trim(),
                                daysSupply = days
                            )
                            reminders = reminders + reminder
                            scheduleReminder(context, reminder)
                            nextId++
                            medicineName = ""
                            daysSupply = ""
                            showSuccess = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00897B)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Set Reminder",
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Success message
                if (showSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reminder set successfully!",
                        color = Color(0xFF2E7D32),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    LaunchedEffect(showSuccess) {
                        kotlinx.coroutines.delay(2000)
                        showSuccess = false
                    }
                }
            }
        }

        // Reminders list
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No reminders set yet.\nAdd your first medicine above.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Text(
                text = "Active Reminders (${reminders.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onDelete = {
                            reminders = reminders.filter { it.id != reminder.id }
                            cancelReminder(context, reminder.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderCard(reminder: MedicineReminder, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Medicine icon placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE1F5EE), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "💊", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.medicineName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "${reminder.daysSupply} days supply",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Reminder in ${reminder.daysSupply - 3} days",
                    fontSize = 12.sp,
                    color = Color(0xFF00897B),
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFC62828)
                )
            }
        }
    }
}

fun scheduleReminder(context: Context, reminder: MedicineReminder) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("medicine_name", reminder.medicineName)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminder.id,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val daysBeforeReminder = (reminder.daysSupply - 3).coerceAtLeast(1)
    val triggerTime = System.currentTimeMillis() +
            (daysBeforeReminder * 24 * 60 * 60 * 1000L)

    try {
        when {
            // Android 12+
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm if permission not granted
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
            // Android 6 - 11
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            // Below Android 6
            else -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    } catch (e: SecurityException) {
        // If exact alarm permission denied, use inexact
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

fun cancelReminder(context: Context, reminderId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, ReminderReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}