package com.zayyan.dormfood.ui.screen.warung

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.zayyan.dormfood.ui.theme.DormRed
import com.zayyan.dormfood.ui.viewmodel.WarungViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

private fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaWarungScreen(
    navController: NavHostController,
    warungViewModel: WarungViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        warungViewModel.loadWarungData()
    }

    val context = LocalContext.current
    val warungProfile by warungViewModel.warungProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedPoint by remember { mutableStateOf(GeoPoint(-6.9175, 107.6191)) }
    var isSaving by remember { mutableStateOf(false) }

    var showEnableLocationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(warungProfile) {
        val lat = warungProfile?.latitude?.takeIf { it != 0.0 }
        val lon = warungProfile?.longitude?.takeIf { it != 0.0 }
        if (lat != null && lon != null) {
            selectedPoint = GeoPoint(lat, lon)
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (isLocationEnabled(context)) {
            getCurrentLocation(context) { lat, lon ->
                selectedPoint = GeoPoint(lat, lon)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (isLocationEnabled(context)) {
                getCurrentLocation(context) { lat, lon ->
                    selectedPoint = GeoPoint(lat, lon)
                }
            } else {
                showEnableLocationDialog = true
            }
        }
    }

    fun handleMyLocationClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        when {
            !hasPermission -> {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            !isLocationEnabled(context) -> {
                showEnableLocationDialog = true
            }
            else -> {
                getCurrentLocation(context) { lat, lon ->
                    selectedPoint = GeoPoint(lat, lon)
                }
            }
        }
    }

    if (showEnableLocationDialog) {
        AlertDialog(
            onDismissRequest = { showEnableLocationDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = DormRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Aktifkan Lokasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Layanan lokasi perangkat Anda sedang nonaktif. Aktifkan lokasi agar kami dapat mendeteksi posisi warung Anda secara akurat.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEnableLocationDialog = false
                        locationSettingsLauncher.launch(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DormRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buka Pengaturan")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEnableLocationDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Nanti Saja", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Titik Lokasi Warung", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(17.0)
                        controller.setCenter(selectedPoint)
                        overlays.add(object : Overlay() {
                            override fun onSingleTapConfirmed(
                                e: MotionEvent,
                                mapView: MapView
                            ): Boolean {
                                val tappedPoint = mapView.projection.fromPixels(
                                    e.x.toInt(), e.y.toInt()
                                ) as GeoPoint
                                selectedPoint = tappedPoint
                                updateMarker(mapView, tappedPoint)
                                return true
                            }
                        })
                        updateMarker(this, selectedPoint)
                    }
                },
                update = { mapView ->
                    updateMarker(mapView, selectedPoint)
                    mapView.controller.animateTo(selectedPoint)
                }
            )

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DormRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "%.6f, %.6f".format(
                                selectedPoint.latitude,
                                selectedPoint.longitude
                            ),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Text(
                        text = "Tap pada peta untuk memilih titik lokasi warung Anda.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { handleMyLocationClick() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DormRed),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(DormRed)
                            )
                        ) {
                            Text("Lokasi Saya", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                isSaving = true
                                warungViewModel.updateWarungLocation(
                                    latitude = selectedPoint.latitude,
                                    longitude = selectedPoint.longitude,
                                    onSuccess = {
                                        isSaving = false
                                        warungViewModel.setUpdateMessage("Titik lokasi berhasil disimpan")
                                        navController.popBackStack()
                                    },
                                    onError = { error ->
                                        isSaving = false
                                        warungViewModel.setUpdateMessage("Gagal menyimpan: $error")
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DormRed)
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Simpan Lokasi", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun updateMarker(mapView: MapView, point: GeoPoint) {
    mapView.overlays.removeAll { it is Marker }
    val marker = Marker(mapView).apply {
        position = point
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = "Lokasi Warung"
    }
    mapView.overlays.add(marker)
    mapView.invalidate()
}

private fun getCurrentLocation(context: Context, onResult: (Double, Double) -> Unit) {
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    val cancelToken = CancellationTokenSource()
    try {
        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancelToken.token
        ).addOnSuccessListener { location ->
            location?.let { onResult(it.latitude, it.longitude) }
        }
    } catch (_: SecurityException) {}
}