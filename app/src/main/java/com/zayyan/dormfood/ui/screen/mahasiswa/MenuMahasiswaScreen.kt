package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuMahasiswaContent(
    paddingValues: PaddingValues,
    detail: MenuWarungDetail,
    menuViewModel: MenuViewModel
) {
    val menuList by menuViewModel.menuListByWarung.collectAsState()
    var showMenuPreview by remember { mutableStateOf(false) }
    val quantities = remember { mutableStateMapOf<String, Int>() }

    val makanan = menuList.filter { it.tipe.contains("makanan", ignoreCase = true) }
    val minuman = menuList.filter { it.tipe.contains("minuman", ignoreCase = true) }
    val lainnya = menuList.filter {
        !it.tipe.contains("makanan", ignoreCase = true) &&
                !it.tipe.contains("minuman", ignoreCase = true)
    }

    val totalHarga = menuList.sumOf { menu ->
        val qty = quantities[menu.namaMenu] ?: 0
        if (qty > 0) menu.harga.toLong() * qty else 0L
    }

    val formatRupiah = { amount: Long ->
        "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)}"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = if (totalHarga > 0) 88.dp else 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(130.dp)
                                .fillMaxHeight()
                        ) {
                            if (detail.fotoMenu.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                                    onClick = { showMenuPreview = true }
                                ) {
                                    AsyncImage(
                                        model = detail.fotoMenu,
                                        contentDescription = "Foto Menu",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFF0F0F0))
                                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📋", fontSize = 32.sp)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = detail.namaWarung,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "5.0 / 3 ulasan",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Button(
                                onClick = { },
                                colors = ButtonDefaults.buttonColors(containerColor = DormRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lihat Ulasan", fontSize = 12.sp, color = Color.White)
                            }
                            if (detail.phone.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Phone,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = detail.phone,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (menuList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
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
                stickyHeader {
                    MenuSectionHeader(title = "Makanan")
                }
                item {
                    MenuSectionCard(
                        menuList = makanan,
                        quantities = quantities
                    )
                }
            }

            if (minuman.isNotEmpty()) {
                stickyHeader {
                    MenuSectionHeader(title = "Minuman")
                }
                item {
                    MenuSectionCard(
                        menuList = minuman,
                        quantities = quantities
                    )
                }
            }

            if (lainnya.isNotEmpty()) {
                stickyHeader {
                    MenuSectionHeader(title = "🛒 Lainnya")
                }
                item {
                    MenuSectionCard(
                        menuList = lainnya,
                        quantities = quantities
                    )
                }
            }
        }

        if (totalHarga > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = DormRed),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Pesanan",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = formatRupiah(totalHarga),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Bayar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
}

@Composable
fun MenuSectionCard(
    menuList: List<MenuItem>,
    quantities: androidx.compose.runtime.snapshots.SnapshotStateMap<String, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        menuList.forEachIndexed { index, menu ->
            val key = menu.namaMenu
            MenuItemRow(
                menu = menu,
                quantity = quantities[key] ?: 0,
                onAdd = {
                    quantities[key] = (quantities[key] ?: 0) + 1
                },
                onRemove = {
                    val current = quantities[key] ?: 0
                    if (current > 0) quantities[key] = current - 1
                }
            )
            if (index < menuList.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    color = Color(0xFFF0F0F0),
                    thickness = 1.dp
                )
            }
        }
    }
}

@Composable
fun MenuItemRow(
    menu: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val formattedHarga = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(menu.harga)}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = menu.namaMenu,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (menu.ketersediaan) Color.Unspecified else Color.Gray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedHarga,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (menu.ketersediaan) DormRed else Color.Gray
            )
        }

        if (!menu.ketersediaan) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Gray.copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Habis",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledIconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(30.dp),
                    enabled = quantity > 0,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (quantity > 0) DormRed else Color.Gray.copy(alpha = 0.2f),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
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
                    modifier = Modifier.widthIn(min = 22.dp)
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

@Composable
fun MenuSectionHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}