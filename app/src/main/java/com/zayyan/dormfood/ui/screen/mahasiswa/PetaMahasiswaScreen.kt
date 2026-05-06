package com.zayyan.dormfood.ui.screen.mahasiswa

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
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.zayyan.dormfood.ui.theme.DormRed
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaMahasiswaScreen(
    navController: NavHostController,
    userId: String
) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    var namaWarung by remember { mutableStateOf("Lokasi Warung") }
    var alamatWarung by remember { mutableStateOf("") }
    var warungLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasLocation by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        Configuration.getInstance().userAgentValue = context.packageName

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                namaWarung = userDoc.getString("name") ?: "Lokasi Warung"
            }

        firestore.collection("warungs").document(userId).get()
            .addOnSuccessListener { warungDoc ->
                val lat = warungDoc.getDouble("latitude") ?: 0.0
                val lon = warungDoc.getDouble("longitude") ?: 0.0
                alamatWarung = warungDoc.getString("alamatWarung") ?: ""

                if (lat != 0.0 && lon != 0.0) {
                    warungLocation = GeoPoint(lat, lon)
                    hasLocation = true
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = namaWarung,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DormRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DormRed
                    )
                }

                !hasLocation -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Warung belum mengatur lokasi",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }

                else -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(17.0)
                                controller.setCenter(warungLocation)

                                warungLocation?.let { point ->
                                    val marker = Marker(this).apply {
                                        position = point
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        title = namaWarung
                                        snippet = alamatWarung.ifEmpty { null }
                                    }
                                    overlays.add(marker)
                                    marker.showInfoWindow()
                                }
                            }
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
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = DormRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = namaWarung,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    if (alamatWarung.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = alamatWarung,
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "%.6f, %.6f".format(
                                            warungLocation?.latitude,
                                            warungLocation?.longitude
                                        ),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val lat = warungLocation?.latitude ?: return@Button
                                    val lon = warungLocation?.longitude ?: return@Button
                                    val uri = "geo:$lat,$lon?q=$lat,$lon(${
                                        namaWarung.replace(
                                            " ",
                                            "+"
                                        )
                                    })".toUri()
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        uri
                                    ).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        val browserUri =
                                            "https://www.google.com/maps/search/?api=1&query=$lat,$lon".toUri()
                                        context.startActivity(
                                            android.content.Intent(
                                                android.content.Intent.ACTION_VIEW,
                                                browserUri
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DormRed)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Buka di Google Maps",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}