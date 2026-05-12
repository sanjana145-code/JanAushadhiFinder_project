package com.example.janaushadhifinder

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

val TealDark   = Color(0xFF00695C)
val TealLight  = Color(0xFFB2DFDB)
val TealBg     = Color(0xFF00897B)
val RedLight   = Color(0xFFFFEBEE)
val RedText    = Color(0xFFC62828)
val GreenLight = Color(0xFFE8F5E9)
val GreenText  = Color(0xFF2E7D32)
val SavingsBg  = Color(0xFF00897B)

@Composable
fun MedicineSearchScreen(
    viewModel: MedicineSearchViewModel = viewModel(),
    onViewNearbyStore: (Medicine) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var stockRequestMedicine by remember { mutableStateOf<Medicine?>(null) }
    var showStockSuccess by remember { mutableStateOf(false) }
    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) viewModel.onQueryChanged(spoken)
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
                .background(TealBg)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "Jan-Aushadhi Finder",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Find affordable generic medicines",
                color = TealLight,
                fontSize = 13.sp
            )
        }

        // Search box
        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChanged,
            placeholder = { Text("Search medicine name (e.g. Crocin)") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocalPharmacy,
                    contentDescription = null,
                    tint = TealBg
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak medicine name")
                        }
                        voiceLauncher.launch(intent)
                    },
                    modifier = Modifier.semantics { contentDescription = "Voice search" }
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = TealBg)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealBg,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        if (uiState.suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.suggestions.take(3).forEach { suggestion ->
                    AssistChip(
                        onClick = { viewModel.onQueryChanged(suggestion) },
                        label = { Text(suggestion, fontSize = 12.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Status row
        if (uiState.isLoading) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = TealBg,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Loading medicines from cloud...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${uiState.medicines.size} medicines found",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                // Source badge
                Box(
                    modifier = Modifier
                        .background(
                            if (uiState.dataSource.contains("Firebase")) Color(0xFFE8F5E9)
                            else Color(0xFFFFF3E0),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = uiState.dataSource,
                        fontSize = 10.sp,
                        color = if (uiState.dataSource.contains("Firebase")) Color(0xFF2E7D32)
                        else Color(0xFFE65100),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Medicine list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.medicines, key = { "${it.brandName}-${it.genericName}" }) { medicine ->
                MedicineCard(
                    medicine = medicine,
                    query = uiState.query,
                    onViewNearbyStore = { onViewNearbyStore(medicine) },
                    onRequestStock = { stockRequestMedicine = medicine }
                )
            }
        }
    }

    stockRequestMedicine?.let { medicine ->
        AlertDialog(
            onDismissRequest = { stockRequestMedicine = null },
            title = { Text("Request Stock", fontWeight = FontWeight.Bold) },
            text = {
                Text("Send a simulated stock request for ${medicine.brandName} to nearby Jan-Aushadhi stores?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        stockRequestMedicine = null
                        showStockSuccess = true
                    }
                ) {
                    Text("Send Request", color = TealBg, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { stockRequestMedicine = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showStockSuccess) {
        AlertDialog(
            onDismissRequest = { showStockSuccess = false },
            title = { Text("Request Sent", fontWeight = FontWeight.Bold) },
            text = { Text("Your request has been sent successfully.") },
            confirmButton = {
                TextButton(onClick = { showStockSuccess = false }) {
                    Text("OK", color = TealBg, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun MedicineCard(
    medicine: Medicine,
    query: String = "",
    onViewNearbyStore: () -> Unit = {},
    onRequestStock: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color(0xFFE0F2F1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = TealBg,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    HighlightedText(
                        text = medicine.brandName,
                        query = query,
                        fontSize = 18
                    )
                    HighlightedText(
                        text = medicine.genericName,
                        query = query,
                        fontSize = 16,
                        color = TealBg,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "${medicine.category} - ${medicine.manufacturer}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriceBox(
                    label = "Branded",
                    price = "Rs. ${medicine.brandedPrice}",
                    bgColor = RedLight,
                    labelColor = RedText,
                    priceColor = RedText,
                    modifier = Modifier.weight(1f)
                )
                PriceBox(
                    label = "Generic",
                    price = "Rs. ${medicine.genericPrice}",
                    bgColor = GreenLight,
                    labelColor = GreenText,
                    priceColor = GreenText,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(SavingsBg, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Save", color = Color.White, fontSize = 11.sp)
                        Text(
                            text = "Rs. ${medicine.savings}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "${medicine.savingsPercent}%", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onViewNearbyStore,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealBg)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("View Nearby Store", fontSize = 12.sp)
                }
                Button(
                    onClick = onRequestStock,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealBg)
                ) {
                    Text("Request Stock", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    fontSize: Int,
    color: Color = Color(0xFF212121),
    modifier: Modifier = Modifier
) {
    val start = if (query.isBlank()) -1 else text.lowercase().indexOf(query.lowercase())
    Text(
        text = if (start >= 0) {
            buildAnnotatedString {
                append(text.substring(0, start))
                withStyle(SpanStyle(background = Color(0xFFFFF59D), fontWeight = FontWeight.Bold)) {
                    append(text.substring(start, start + query.length))
                }
                append(text.substring(start + query.length))
            }
        } else {
            buildAnnotatedString { append(text) }
        },
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = modifier
    )
}

@Composable
fun PriceBox(
    label: String,
    price: String,
    bgColor: Color,
    labelColor: Color,
    priceColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = label, fontSize = 11.sp, color = labelColor)
            Text(
                text = price,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = priceColor
            )
        }
    }
}
