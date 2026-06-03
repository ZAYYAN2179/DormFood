package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.zayyan.dormfood.ui.component.BottomBarMahasiswa
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.model.ItemPesanan
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel
import com.zayyan.dormfood.ui.viewmodel.RatingViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PesananMahasiswa(
    navController: NavHostController,
    pesananViewModel: PesananViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = { TopAppBar(title = "Dorm Food") },
        bottomBar = {
            BottomBarMahasiswa(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = Background
    ) { paddingValues ->
        PesananMahasiswaContent(
            paddingValues = paddingValues,
            pesananViewModel = pesananViewModel,
            navController = navController
        )
    }
}

@Composable
fun PesananMahasiswaContent(
    paddingValues: PaddingValues,
    pesananViewModel: PesananViewModel,
    navController: NavHostController
) {
    val ratingViewModel: RatingViewModel = viewModel()

    val orders by pesananViewModel.orders.collectAsState()
    val isLoading by pesananViewModel.isLoadingOrders.collectAsState()


    val aktif = orders.filter {
        it.statusPesanan.lowercase() in listOf(
            "menunggu konfirmasi", "dibuat", "diantar"
        )
    }
    val selesai = orders.filter { it.statusPesanan == "Selesai" }

    val formatRupiah = { amount: Long ->
        "Rp ${NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount)}"
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DormRed)
        }
        return
    }

    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛍️", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Belum ada pesanan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Text(
                    text = "Yuk pesan makanan di warung favoritmu!",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (aktif.isNotEmpty()) {
            item { SectionLabel(title = "Status Pesanan") }
            items(aktif, key = { it.orderId }) { order ->
                OrderCard(
                    order = order,
                    formatRupiah = formatRupiah,
                    showReviewButton = false,
                    navController = navController,
                    ratingViewModel = ratingViewModel
                )
            }
        }

        if (selesai.isNotEmpty()) {
            item { SectionLabel(title = "Riwayat Pesanan") }
            items(selesai, key = { it.orderId }) { order ->
                OrderCard(
                    order = order,
                    formatRupiah = formatRupiah,
                    showReviewButton = true,
                    navController = navController,
                    ratingViewModel = ratingViewModel
                )
            }
        }
    }
}

@Composable
fun SectionLabel(title: String) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(2.dp))
        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
    }
}

@Composable
fun OrderCard(
    order: OrderItem,
    formatRupiah: (Long) -> String,
    showReviewButton: Boolean,
    navController: NavHostController,
    ratingViewModel: RatingViewModel,
) {
    var expanded by remember { mutableStateOf(true) }

    var sudahDiulas by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(order.orderId) {
        if (showReviewButton) {
            ratingViewModel.cekSudahDiulas(order.orderId) { hasil ->
                sudahDiulas = hasil
            }
        }
    }

    val statusColor = when (order.statusPesanan) {
        "Menunggu Konfirmasi" -> Color(0xFFE53935)
        "Dibuat" -> Color(0xFFF57C00)
        "Diantar" -> Color(0xFF1976D2)
        "Selesai" -> Color(0xFF2E7D32)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.namaWarung,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatTimestamp(order.createdAt),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = statusColor
                    ) {
                        Text(
                            text = order.statusPesanan,
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    order.items.forEach { item ->
                        OrderItemRow(item = item, formatRupiah = formatRupiah)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Harga",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DormRed
                        )
                        Text(
                            text = formatRupiah(order.totalHarga),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DormRed
                        )
                    }

                    if (showReviewButton) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            when (sudahDiulas) {
                                null -> {
                                    OutlinedButton(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                true -> {
                                    OutlinedButton(
                                        onClick = {},
                                        enabled = false,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            disabledContentColor = Color(0xFF9E9E9E)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp, Color(0xFFBDBDBD)
                                        )
                                    ) {
                                        Text(
                                            text = "✓ Ulasan Terkirim",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                false -> {
                                    OutlinedButton(
                                        onClick = {
                                            navController.navigate(
                                                Screen.RatingUlasanMahasiswaScreen.createRoute(
                                                    orderId = order.orderId,
                                                    warungId = order.warungId,
                                                    namaWarung = order.namaWarung
                                                )
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFF57C00)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp, Color(0xFFF57C00)
                                        )
                                    ) {
                                        Text(
                                            text = "Berikan Ulasan",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    navController.navigate(
                                        Screen.MenuMahasiswaScreen.createRoute(userId = order.warungId)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DormRed)
                            ) {
                                Text(
                                    text = "Pesan Lagi",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemRow(item: ItemPesanan, formatRupiah: (Long) -> String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${item.quantity}x  ${item.namaMenu}",
            fontSize = 13.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = formatRupiah(item.harga.toLong() * item.quantity),
            fontSize = 13.sp,
            color = Color.DarkGray
        )
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm z", Locale.forLanguageTag("id-ID"))
    return sdf.format(timestamp.toDate())
}