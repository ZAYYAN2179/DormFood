package com.zayyan.dormfood.ui.screen.warung

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaWarungScreen(
    navController: NavHostController,
    warungViewModel: WarungViewModel = viewModel()
) {
    val context = LocalContext.current
    val warungProfile by warungViewModel.warungProfile.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Koordinat yang dipilih user — pakai lokasi tersimpan jika ada
    val initialLat = warungProfile?.latitude?.takeIf { it != 0.0 } ?: -6.9175
    val initialLon = warungProfile?.longitude?.takeIf { it != 0.0 } ?: 107.6191
    var selectedPoint by remember { mutableStateOf(GeoPoint(initialLat, initialLon)) }
    var isSaving by remember { mutableStateOf(false) }

    // Inisialisasi OSMDroid user-agent
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            getCurrentLocation(context) { lat, lon ->
                selectedPoint = GeoPoint(lat, lon)
            }
        }
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
            // ── Peta OSM ──────────────────────────────────────────────
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(17.0)
                        controller.setCenter(selectedPoint)

                        // Overlay: tap untuk memindahkan marker
                        overlays.add(object : Overlay() {
                            override fun onSingleTapConfirmed(
                                e: MotionEvent,
                                mapView: MapView
                            ): Boolean {
                                val projection = mapView.projection
                                val tappedPoint = projection.fromPixels(
                                    e.x.toInt(), e.y.toInt()
                                ) as GeoPoint
                                selectedPoint = tappedPoint
                                updateMarker(mapView, tappedPoint)
                                return true
                            }
                        })

                        // Marker awal
                        updateMarker(this, selectedPoint)
                    }
                },
                update = { mapView ->
                    // Saat selectedPoint berubah (misal dari GPS), update peta
                    updateMarker(mapView, selectedPoint)
                    mapView.controller.animateTo(selectedPoint)
                }
            )

            // ── Panel bawah ───────────────────────────────────────────
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
                    // Koordinat terpilih
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
                        // Tombol lokasi saat ini
                        OutlinedButton(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    getCurrentLocation(context) { lat, lon ->
                                        selectedPoint = GeoPoint(lat, lon)
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DormRed),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(DormRed)
                            )
                        ) {
                            Text("Lokasi Saya", fontSize = 13.sp)
                        }

                        // Tombol simpan
                        Button(
                            onClick = {
                                isSaving = true
                                warungViewModel.updateWarungLocation(
                                    latitude = selectedPoint.latitude,
                                    longitude = selectedPoint.longitude,
                                    onSuccess = {
                                        isSaving = false
                                        warungViewModel.setUpdateMessage(
                                            "Titik lokasi berhasil disimpan"
                                        )
                                        navController.popBackStack()
                                    },
                                    onError = { error ->
                                        isSaving = false
                                        warungViewModel.setUpdateMessage(
                                            "Gagal menyimpan: $error"
                                        )
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

// ── Helper: update/tambah marker di peta ──────────────────────────────────────
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

// ── Helper: ambil GPS saat ini ────────────────────────────────────────────────
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
    } catch (_: SecurityException) {
        // Permission belum diberikan — tangani di caller
    }
}