package com.example.janaushadhifinder

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    stockRequestViewModel: StockRequestViewModel = viewModel(),
    onViewNearbyStore: (Medicine) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stockRequestUiState by stockRequestViewModel.uiState.collectAsStateWithLifecycle()
    var stockRequestMedicine by remember { mutableStateOf<Medicine?>(null) }
    var voiceError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
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

    LaunchedEffect(stockRequestMedicine) {
        stockRequestMedicine?.let { stockRequestViewModel.prepare(it) }
    }

    LaunchedEffect(stockRequestUiState.successMessage) {
        if (stockRequestUiState.successMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(stockRequestUiState.successMessage)
            stockRequestMedicine = null
            stockRequestViewModel.clearResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFFF5F5F5)
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
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
            placeholder = { Text("Search branded or generic medicine") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TealBg
                )
            },
            trailingIcon = {
                Row {
                    if (uiState.query.isNotBlank()) {
                        IconButton(
                            onClick = viewModel::clearQuery,
                            modifier = Modifier.semantics { contentDescription = "Clear search" }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                        }
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak medicine name")
                            }
                            try {
                                voiceLauncher.launch(intent)
                            } catch (_: ActivityNotFoundException) {
                                voiceError = true
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = "Voice search" }
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = TealBg)
                    }
                }
            },
            supportingText = {
                if (voiceError) {
                    Text("Voice search is not available on this device.", color = RedText)
                    LaunchedEffect(voiceError) {
                        kotlinx.coroutines.delay(2500)
                        voiceError = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealBg,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        if (uiState.suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    uiState.suggestions.take(5).forEachIndexed { index, suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onQueryChanged(suggestion) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TealBg,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            HighlightedText(
                                text = suggestion,
                                query = uiState.query,
                                fontSize = 14,
                                color = Color(0xFF212121),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (index != uiState.suggestions.take(5).lastIndex) {
                            HorizontalDivider(color = Color(0xFFF1F3F4))
                        }
                    }
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
            if (!uiState.isLoading && uiState.medicines.isEmpty() && uiState.query.isNotBlank()) {
                item {
                    EmptySearchState(
                        query = uiState.query,
                        closestMatch = uiState.closestMatch,
                        onSuggestionClick = { suggestion -> viewModel.onQueryChanged(suggestion) }
                    )
                }
            } else {
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
        }
    }

    stockRequestMedicine?.let { medicine ->
        StockRequestBottomSheet(
            medicine = medicine,
            uiState = stockRequestUiState,
            stores = sampleStores,
            onDismiss = {
                stockRequestMedicine = null
                stockRequestViewModel.clearResult()
            },
            onQuantityChanged = stockRequestViewModel::onQuantityChanged,
            onPhoneChanged = stockRequestViewModel::onPhoneChanged,
            onNotesChanged = stockRequestViewModel::onNotesChanged,
            onStoreSelected = stockRequestViewModel::onStoreSelected,
            onSendRequest = { stockRequestViewModel.sendRequest(medicine) }
        )
    }
}

@Composable
fun EmptySearchState(
    query: String,
    closestMatch: String?,
    onSuggestionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = Color(0xFF90A4AE),
                modifier = Modifier.size(46.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No exact match found for \"$query\".",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263238)
            )
            Text(
                text = "Try another medicine name",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (!closestMatch.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AssistChip(
                    onClick = { onSuggestionClick(closestMatch) },
                    label = { Text("Did you mean: $closestMatch?") },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = TealBg
                    )
                )
            }
        }
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
