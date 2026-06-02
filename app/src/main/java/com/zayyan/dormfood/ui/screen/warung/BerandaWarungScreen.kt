package com.zayyan.dormfood.ui.screen.warung

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.component.BottomBarWarung
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel
import com.zayyan.dormfood.ui.model.StatusPesanan
import com.zayyan.dormfood.ui.viewmodel.WarungViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun BerandaWarung(
    navController: NavHostController,
    warungViewModel: WarungViewModel = viewModel(),
    pesananViewModel: PesananViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        warungViewModel.loadWarungData()
        pesananViewModel.refreshWarungOrders()
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
        BerandaWarungContent(
            paddingValues = paddingValues,
            warungViewModel = warungViewModel,
            pesananViewModel = pesananViewModel,
            navController = navController
        )
    }
}

@Composable
fun BerandaWarungContent(
    paddingValues: PaddingValues,
    warungViewModel: WarungViewModel = viewModel(),
    pesananViewModel: PesananViewModel = viewModel(),
    navController: NavHostController
) {
    val warungProfile by warungViewModel.warungProfile.collectAsState()
    val warungData by warungViewModel.warungData.collectAsState()
    val orders by pesananViewModel.warungOrders.collectAsState()
    val isLoadingOrders by pesananViewModel.isLoadingWarungOrders.collectAsState()

    val selesaiOrders = orders.filter { it.statusPesanan == StatusPesanan.SELESAI.label }

    val totalPemasukan = selesaiOrders.sumOf { it.totalHarga }

    val totalPesanan = selesaiOrders.size

    var layananAntarState by remember(warungProfile?.layananAntar) {
        mutableStateOf(warungProfile?.layananAntar ?: false)
    }

    val bulanList = remember(selesaiOrders) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
        listOf("Semua Bulan") + selesaiOrders
            .mapNotNull { it.createdAt?.toDate() }
            .map { sdf.format(it) }
            .distinct()
    }

    var selectedBulan by remember { mutableStateOf("Semua Bulan") }
    var showBulanDropdown by remember { mutableStateOf(false) }

    val filteredSelesai = remember(selesaiOrders, selectedBulan) {
        if (selectedBulan == "Semua Bulan") {
            selesaiOrders
        } else {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
            selesaiOrders.filter { order ->
                order.createdAt?.toDate()?.let { sdf.format(it) } == selectedBulan
            }
        }
    }

    val formatRupiah = { amount: Long ->
        "Rp ${NumberFormat.getNumberInstance(Locale.forLanguageTag("id-ID")).format(amount)}"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(48.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(DormRed)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Total Pemasukan",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatRupiah(totalPemasukan),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DormRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF1976D2))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Total Pesanan",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$totalPesanan Pesanan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛵", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Status Delivery",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A0A00)
                        )
                    }
                    Switch(
                        checked = layananAntarState,
                        onCheckedChange = { isOn ->
                            layananAntarState = isOn
                            warungViewModel.updateLayananAntar(
                                isOn,
                                onError = {
                                    layananAntarState = !isOn
                                }
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DormRed,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFBDBDBD)
                        )
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        warungData?.uid?.let { uid ->
                            navController.navigate(Screen.RatingUlasanWarungScreen.createRoute(uid))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DormRed)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lihat Penilaian dan Ulasan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { navController.navigate(Screen.AnalisisPenjualanWarungScreen.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DormRed
                    )
                ) {
                    Text(text = "📊", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analisis Penjualan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "History Pesanan",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Box {
                    OutlinedButton(
                        onClick = { showBulanDropdown = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1A0A00)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBDBDBD))
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (selectedBulan == "Semua Bulan") "Filter Bulan"
                            else selectedBulan.split(" ").first(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showBulanDropdown,
                        onDismissRequest = { showBulanDropdown = false }
                    ) {
                        bulanList.forEach { bulan ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = bulan,
                                        fontSize = 13.sp,
                                        fontWeight = if (bulan == selectedBulan)
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (bulan == selectedBulan) DormRed
                                        else Color.Black
                                    )
                                },
                                onClick = {
                                    selectedBulan = bulan
                                    showBulanDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isLoadingOrders) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = DormRed)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Memuat pesanan...", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else if (filteredSelesai.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum ada pesanan selesai",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        if (selectedBulan != "Semua Bulan") {
                            Text(
                                text = "di bulan $selectedBulan",
                                fontSize = 13.sp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredSelesai, key = { it.orderId }) { order ->
                HistoryPesananCard(
                    order = order,
                    formatRupiah = formatRupiah
                )
            }
        }
    }
}

@Composable
fun HistoryPesananCard(
    order: OrderItem,
    formatRupiah: (Long) -> String
) {
    val sdfDisplay = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
    val tanggal = order.createdAt?.toDate()?.let { sdfDisplay.format(it) } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        text = buildString {
                            append(order.namaUser.ifBlank { "Pelanggan" })
                            if (order.building.isNotBlank()) append(" - Gedung ${order.building}")
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A0A00),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tanggal,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF2E7D32)
                ) {
                    Text(
                        text = "Selesai",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF5F5F5))
            Spacer(modifier = Modifier.height(8.dp))

            order.items.forEach { item ->
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
                Spacer(modifier = Modifier.height(3.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Harga",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Text(
                    text = formatRupiah(order.totalHarga),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DormRed
                )
            }
        }
    }
}