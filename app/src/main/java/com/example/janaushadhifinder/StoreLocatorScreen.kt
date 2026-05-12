package com.example.janaushadhifinder

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// Sample Jan-Aushadhi store data
data class JanAushadhiStore(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val isOpen: Boolean
)

val sampleStores = listOf(
    JanAushadhiStore("Jan-Aushadhi Kendra - Koramangala",
        "80 Feet Rd, Koramangala, Bengaluru", 12.9352, 77.6245, true),
    JanAushadhiStore("Jan-Aushadhi Kendra - Jayanagar",
        "11th Main, Jayanagar 4th Block, Bengaluru", 12.9299, 77.5933, true),
    JanAushadhiStore("Jan-Aushadhi Kendra - HSR Layout",
        "Sector 2, HSR Layout, Bengaluru", 12.9116, 77.6389, false),
    JanAushadhiStore("Jan-Aushadhi Kendra - BTM Layout",
        "BTM 2nd Stage, Bengaluru", 12.9166, 77.6101, true),
    JanAushadhiStore("Jan-Aushadhi Kendra - Indiranagar",
        "100 Feet Road, Indiranagar, Bengaluru", 12.9784, 77.6408, true),
)

@Composable
fun StoreLocatorScreen() {
    val context = LocalContext.current
    var locationGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> locationGranted = granted }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) locationGranted = true
        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                text = "Nearby Stores",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Jan-Aushadhi Kendras within 10 km",
                color = Color(0xFFB2DFDB),
                fontSize = 13.sp
            )
        }

        // Google Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            MapViewComposable()
        }

        // Store list
        Text(
            text = "${sampleStores.size} stores nearby",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleStores) { store ->
                StoreCard(store = store)
            }
        }
    }
}

@Composable
fun MapViewComposable() {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = { mapView.apply { onCreate(null); onResume() } },
        modifier = Modifier.fillMaxSize()
    ) { mv ->
        mv.getMapAsync { googleMap: GoogleMap ->
            // Default center - Bengaluru
            val bengaluru = LatLng(12.9352, 77.6245)
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(bengaluru, 12f)
            )
            // Add markers for each store
            sampleStores.forEach { store ->
                googleMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(store.lat, store.lng))
                        .title(store.name)
                        .snippet(if (store.isOpen) "Open Now" else "Closed")
                )
            }
            googleMap.uiSettings.isZoomControlsEnabled = true
        }
    }
}

@Composable
fun StoreCard(store: JanAushadhiStore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = store.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = store.address,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Open/Closed badge
            Box(
                modifier = Modifier
                    .background(
                        if (store.isOpen) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (store.isOpen) "Open" else "Closed",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (store.isOpen) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    }
}