package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.viewmodel.OrderItem
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel
import com.zayyan.dormfood.ui.viewmodel.StatusPesanan
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

// ── Data model hasil prediksi ─────────────────────────────────────
data class PrediksiMenu(
    val namaMenu: String,
    val totalTerjual: Int,
    val rataHarian: Double,
    val rataMingguan: Double,
    val rekomendasiStok3Hari: Int,
    val rekomendasiStok7Hari: Int,
    val tren: TrenPenjualan
)

enum class TrenPenjualan { NAIK, TURUN, STABIL }

// ── Periode filter ────────────────────────────────────────────────
enum class PeriodePrediksi(val label: String, val hari: Int) {
    TIGA_HARI("3 Hari", 3),
    TUJUH_HARI("7 Hari", 7),
    EMPAT_BELAS_HARI("14 Hari", 14)
}

// ── Fungsi kalkulasi statistik ────────────────────────────────────
fun hitungPrediksi(orders: List<OrderItem>): List<PrediksiMenu> {
    val selesai = orders.filter { it.statusPesanan == StatusPesanan.SELESAI.label }
    if (selesai.isEmpty()) return emptyList()

    val tanggalList = selesai.mapNotNull { it.createdAt?.toDate() }
    if (tanggalList.isEmpty()) return emptyList()

    val minDate = tanggalList.minOf { it.time }
    val maxDate = tanggalList.maxOf { it.time }
    val hariAktif = max(1, ((maxDate - minDate) / (1000 * 60 * 60 * 24) + 1).toInt())

    val penjualanPerMenu = mutableMapOf<String, Int>()
    for (order in selesai) {
        for (item in order.items) {
            penjualanPerMenu[item.namaMenu] =
                (penjualanPerMenu[item.namaMenu] ?: 0) + item.quantity
        }
    }

    val batas7Hari = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
    val batas14Hari = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -14) }.time
    val penjualan7Hari = mutableMapOf<String, Int>()
    val penjualan7_14Hari = mutableMapOf<String, Int>()

    for (order in selesai) {
        val tgl = order.createdAt?.toDate() ?: continue
        for (item in order.items) {
            when {
                tgl.after(batas7Hari) ->
                    penjualan7Hari[item.namaMenu] =
                        (penjualan7Hari[item.namaMenu] ?: 0) + item.quantity
                tgl.after(batas14Hari) ->
                    penjualan7_14Hari[item.namaMenu] =
                        (penjualan7_14Hari[item.namaMenu] ?: 0) + item.quantity
            }
        }
    }

    return penjualanPerMenu.entries
        .sortedByDescending { it.value }
        .map { (nama, total) ->
            val rataHarian = total.toDouble() / hariAktif
            val rataMingguan = rataHarian * 7
            val jual7 = penjualan7Hari[nama] ?: 0
            val jual7_14 = penjualan7_14Hari[nama] ?: 0
            val tren = when {
                jual7_14 == 0 -> TrenPenjualan.STABIL
                jual7 > jual7_14 * 1.1 -> TrenPenjualan.NAIK
                jual7 < jual7_14 * 0.9 -> TrenPenjualan.TURUN
                else -> TrenPenjualan.STABIL
            }
            val multiplier = if (tren == TrenPenjualan.NAIK) 1.2 else 1.0
            PrediksiMenu(
                namaMenu = nama,
                totalTerjual = total,
                rataHarian = rataHarian,
                rataMingguan = rataMingguan,
                rekomendasiStok3Hari = ceil(rataHarian * 3 * multiplier).toInt().coerceAtLeast(1),
                rekomendasiStok7Hari = ceil(rataMingguan * multiplier).toInt().coerceAtLeast(1),
                tren = tren
            )
        }
}

// ── Akurasi prediksi berdasarkan jumlah data ──────────────────────
fun hitungAkurasi(totalPesanan: Int): Int = when {
    totalPesanan >= 100 -> 90
    totalPesanan >= 50  -> 80
    totalPesanan >= 20  -> 70
    totalPesanan >= 10  -> 60
    else                -> 50
}

// ── Screen utama ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrediksiStokMenuWarungScreen(
    navController: NavHostController,
    pesananViewModel: PesananViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        pesananViewModel.refreshWarungOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Prediksi Stok Menu",
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
        containerColor = Background
    ) { paddingValues ->
        PrediksiStokMenuWarungContent(
            paddingValues = paddingValues,
            pesananViewModel = pesananViewModel
        )
    }
}

