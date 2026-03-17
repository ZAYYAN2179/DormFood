package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.zayyan.dormfood.ui.component.BottomBarWarung
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.model.ItemPesanan
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.model.PesananViewModel
import com.zayyan.dormfood.ui.model.StatusPesanan
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val filterLabels = listOf("Semua", "Baru", "Dibuat", "Diantar", "Selesai")

@Composable
fun PesananWarung(
    navController: NavHostController,
    pesananViewModel: PesananViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        pesananViewModel.loadWarungOrders()
    }

    Scaffold(
        topBar = { TopAppBar(title = "Dorm Food") },
        bottomBar = {
            BottomBarWarung(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = Background
    ) { paddingValues ->
        PesananWarungContent(
            paddingValues = paddingValues,
            pesananViewModel = pesananViewModel
        )
    }
}

@Composable
fun PesananWarungContent(
    paddingValues: PaddingValues,
    pesananViewModel: PesananViewModel = viewModel()
) {
    val orders by pesananViewModel.warungOrders.collectAsState()
    val isLoading by pesananViewModel.isLoadingWarungOrders.collectAsState()

    var selectedFilter by remember { mutableStateOf("Semua") }
    var showBuktiDialog by remember { mutableStateOf(false) }
    var buktiUrl by remember { mutableStateOf("") }

    val formatRupiah = { amount: Long ->
        "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)}"
    }

    val filteredOrders = when (selectedFilter) {
        "Baru"    -> orders.filter {
            it.statusPesanan == StatusPesanan.MENUNGGU_KONFIRMASI.label
        }
        "Dibuat"  -> orders.filter { it.statusPesanan == StatusPesanan.DIBUAT.label }
        "Diantar" -> orders.filter { it.statusPesanan == StatusPesanan.DIANTAR.label }
        "Selesai" -> orders.filter { it.statusPesanan == StatusPesanan.SELESAI.label }
        else      -> orders
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DormRed)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Memuat pesanan...", color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Daftar Pesanan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filterLabels) { label ->
                            val isActive = selectedFilter == label
                            Surface(
                                onClick = { selectedFilter = label },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isActive) DormRed else Color.White,
                                shadowElevation = if (isActive) 0.dp else 1.dp
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) Color.White else Color.DarkGray,
                                    modifier = Modifier.padding(
                                        horizontal = 18.dp,
                                        vertical = 8.dp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📋", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak ada pesanan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Text(
                                text = "Pesanan akan muncul di sini",
                                fontSize = 13.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            } else {
                items(filteredOrders, key = { it.orderId }) { order ->
                    WarungOrderCard(
                        order = order,
                        formatRupiah = formatRupiah,
                        onStatusChange = { statusPesanan ->
                            pesananViewModel.updateStatusPesanan(order.orderId, statusPesanan)
                        },
                        onLihatBukti = { url ->
                            buktiUrl = url
                            showBuktiDialog = true
                        }
                    )
                }
            }
        }
    }

    // Dialog preview bukti pembayaran
    if (showBuktiDialog && buktiUrl.isNotEmpty()) {
        ImagePreviewDialog(
            imageUrl = buktiUrl,
            title = "Bukti Pembayaran",
            onDismiss = {
                showBuktiDialog = false
                buktiUrl = ""
            }
        )
    }
}

@Composable
fun WarungOrderCard(
    order: OrderItem,
    formatRupiah: (Long) -> String,
    onStatusChange: (StatusPesanan) -> Unit,
    onLihatBukti: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    var showKonfirmasiDialog by remember { mutableStateOf(false) }
    var statusYakanDiubah by remember { mutableStateOf<StatusPesanan?>(null) }

    val statusColor = when (order.statusPesanan) {
        StatusPesanan.MENUNGGU_KONFIRMASI.label -> Color(0xFFE53935)
        StatusPesanan.DIBUAT.label              -> Color(0xFFF57C00)
        StatusPesanan.DIANTAR.label             -> Color(0xFF1976D2)
        StatusPesanan.SELESAI.label             -> Color(0xFF2E7D32)
        else                                    -> Color.Gray
    }

    // Setiap status hanya punya 1 pilihan selanjutnya (bertahap)
    val nextStatuses: List<StatusPesanan> = when (order.statusPesanan) {
        StatusPesanan.MENUNGGU_KONFIRMASI.label -> listOf(StatusPesanan.DIBUAT)
        StatusPesanan.DIBUAT.label             -> listOf(StatusPesanan.DIANTAR)
        StatusPesanan.DIANTAR.label            -> listOf(StatusPesanan.SELESAI)
        else                                   -> emptyList()
    }

    // Dialog konfirmasi ubah status
    if (showKonfirmasiDialog && statusYakanDiubah != null) {
        AlertDialog(
            onDismissRequest = {
                showKonfirmasiDialog = false
                statusYakanDiubah = null
            },
            title = { Text("Ubah Status Pesanan", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin mengubah status pesanan menjadi \"${statusYakanDiubah!!.label}\"?",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        statusYakanDiubah?.let { onStatusChange(it) }
                        showKonfirmasiDialog = false
                        statusYakanDiubah = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DormRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ya, Ubah", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showKonfirmasiDialog = false
                        statusYakanDiubah = null
                    }
                ) {
                    Text("Batal", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
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
                        text = order.namaWarung.ifBlank { "Pelanggan" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatTimestampWarung(order.createdAt),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(20.dp), color = statusColor) {
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
                        WarungOrderItemRow(item = item, formatRupiah = formatRupiah)
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tombol lihat bukti bayar
                        if (order.buktiPembayaranUrl.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onLihatBukti(order.buktiPembayaranUrl) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF1A0A00)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, Color(0xFFBDBDBD)
                                )
                            ) {
                                Text(
                                    text = "Bukti Bayar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Tombol ubah status — hanya tampil jika ada next status
                        if (nextStatuses.isNotEmpty()) {
                            val nextStatus = nextStatuses.first()
                            val buttonColor = when (nextStatus) {
                                StatusPesanan.DIBUAT  -> Color(0xFFF57C00)
                                StatusPesanan.DIANTAR -> Color(0xFF1976D2)
                                StatusPesanan.SELESAI -> Color(0xFF2E7D32)
                                else                  -> DormRed
                            }
                            Button(
                                onClick = {
                                    // Tampilkan konfirmasi sebelum ubah
                                    statusYakanDiubah = nextStatus
                                    showKonfirmasiDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor
                                )
                            ) {
                                Text(
                                    text = nextStatus.label,
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
fun WarungOrderItemRow(item: ItemPesanan, formatRupiah: (Long) -> String) {
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

fun formatTimestampWarung(timestamp: Timestamp?): String {
    if (timestamp == null) return ""
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm z", Locale("id", "ID"))
    return sdf.format(timestamp.toDate())
}