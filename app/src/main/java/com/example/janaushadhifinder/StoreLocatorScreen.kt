package com.example.janaushadhifinder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.time.LocalTime

data class JanAushadhiStore(
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val isOpen: Boolean,
    val timings: String = "9:00 AM - 9:00 PM",
    val phone: String = "1800-180-8080"
) {
    fun isOpenNow(): Boolean {
        val now = LocalTime.now()
        return isOpen && !now.isBefore(LocalTime.of(9, 0)) && now.isBefore(LocalTime.of(21, 0))
    }
}

data class NearbyStore(
    val store: JanAushadhiStore,
    val distanceKm: Double?
)

val sampleStores = listOf(
    JanAushadhiStore("Jan-Aushadhi Kendra - Koramangala", "80 Feet Rd, Koramangala, Bengaluru", 12.9352, 77.6245, true),
    JanAushadhiStore("Jan-Aushadhi Kendra - Jayanagar", "11th Main, Jayanagar 4th Block, Bengaluru", 12.9299, 77.5933, true),
    JanAushadhiStore("Jan-Aushadhi Kendra - HSR Layout", "Sector 2, HSR Layout, Bengaluru", 12.9116, 77.6389, true),
    JanAushadhiStore("Jan-Aushadhi Kendra - BTM Layout", "BTM 2nd Stage, Bengaluru", 12.9166, 77.6101, true),
    JanAushadhiStore("Jan-Aushadhi Kendra - Indiranagar", "100 Feet Road, Indiranagar, Bengaluru", 12.9784, 77.6408, true)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreLocatorScreen(
    focusRequest: Int = 0,
    requestedMedicineName: String? = null
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var isLocationEnabled by remember { mutableStateOf(context.isDeviceLocationEnabled()) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedStore by remember { mutableStateOf<NearbyStore?>(null) }
    var statusMessage by remember { mutableStateOf("Detecting current location...") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        statusMessage = if (hasLocationPermission) {
            "Fetching live location..."
        } else {
            "Location permission denied. Enable permission to see nearby stores."
        }
    }

    LaunchedEffect(focusRequest) {
        isLocationEnabled = context.isDeviceLocationEnabled()
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else if (!isLocationEnabled) {
            statusMessage = "GPS is disabled. Turn on location services for live nearby stores."
        } else {
            statusMessage = "Fetching live location..."
        }
    }

    LiveLocationEffect(
        enabled = hasLocationPermission && isLocationEnabled,
        fusedLocationClient = fusedLocationClient,
        onLocation = { location ->
            currentLocation = LatLng(location.latitude, location.longitude)
            statusMessage = "Current location detected"
        },
        onLocationUnavailable = {
            statusMessage = "Waiting for GPS fix. Showing available store list."
        }
    )

    val nearbyStores = remember(currentLocation) {
        buildNearbyStores(currentLocation)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
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
                text = requestedMedicineName?.let { "Stores near you for $it" }
                    ?: "Jan-Aushadhi Kendras within 10 km",
                color = Color(0xFFB2DFDB),
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            MapViewComposable(
                nearbyStores = nearbyStores,
                currentLocation = currentLocation,
                hasLocationPermission = hasLocationPermission,
                focusRequest = focusRequest,
                onStoreSelected = { selectedStore = it }
            )
            CurrentLocationBadge(
                text = statusMessage,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }

        Text(
            text = "${nearbyStores.count { it.distanceKm == null || it.distanceKm <= 10.0 }} stores nearby",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nearbyStores, key = { it.store.name }) { nearbyStore ->
                StoreCard(
                    nearbyStore = nearbyStore,
                    onSelect = { selectedStore = nearbyStore },
                    onNavigate = { context.openNavigation(nearbyStore.store) }
                )
            }
        }
    }

    selectedStore?.let { nearbyStore ->
        ModalBottomSheet(onDismissRequest = { selectedStore = null }) {
            StoreDetailSheet(
                nearbyStore = nearbyStore,
                onNavigate = { context.openNavigation(nearbyStore.store) },
                onCall = { context.openDialer(nearbyStore.store.phone) }
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun LiveLocationEffect(
    enabled: Boolean,
    fusedLocationClient: FusedLocationProviderClient,
    onLocation: (Location) -> Unit,
    onLocationUnavailable: () -> Unit
) {
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) onLocation(location) else onLocationUnavailable()
            }
            .addOnFailureListener { onLocationUnavailable() }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L)
            .setMinUpdateIntervalMillis(2_000L)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let(onLocation)
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        onDispose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}

@Composable
private fun MapViewComposable(
    nearbyStores: List<NearbyStore>,
    currentLocation: LatLng?,
    hasLocationPermission: Boolean,
    focusRequest: Int,
    onStoreSelected: (NearbyStore) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = {
            mapView.apply {
                onCreate(null)
                onResume()
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { view ->
            view.getMapAsync { googleMap ->
                renderNearbyStoresMap(
                    googleMap = googleMap,
                    context = context,
                    nearbyStores = nearbyStores,
                    currentLocation = currentLocation,
                    hasLocationPermission = hasLocationPermission,
                    focusRequest = focusRequest,
                    onStoreSelected = onStoreSelected
                )
            }
        }
    )
}

@SuppressLint("MissingPermission")
private fun renderNearbyStoresMap(
    googleMap: GoogleMap,
    context: Context,
    nearbyStores: List<NearbyStore>,
    currentLocation: LatLng?,
    hasLocationPermission: Boolean,
    focusRequest: Int,
    onStoreSelected: (NearbyStore) -> Unit
) {
    googleMap.clear()
    googleMap.uiSettings.isZoomControlsEnabled = true
    googleMap.uiSettings.isMyLocationButtonEnabled = true
    googleMap.isMyLocationEnabled = hasLocationPermission && context.hasLocationPermission()

    nearbyStores.forEach { nearbyStore ->
        val store = nearbyStore.store
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(store.lat, store.lng))
                .title(store.name)
                .snippet("${nearbyStore.distanceLabel()} - ${store.openStatusLabel()} - ${store.timings}")
                .icon(
                    BitmapDescriptorFactory.defaultMarker(
                        if (store.isOpenNow()) BitmapDescriptorFactory.HUE_GREEN
                        else BitmapDescriptorFactory.HUE_RED
                    )
                )
        )
        marker?.tag = nearbyStore
    }

    googleMap.setOnMarkerClickListener { marker ->
        (marker.tag as? NearbyStore)?.let(onStoreSelected)
        false
    }

    val fallback = nearbyStores.firstOrNull()?.store?.let { LatLng(it.lat, it.lng) }
        ?: LatLng(12.9352, 77.6245)
    val target = currentLocation ?: fallback
    val zoom = if (currentLocation != null) 13.5f else 12f
    if (focusRequest > 0 || currentLocation != null) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
    } else {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, zoom))
    }
}