// ── Konten ────────────────────────────────────────────────────────
@Composable
fun PrediksiStokMenuWarungContent(
    paddingValues: PaddingValues,
    pesananViewModel: PesananViewModel = viewModel()
) {
    val orders by pesananViewModel.warungOrders.collectAsState()
    val isLoading by pesananViewModel.isLoadingWarungOrders.collectAsState()

    val selesaiOrders = orders.filter { it.statusPesanan == StatusPesanan.SELESAI.label }

    // Daftar bulan unik
    val sdfBulan = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    val bulanList = remember(selesaiOrders) {
        listOf("Semua Bulan") + selesaiOrders
            .mapNotNull { it.createdAt?.toDate() }
            .map { sdfBulan.format(it) }
            .distinct()
    }

    // State filter
    var selectedBulan by remember { mutableStateOf("Semua Bulan") }
    var selectedMenu by remember { mutableStateOf("Pilih Menu") }
    var selectedPeriode by remember { mutableStateOf(PeriodePrediksi.TUJUH_HARI) }
    var showDropdownBulan by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showDropdownPeriode by remember { mutableStateOf(false) }
    var hasilTampil by remember { mutableStateOf(false) }

    // Filter orders berdasarkan bulan
    val ordersTerfilter = remember(selesaiOrders, selectedBulan) {
        if (selectedBulan == "Semua Bulan") selesaiOrders
        else selesaiOrders.filter { order ->
            order.createdAt?.toDate()?.let { sdfBulan.format(it) } == selectedBulan
        }
    }

    // Daftar menu dari bulan terpilih
    val daftarMenu = remember(ordersTerfilter) {
        ordersTerfilter.flatMap { it.items }.map { it.namaMenu }.distinct().sorted()
    }

    // Prediksi untuk menu terpilih
    val prediksiTerpilih = remember(ordersTerfilter, selectedMenu) {
        if (selectedMenu == "Pilih Menu") null
        else hitungPrediksi(ordersTerfilter).find { it.namaMenu == selectedMenu }
    }

    val totalPesananMenu = remember(ordersTerfilter, selectedMenu) {
        if (selectedMenu == "Pilih Menu") 0
        else ordersTerfilter.sumOf { order ->
            order.items.filter { it.namaMenu == selectedMenu }.sumOf { it.quantity }
        }
    }

    val rekomendasiStok = remember(prediksiTerpilih, selectedPeriode) {
        prediksiTerpilih?.let {
            ceil(
                it.rataHarian * selectedPeriode.hari *
                        (if (it.tren == TrenPenjualan.NAIK) 1.2 else 1.0)
            ).toInt().coerceAtLeast(1)
        } ?: 0
    }

    val akurasi = remember(totalPesananMenu) { hitungAkurasi(totalPesananMenu) }

    // Loading
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = DormRed)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Menganalisis data pesanan...", color = Color.Gray, fontSize = 14.sp)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // ── Dropdown filter bulan ─────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { showDropdownBulan = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1A0A00)
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    width = 1.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBDBDBD))
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedBulan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = showDropdownBulan,
                onDismissRequest = { showDropdownBulan = false }
            ) {
                bulanList.forEach { bulan ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = bulan,
                                fontSize = 13.sp,
                                fontWeight = if (bulan == selectedBulan) FontWeight.Bold else FontWeight.Normal,
                                color = if (bulan == selectedBulan) DormRed else Color.Black
                            )
                        },
                        onClick = {
                            selectedBulan = bulan
                            selectedMenu = "Pilih Menu"
                            hasilTampil = false
                            showDropdownBulan = false
                        }
                    )
                }
            }
        }

        // ── Row: dropdown menu + dropdown periode + tombol terapkan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown pilih menu
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { showDropdownMenu = true },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A0A00)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBDBDBD))
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedMenu,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false }
                ) {
                    if (daftarMenu.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Belum ada data", color = Color.Gray, fontSize = 13.sp) },
                            onClick = { showDropdownMenu = false }
                        )
                    } else {
                        daftarMenu.forEach { menu ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = menu,
                                        fontSize = 13.sp,
                                        fontWeight = if (menu == selectedMenu) FontWeight.Bold else FontWeight.Normal,
                                        color = if (menu == selectedMenu) DormRed else Color.Black
                                    )
                                },
                                onClick = {
                                    selectedMenu = menu
                                    hasilTampil = false
                                    showDropdownMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Dropdown periode
            Box(modifier = Modifier.weight(0.7f)) {
                OutlinedButton(
                    onClick = { showDropdownPeriode = true },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A0A00)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBDBDBD))
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = selectedPeriode.label, fontSize = 12.sp)
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                DropdownMenu(
                    expanded = showDropdownPeriode,
                    onDismissRequest = { showDropdownPeriode = false }
                ) {
                    PeriodePrediksi.entries.forEach { periode ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = periode.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (periode == selectedPeriode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (periode == selectedPeriode) DormRed else Color.Black
                                )
                            },
                            onClick = {
                                selectedPeriode = periode
                                hasilTampil = false
                                showDropdownPeriode = false
                            }
                        )
                    }
                }
            }

            // Tombol terapkan
            Button(
                onClick = { if (selectedMenu != "Pilih Menu") hasilTampil = true },
                modifier = Modifier.height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DormRed),
                enabled = selectedMenu != "Pilih Menu"
            ) {
                Text(text = "Terapkan", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── Hasil prediksi ────────────────────────────────────────
        if (hasilTampil && prediksiTerpilih != null) {

            // Card: hasil prediksi menu
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Menu: ${prediksiTerpilih.namaMenu}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A0A00)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(10.dp))
                    HasilRow(label = "Permintaan Prediksi", value = "$rekomendasiStok porsi")
                    Spacer(modifier = Modifier.height(6.dp))
                    HasilRow(label = "Akurasi Prediksi", value = "$akurasi%")
                    Spacer(modifier = Modifier.height(6.dp))
                    HasilRow(label = "Rekomendasi Stok", value = "$rekomendasiStok porsi")
                }
            }

            // Card: ringkasan (border biru seperti referensi)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFF1976D2),
                        shape = RoundedCornerShape(12.dp)
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A0A00)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HasilRow(label = "Total Pesanan", value = "$totalPesananMenu porsi")
                    Spacer(modifier = Modifier.height(6.dp))
                    HasilRow(
                        label = "Rata-rata / hari",
                        value = "${"%.1f".format(prediksiTerpilih.rataHarian)} porsi"
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HasilRow(
                        label = "Tren Penjualan",
                        value = when (prediksiTerpilih.tren) {
                            TrenPenjualan.NAIK   -> "↑ Naik"
                            TrenPenjualan.TURUN  -> "↓ Turun"
                            TrenPenjualan.STABIL -> "→ Stabil"
                        },
                        valueColor = when (prediksiTerpilih.tren) {
                            TrenPenjualan.NAIK   -> Color(0xFF2E7D32)
                            TrenPenjualan.TURUN  -> Color(0xFFE53935)
                            TrenPenjualan.STABIL -> Color(0xFF1976D2)
                        }
                    )
                }
            }

        } else if (selesaiOrders.isEmpty()) {
            // Belum ada data sama sekali
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada data pesanan selesai",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else if (!hasilTampil) {
            // Petunjuk awal
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pilih bulan, menu, dan periode prediksi, lalu tekan Terapkan untuk melihat hasil.",
                        fontSize = 12.sp,
                        color = Color(0xFF795548),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// ── Helper composable ─────────────────────────────────────────────
@Composable
fun HasilRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF1A0A00)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.Gray)
        Text(
            text = ": $value",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPrediksiStokMenuWarungV2() {

    // Dummy prediksi
    val dummyPrediksi = PrediksiMenu(
        namaMenu = "Nasi Goreng Spesial",
        totalTerjual = 120,
        rataHarian = 10.5,
        rataMingguan = 73.5,
        rekomendasiStok3Hari = 32,
        rekomendasiStok7Hari = 88,
        tren = TrenPenjualan.NAIK
    )

    val selectedPeriode = PeriodePrediksi.TUJUH_HARI
    val rekomendasiStok = 88
    val akurasi = 85
    val totalPesananMenu = 120

    MaterialTheme {
        Surface(color = Background) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Dropdown Bulan (Dummy) ──
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Januari 2026")
                        Icon(Icons.Default.KeyboardArrowDown, null)
                    }
                }

                // ── Row filter ──
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Menu
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Nasi Goreng Spesial",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
                        }
                    }

                    // Periode
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier.weight(0.7f).height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(selectedPeriode.label, fontSize = 12.sp)
                            Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(16.dp))
                        }
                    }

                    // Button
                    Button(
                        onClick = {},
                        modifier = Modifier.height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DormRed)
                    ) {
                        Text("Terapkan", fontSize = 13.sp)
                    }
                }

                // ── Card hasil ──
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = "Menu: ${dummyPrediksi.namaMenu}",
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        HasilRow("Permintaan Prediksi", "$rekomendasiStok porsi")
                        Spacer(modifier = Modifier.height(6.dp))
                        HasilRow("Akurasi Prediksi", "$akurasi%")
                        Spacer(modifier = Modifier.height(6.dp))
                        HasilRow("Rekomendasi Stok", "$rekomendasiStok porsi")
                    }
                }

                // ── Card ringkasan ──
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF1976D2), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text("Ringkasan", fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))

                        HasilRow("Total Pesanan", "$totalPesananMenu porsi")
                        Spacer(modifier = Modifier.height(6.dp))
                        HasilRow("Rata-rata / hari", "10.5 porsi")
                        Spacer(modifier = Modifier.height(6.dp))
                        HasilRow(
                            "Tren Penjualan",
                            "↑ Naik",
                            Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}