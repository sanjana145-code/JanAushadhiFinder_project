package com.example.janaushadhifinder

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PrescriptionItem(
    val id: Int,
    val medicineName: String,
    val brandedPrice: Int,
    val genericPrice: Int
) {
    val savings: Int get() = brandedPrice - genericPrice
    val savingsPercent: Int get() = ((savings.toFloat() / brandedPrice) * 100).toInt()
}

@Composable
fun SavingsCalculatorScreen() {

    var medicineName   by remember { mutableStateOf("") }
    var brandedPrice   by remember { mutableStateOf("") }
    var genericPrice   by remember { mutableStateOf("") }
    var items          by remember { mutableStateOf(listOf<PrescriptionItem>()) }
    var nextId         by remember { mutableStateOf(1) }
    var errorMessage   by remember { mutableStateOf("") }

    val totalBranded = items.sumOf { it.brandedPrice }
    val totalGeneric  = items.sumOf { it.genericPrice }
    val totalSavings  = totalBranded - totalGeneric
    val totalPercent  = if (totalBranded > 0)
        ((totalSavings.toFloat() / totalBranded) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        // ── Header ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00897B))
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Savings Calculator",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See how much you save on generics",
                color = Color(0xFFB2DFDB),
                fontSize = 13.sp
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Input Card ──────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = "Add Medicine to Prescription",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Medicine name
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

                        // Price row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = brandedPrice,
                                onValueChange = { brandedPrice = it },
                                label = { Text("Branded Price") },
                                placeholder = { Text("Rs.") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFC62828),
                                    unfocusedBorderColor = Color.LightGray
                                ),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = genericPrice,
                                onValueChange = { genericPrice = it },
                                label = { Text("Generic Price") },
                                placeholder = { Text("Rs.") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2E7D32),
                                    unfocusedBorderColor = Color.LightGray
                                ),
                                singleLine = true
                            )
                        }

                        // Error message
                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = errorMessage,
                                color = Color(0xFFC62828),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val bp = brandedPrice.toIntOrNull()
                                val gp = genericPrice.toIntOrNull()
                                when {
                                    medicineName.isBlank() ->
                                        errorMessage = "Please enter medicine name"
                                    bp == null || bp <= 0 ->
                                        errorMessage = "Enter valid branded price"
                                    gp == null || gp <= 0 ->
                                        errorMessage = "Enter valid generic price"
                                    gp >= bp ->
                                        errorMessage = "Generic price must be less than branded"
                                    else -> {
                                        items = items + PrescriptionItem(
                                            id          = nextId,
                                            medicineName = medicineName.trim(),
                                            brandedPrice = bp,
                                            genericPrice = gp
                                        )
                                        nextId++
                                        medicineName  = ""
                                        brandedPrice  = ""
                                        genericPrice  = ""
                                        errorMessage  = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00897B)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Add to Prescription",
                                fontSize = 15.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // ── Savings Summary Card ────────────────────────────────────────
            if (items.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF00897B)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Monthly Savings",
                                color = Color(0xFFB2DFDB),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Rs. $totalSavings",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "You save $totalPercent% on your prescription",
                                color = Color(0xFFE0F2F1),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFF4DB6AC))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Branded vs Generic total row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SummaryColumn(
                                    label = "Branded Total",
                                    value = "Rs. $totalBranded",
                                    valueColor = Color(0xFFFFCDD2)
                                )
                                SummaryColumn(
                                    label = "Generic Total",
                                    value = "Rs. $totalGeneric",
                                    valueColor = Color(0xFFC8E6C9)
                                )
                                SummaryColumn(
                                    label = "Annual Saving",
                                    value = "Rs. ${totalSavings * 12}",
                                    valueColor = Color.White
                                )
                            }
                        }
                    }
                }

                // ── Prescription Items ──────────────────────────────────────
                item {
                    Text(
                        text = "Prescription (${items.size} medicines)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }

                items(items) { item ->
                    PrescriptionItemCard(
                        item = item,
                        onDelete = { items = items.filter { it.id != item.id } }
                    )
                }
            } else {
                // Empty state
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add medicines from your prescription\nto calculate total savings.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryColumn(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xFFB2DFDB), fontSize = 11.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PrescriptionItemCard(item: PrescriptionItem, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.medicineName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Branded
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFEBEE), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Rs. ${item.brandedPrice}",
                            fontSize = 12.sp,
                            color = Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Generic
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Rs. ${item.genericPrice}",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Savings
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE0F2F1), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Save ${item.savingsPercent}%",
                            fontSize = 12.sp,
                            color = Color(0xFF00695C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

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