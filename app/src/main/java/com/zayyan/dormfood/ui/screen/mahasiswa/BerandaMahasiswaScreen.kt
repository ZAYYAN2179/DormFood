package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.zayyan.dormfood.ui.component.BottomBarMahasiswa
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.model.WarungItem
import com.zayyan.dormfood.ui.model.WarungViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.screen.warung.ImagePreviewDialog
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import androidx.core.net.toUri

enum class FilterStatus { SEMUA, BUKA, TUTUP }

@Composable
fun BerandaMahasiswa(
    navController: NavHostController,
    warungViewModel: WarungViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        warungViewModel.loadAllWarungs()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = "Dorm Food")
        },
        bottomBar = {
            BottomBarMahasiswa(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = Background
    ) { paddingValues ->
        BerandaMahasiswaContent(
            paddingValues = paddingValues,
            navController = navController,
            warungViewModel = warungViewModel
        )
    }
}

@Composable
fun BerandaMahasiswaContent(
    paddingValues: PaddingValues,
    navController: NavHostController,
    warungViewModel: WarungViewModel
) {
    val warungs by warungViewModel.warungList.collectAsState()
    val isLoading by warungViewModel.isLoadingList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(FilterStatus.SEMUA) }

    val filteredWarungs = remember(warungs, searchQuery, activeFilter) {
        warungs
            .filter { warung ->
                warung.namaWarung.contains(searchQuery.trim(), ignoreCase = true)
            }
            .filter { warung ->
                when (activeFilter) {
                    FilterStatus.SEMUA -> true
                    FilterStatus.BUKA -> warung.layananAntar
                    FilterStatus.TUTUP -> !warung.layananAntar
                }
            }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = DormRed)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Memuat warung...", color = Color.Gray, fontSize = 14.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(text = "Cari Warung", color = Color.Gray, fontSize = 14.sp)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Cari",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Hapus",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DormRed,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // Filter Chips: Semua / Buka / Tutup
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    FilterChipItem(
                        label = "Semua",
                        isSelected = activeFilter == FilterStatus.SEMUA,
                        onClick = { activeFilter = FilterStatus.SEMUA }
                    )
                    FilterChipItem(
                        label = "Buka",
                        isSelected = activeFilter == FilterStatus.BUKA,
                        selectedColor = Color(0xFF16A34A),
                        onClick = { activeFilter = FilterStatus.BUKA }
                    )
                    FilterChipItem(
                        label = "Tutup",
                        isSelected = activeFilter == FilterStatus.TUTUP,
                        selectedColor = Color(0xFFE53935),
                        onClick = { activeFilter = FilterStatus.TUTUP }
                    )
                }
            }

            // Header jumlah hasil
            item {
                Text(
                    text = if (searchQuery.isEmpty() && activeFilter == FilterStatus.SEMUA)
                        "Daftar Warung"
                    else
                        "${filteredWarungs.size} warung ditemukan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            // Hasil daftar warung
            if (filteredWarungs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty())
                                "Warung \"$searchQuery\" tidak ditemukan"
                            else
                                "Tidak ada warung dengan status ini",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(filteredWarungs) { warung ->
                    WarungCard(
                        warung = warung,
                        onClick = {
                            navController.navigate(
                                Screen.MenuMahasiswaScreen.createRoute(warung.userId)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    selectedColor: Color = DormRed,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) selectedColor
                else Color(0xFFE8E8E8)
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun WarungCard(
    warung: WarungItem,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showImagePreview by remember { mutableStateOf(false) }

    val isOpen = warung.layananAntar

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isOpen) { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(width = 100.dp, height = 100.dp)) {
                if (warung.fotoMenu.isNotEmpty()) {
                    AsyncImage(
                        model = warung.fotoMenu,
                        contentDescription = warung.namaWarung,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showImagePreview = true },
                        contentScale = ContentScale.Crop,
                        alpha = if (isOpen) 1f else 0.4f
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = warung.namaWarung,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOpen) Color.Black else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (warung.alamatWarung.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = DormRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = warung.alamatWarung,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge BUKA / TUTUP
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isOpen) Color(0xFF16A34A)
                                else Color(0xFFE53935).copy(alpha = 0.12f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isOpen) "BUKA" else "TUTUP",
                            color = if (isOpen) Color.White else Color(0xFFE53935),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Tombol WhatsApp
                    if (warung.phone.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF25D366))
                                .clickable {
                                    val formatted = warung.phone.trim().let {
                                        if (it.startsWith("0")) "+62" + it.drop(1) else it
                                    }
                                    val uri = "https://wa.me/$formatted".toUri()
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW, uri
                                    )
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = warung.phone,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showImagePreview && warung.fotoMenu.isNotEmpty()) {
        ImagePreviewDialog(
            imageUrl = warung.fotoMenu,
            title = warung.namaWarung,
            onDismiss = { showImagePreview = false }
        )
    }
}