@Composable
private fun CurrentLocationBadge(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFF1976D2)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = text, fontSize = 12.sp, color = Color(0xFF37474F))
        }
    }
}

@Composable
fun StoreCard(
    nearbyStore: NearbyStore,
    onSelect: () -> Unit,
    onNavigate: () -> Unit
) {
    val store = nearbyStore.store
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onSelect
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
                Text(
                    text = "${nearbyStore.distanceLabel()} - ${store.timings}",
                    fontSize = 12.sp,
                    color = Color(0xFF00897B),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            OpenStatusChip(store = store)
            TextButton(onClick = onNavigate) {
                Text("Navigate", color = Color(0xFF00897B), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun OpenStatusChip(store: JanAushadhiStore) {
    val isOpenNow = store.isOpenNow()
    Box(
        modifier = Modifier
            .background(
                if (isOpenNow) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = store.openStatusLabel(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOpenNow) Color(0xFF2E7D32) else Color(0xFFC62828)
        )
    }
}

@Composable
fun StoreDetailSheet(
    nearbyStore: NearbyStore,
    onNavigate: () -> Unit,
    onCall: () -> Unit
) {
    val store = nearbyStore.store
    Column(modifier = Modifier.padding(20.dp)) {
        Text(store.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(store.address, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = {},
                label = { Text(store.openStatusLabel()) },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = if (store.isOpenNow()) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            )
            AssistChip(onClick = {}, label = { Text(nearbyStore.distanceLabel()) })
        }
        Text(
            text = "Timings: ${store.timings} (${store.closingStatusLabel()})",
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onNavigate,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Navigate")
            }
            OutlinedButton(
                onClick = onCall,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Store")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun buildNearbyStores(currentLocation: LatLng?): List<NearbyStore> {
    return sampleStores
        .map { store ->
            NearbyStore(
                store = store,
                distanceKm = currentLocation?.let {
                    distanceKm(it.latitude, it.longitude, store.lat, store.lng)
                }
            )
        }
        .filter { it.distanceKm == null || it.distanceKm <= 10.0 }
        .sortedWith(compareBy(nullsLast()) { it.distanceKm })
        .ifEmpty {
            sampleStores.map { NearbyStore(it, null) }
        }
}

fun distanceKm(startLat: Double, startLng: Double, endLat: Double, endLng: Double): Double {
    val result = FloatArray(1)
    Location.distanceBetween(startLat, startLng, endLat, endLng, result)
    return result[0] / 1000.0
}

private fun NearbyStore.distanceLabel(): String {
    return distanceKm?.let { String.format("%.1f km", it) } ?: "Distance unavailable"
}

private fun JanAushadhiStore.openStatusLabel(): String {
    return if (isOpenNow()) "Open Now" else "Closed"
}

private fun JanAushadhiStore.closingStatusLabel(): String {
    return if (isOpenNow()) "Closes at 9:00 PM" else "Opens at 9:00 AM"
}

private fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

private fun Context.isDeviceLocationEnabled(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

private fun Context.openNavigation(store: JanAushadhiStore) {
    val uri = Uri.parse("google.navigation:q=${store.lat},${store.lng}")
    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (mapsIntent.resolveActivity(packageManager) != null) {
        startActivity(mapsIntent)
    } else {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${store.lat},${store.lng}?q=${store.lat},${store.lng}(${Uri.encode(store.name)})")))
    }
}

private fun Context.openDialer(phone: String) {
    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
}
