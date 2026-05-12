package com.example.janaushadhifinder

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val TealDark   = Color(0xFF00695C)
val TealLight  = Color(0xFFB2DFDB)
val TealBg     = Color(0xFF00897B)
val RedLight   = Color(0xFFFFEBEE)
val RedText    = Color(0xFFC62828)
val GreenLight = Color(0xFFE8F5E9)
val GreenText  = Color(0xFF2E7D32)
val SavingsBg  = Color(0xFF00897B)

@Composable
fun MedicineSearchScreen() {
    var searchQuery  by remember { mutableStateOf("") }
    var allMedicines by remember { mutableStateOf(MedicineData.allMedicines) }
    var isLoading    by remember { mutableStateOf(true) }
    var dataSource   by remember { mutableStateOf("local") }

    LaunchedEffect(Unit) {
        Log.d("Firebase", "LaunchedEffect started - calling Firebase")
        try {
            val repo = FirebaseRepository()
            val firebaseMedicines = repo.getMedicines()
            Log.d("Firebase", "Received ${firebaseMedicines.size} medicines from Firebase")
            if (firebaseMedicines.isNotEmpty()) {
                allMedicines = firebaseMedicines
                dataSource = "cloud"
            } else {
                Log.d("Firebase", "Empty list - staying with local data")
                dataSource = "local"
            }
        } catch (e: Exception) {
            Log.e("Firebase", "Exception: ${e.message}")
            dataSource = "local"
        }
        isLoading = false
    }

    val filteredMedicines by remember(searchQuery, allMedicines) {
        derivedStateOf {
            if (searchQuery.isBlank()) allMedicines
            else allMedicines.filter { medicine ->
                FuzzySearch.matches(medicine.brandName, searchQuery) ||
                        FuzzySearch.matches(medicine.genericName, searchQuery)
            }
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
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search medicine name (e.g. Crocin)") },
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

        // Status row
        if (isLoading) {
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
                    text = "${filteredMedicines.size} medicines found",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                // Source badge
                Box(
                    modifier = Modifier
                        .background(
                            if (dataSource == "cloud") Color(0xFFE8F5E9)
                            else Color(0xFFFFF3E0),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (dataSource == "cloud") "Firebase" else "Local",
                        fontSize = 10.sp,
                        color = if (dataSource == "cloud") Color(0xFF2E7D32)
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
            items(filteredMedicines) { medicine ->
                MedicineCard(medicine = medicine)
            }
        }
    }
}

@Composable
fun MedicineCard(medicine: Medicine) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = medicine.brandName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = medicine.genericName,
                fontSize = 13.sp,
                color = TealBg,
                modifier = Modifier.padding(top = 2.dp)
            )
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
                            text = "${medicine.savingsPercent}%",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
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