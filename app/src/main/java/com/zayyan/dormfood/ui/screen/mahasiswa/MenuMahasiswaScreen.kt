package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.zayyan.dormfood.ui.model.MenuViewModel
import com.zayyan.dormfood.ui.model.MenuWarungDetail
import com.zayyan.dormfood.ui.model.MenuItem
import com.zayyan.dormfood.ui.screen.warung.ImagePreviewDialog
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuMahasiswa(
    navController: NavHostController,
    userId: String,
    menuViewModel: MenuViewModel = viewModel()
) {
    val detail by menuViewModel.warungDetail.collectAsState()
    val isLoading by menuViewModel.isLoadingDetail.collectAsState()

    LaunchedEffect(userId) {
        menuViewModel.loadWarungDetail(userId)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detail?.namaWarung ?: "Menu Warung",
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
        }
    ) { paddingValues ->
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
                    Text(text = "Memuat...", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            detail?.let { data ->
                MenuMahasiswaContent(
                    paddingValues = paddingValues,
                    detail = data,
                    menuViewModel = menuViewModel
                )
            }
        }
    }
}

@Composable
fun MenuMahasiswaContent(
    paddingValues: PaddingValues,
    detail: MenuWarungDetail,
    menuViewModel: MenuViewModel
) {
    val menuList by menuViewModel.menuListByWarung.collectAsState()
    var showMenuPreview by remember { mutableStateOf(false) }
    var showQrPreview by remember { mutableStateOf(false) }

    val quantities = remember { mutableStateMapOf<String, Int>() }

    val makanan = menuList.filter { it.tipe.contains("makanan", ignoreCase = true) }
    val minuman = menuList.filter { it.tipe.contains("minuman", ignoreCase = true) }
    val lainnya = menuList.filter {
        !it.tipe.contains("makanan", ignoreCase = true) &&
                !it.tipe.contains("minuman", ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = detail.namaWarung,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (detail.alamatWarung.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = DormRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = detail.alamatWarung, fontSize = 14.sp, color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    if (detail.phone.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = DormRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = detail.phone, fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MediaCard(
                    title = "Daftar Menu",
                    imageUrl = detail.fotoMenu,
                    modifier = Modifier.weight(1f),
                    onClick = { if (detail.fotoMenu.isNotEmpty()) showMenuPreview = true }
                )
                MediaCard(
                    title = "QR Pembayaran",
                    imageUrl = detail.fotoQr,
                    modifier = Modifier.weight(1f),
                    onClick = { if (detail.fotoQr.isNotEmpty()) showQrPreview = true }
                )
            }
        }

        if (menuList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada menu tersedia",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (makanan.isNotEmpty()) {
            item {
                MenuSectionHeader(title = "🍽️ Makanan")
            }
            items(makanan) { menu ->
                val key = menu.namaMenu
                MenuItemCard(
                    menu = menu,
                    quantity = quantities[key] ?: 0,
                    onAdd = { quantities[key] = (quantities[key] ?: 0) + 1 },
                    onRemove = {
                        val current = quantities[key] ?: 0
                        if (current > 0) quantities[key] = current - 1
                    }
                )
            }
        }

        if (minuman.isNotEmpty()) {
            item {
                MenuSectionHeader(title = "🥤 Minuman")
            }
            items(minuman) { menu ->
                val key = menu.namaMenu
                MenuItemCard(
                    menu = menu,
                    quantity = quantities[key] ?: 0,
                    onAdd = { quantities[key] = (quantities[key] ?: 0) + 1 },
                    onRemove = {
                        val current = quantities[key] ?: 0
                        if (current > 0) quantities[key] = current - 1
                    }
                )
            }
        }

        if (lainnya.isNotEmpty()) {
            item {
                MenuSectionHeader(title = "🛒 Lainnya")
            }
            items(lainnya) { menu ->
                val key = menu.namaMenu
                MenuItemCard(
                    menu = menu,
                    quantity = quantities[key] ?: 0,
                    onAdd = { quantities[key] = (quantities[key] ?: 0) + 1 },
                    onRemove = {
                        val current = quantities[key] ?: 0
                        if (current > 0) quantities[key] = current - 1
                    }
                )
            }
        }
    }

    if (showMenuPreview && detail.fotoMenu.isNotEmpty()) {
        ImagePreviewDialog(
            imageUrl = detail.fotoMenu,
            title = "Daftar Menu",
            onDismiss = { showMenuPreview = false }
        )
    }
    if (showQrPreview && detail.fotoQr.isNotEmpty()) {
        ImagePreviewDialog(
            imageUrl = detail.fotoQr,
            title = "QR Pembayaran",
            onDismiss = { showQrPreview = false }
        )
    }
}

@Composable
fun MenuSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun MediaCard(
    title: String,
    imageUrl: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DormRed.copy(alpha = 0.1f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "?",
                                fontSize = 22.sp,
                                color = DormRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Belum tersedia", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(
    menu: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val formattedHarga = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(menu.harga)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menu.namaMenu,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = menu.tipe,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formattedHarga,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DormRed
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (menu.ketersediaan) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else Color.Gray.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (menu.ketersediaan) "Tersedia" else "Habis",
                        fontSize = 11.sp,
                        color = if (menu.ketersediaan) Color(0xFF4CAF50) else Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (menu.ketersediaan) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilledIconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(30.dp),
                            enabled = quantity > 0,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (quantity > 0) DormRed else Color.Gray.copy(alpha = 0.25f),
                                disabledContainerColor = Color.Gray.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = "−",
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                        }

                        Text(
                            text = quantity.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(min = 24.dp)
                        )

                        FilledIconButton(
                            onClick = onAdd,
                            modifier = Modifier.size(30.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = DormRed
                            )
                        ) {
                            Text(
                                text = "+",
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}