package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.zayyan.dormfood.ui.model.DataGrafikMingguan
import com.zayyan.dormfood.ui.model.GrafikUiState
import com.zayyan.dormfood.ui.model.HasilPrediksi
import com.zayyan.dormfood.ui.model.PrediksiMenuResponse
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import com.zayyan.dormfood.ui.viewmodel.PrediksiStokViewModel
import com.zayyan.dormfood.ui.viewmodel.PrediksiUiState
import com.zayyan.dormfood.ui.viewmodel.WarningLevel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrediksiStokWarungScreen(
    navController: NavHostController,
    prediksiViewModel: PrediksiStokViewModel = viewModel()
) {
    val warungId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var showHelpDialog by remember { mutableStateOf(false) }
    LaunchedEffect(warungId) {
        if (warungId.isNotBlank()) {
            prediksiViewModel.loadPrediksi(warungId)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Prediksi Stok Menu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Bantuan",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = {
                        if (warungId.isNotBlank()) {
                            prediksiViewModel.loadPrediksi(warungId)
                        }
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
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
    ) { padding ->
        PrediksiStokWarungScreenContent(
            paddingValues = padding,
            prediksiViewModel = prediksiViewModel,
            warungId = warungId
        )
        if (showHelpDialog) {
            HelpDialog(onDismiss = { showHelpDialog = false })
        }
    }
}

@Composable
fun PrediksiStokWarungScreenContent(
    paddingValues: PaddingValues,
    prediksiViewModel: PrediksiStokViewModel,
    warungId: String
) {
    val state by prediksiViewModel.prediksiState.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        when (val currentState = state) {
            is PrediksiUiState.Idle -> {
                EmptyStateView(
                    icon = Icons.Default.Analytics,
                    title = "Prediksi Stok Menu",
                    subtitle = "Tekan tombol di bawah untuk melihat prediksi penjualan menu besok berdasarkan data histori pesanan.",
                    buttonText = "Lihat Prediksi",
                    onButtonClick = {
                        if (warungId.isNotBlank()) {
                            prediksiViewModel.loadPrediksi(warungId)
                        }
                    }
                )
            }

            is PrediksiUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = DormRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Menganalisis data penjualan...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            is PrediksiUiState.Success -> {
                PrediksiResultContent(
                    data = currentState.data,
                    warningLevel = currentState.warningLevel,
                    prediksiViewModel = prediksiViewModel,
                    warungId = warungId
                )
            }

            is PrediksiUiState.Error -> {
                EmptyStateView(
                    icon = Icons.Default.ErrorOutline,
                    title = "Gagal Memuat Prediksi",
                    subtitle = currentState.message,
                    buttonText = "Coba Lagi",
                    onButtonClick = {
                        if (warungId.isNotBlank()) {
                            prediksiViewModel.loadPrediksi(warungId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PrediksiResultContent(
    data: PrediksiMenuResponse,
    warningLevel: WarningLevel,
    prediksiViewModel: PrediksiStokViewModel,
    warungId: String
) {
    val availableMenus by prediksiViewModel.availableMenus.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GrafikPenjualanCard(
                prediksiViewModel = prediksiViewModel,
                warungId = warungId,
                availableMenus = availableMenus
            )
        }
        item { BannerDataWarning(warningLevel) }
        item { SummaryCard(data) }
        item {
            Text(
                "Detail Per Menu",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        items(data.hasil) { menu -> PrediksiMenuCard(menu) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SummaryCard(data: PrediksiMenuResponse) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = DormRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ringkasan Prediksi Besok",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(
                    modifier = Modifier.weight(1f),
                    valueNumber = "${data.menuSiapPrediksi}",
                    label = "Pola Stabil",
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    valueNumber = "${data.menuDataKurang}",
                    label = "Data Terbatas",
                    color = Color(0xFFFF9800)
                )
                StatItem(
                    modifier = Modifier.weight(1f),
                    valueNumber = "${data.totalMenu}",
                    label = "Teranalisis",
                    color = Color(0xFF2196F3)
                )
            }
            if (data.isDummy) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Science,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Menggunakan data contoh (dummy)",
                            fontSize = 11.sp,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    valueNumber: String,
    label: String,
    color: Color
) {
    val backgroundColor = color.copy(alpha = 0.08f)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = valueNumber,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "Menu",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PrediksiMenuCard(menu: HasilPrediksi) {
    val statusColor = when (menu.status) {
        "OK" -> Color(0xFF4CAF50)
        "DATA_KURANG" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    menu.menu,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        when (menu.status) {
                            "OK" -> "✅ Pola Stabil"
                            "DATA_KURANG" -> "⚠️ Data Terbatas"
                            else -> "❌ Belum tersedia"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (menu.prediksi != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Prediksi Besok",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            "${menu.prediksi} porsi",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Rata-rata 7 hari",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            "${menu.movingAverage7 ?: "-"} porsi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (menu.movingAverage7 != null && menu.movingAverage7 > 0) {
                    val maxVal = maxOf(menu.prediksi, menu.movingAverage7).toFloat()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Besok",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.width(50.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(
                                        fraction = (menu.prediksi / maxVal).coerceIn(
                                            0f,
                                            1f
                                        )
                                    )
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(statusColor)
                            )
                        }
                        Text(
                            "${menu.prediksi}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(30.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Avg 7h",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            modifier = Modifier.width(50.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0E0E0))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(
                                        fraction = (menu.movingAverage7 / maxVal).coerceIn(
                                            0f,
                                            1f
                                        )
                                    )
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF90CAF9))
                            )
                        }
                        Text(
                            "${menu.movingAverage7}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(30.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Data: ${menu.dataTersedia} hari",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        "Rata-rata harian: ${menu.rataHarian}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = Color(0xFFBDBDBD),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Data belum cukup untuk prediksi",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Tersedia ${menu.dataTersedia}/${menu.dataMinimum} hari",
                        fontSize = 11.sp,
                        color = Color(0xFFBDBDBD)
                    )
                }
            }
        }
    }
}

@Composable
fun BannerDataWarning(warningLevel: WarningLevel) {
    when (warningLevel) {
        WarningLevel.NO_DATA, WarningLevel.SEMUA_KURANG -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Data Pesanan Belum Cukup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF92400E)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Prediksi saat ini menggunakan data contoh. " +
                                    "Akurasi akan meningkat setelah ada 14+ hari data transaksi selesai.",
                            fontSize = 11.sp,
                            color = Color(0xFFB45309),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        WarningLevel.SEBAGIAN_KURANG -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Beberapa menu belum cukup data. " +
                                "Menu bertanda ⚠️ mungkin kurang akurat.",
                        fontSize = 11.sp,
                        color = Color(0xFF075985),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        WarningLevel.NONE -> { /* Tidak tampilkan banner */
        }
    }
}

@Composable
private fun EmptyStateView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    buttonText: String?,
    onButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = Color(0xFFBDBDBD)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                subtitle,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            if (buttonText != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DormRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = DormRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cara Kerja & Istilah",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        text = "Bagaimana Fitur Ini Bekerja?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sistem menggunakan model Machine Learning (Regresi Linear) untuk mempelajari riwayat penjualan menu Anda. Sistem menganalisis tren naik/turun penjualan serta pola hari dalam seminggu (misalnya fluktuasi akhir pekan) untuk memperkirakan kebutuhan stok besok.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }

                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = Color(0xFFEEEEEE)
                    )
                }
                item {
                    Text(
                        text = "Penjelasan Istilah & Singkatan:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Black
                    )
                }
                item {
                    Column {
                        Text(
                            text = "• Prediksi Besok",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DormRed
                        )
                        Text(
                            text = "Jumlah porsi menu yang diperkirakan akan terjual besok berdasarkan perhitungan model kecerdasan buatan.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }
                item {
                    Column {
                        Text(
                            text = "• Avg 7h (Rata-rata 7 Hari)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF1E88E5)
                        )
                        Text(
                            text = "Rata-rata penjualan menu tersebut khusus dalam 7 hari terakhir yang memiliki transaksi. Berguna untuk melihat tren penjualan jangka pendek.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }
                item {
                    Column {
                        Text(
                            text = "• Rata-rata Harian",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Rata-rata penjualan menu per hari yang dihitung dari total seluruh hari data transaksi yang ditarik dari database Anda.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }
                item {
                    Column {
                        Text(
                            text = "• Status Pola Stabil vs Data Terbatas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Label 'Pola Stabil' (Hijau) diberikan jika data minimal 14 hari. Label 'Data Terbatas' (Oranye) diberikan jika data baru berkisar 7-13 hari sehingga tingkat akurasinya masih fluktuatif.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Mengerti",
                    color = DormRed,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Composable
private fun GrafikPenjualanCard(
    prediksiViewModel: PrediksiStokViewModel,
    warungId: String,
    availableMenus: List<String>
) {
    val grafikState by prediksiViewModel.grafikState.collectAsState()

    val bulanOptions = remember {
        val namaBulan = listOf(
            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
        )
        (5 downTo 0).map { offset ->
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -offset) }
            Triple(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                "${namaBulan[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.YEAR)}"
            )
        }
    }

    var selectedBulanIndex by remember { mutableIntStateOf(bulanOptions.lastIndex) }
    var selectedMenu by remember(availableMenus) {
        mutableStateOf(availableMenus.firstOrNull() ?: "")
    }

    var showBulanDropdown by remember { mutableStateOf(false) }
    var showMenuDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(selectedBulanIndex, selectedMenu) {
        if (warungId.isNotBlank() && selectedMenu.isNotBlank()) {
            val (tahun, bulan, _) = bulanOptions[selectedBulanIndex]
            prediksiViewModel.loadGrafik(warungId, tahun, bulan, selectedMenu)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = DormRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tren Penjualan per Minggu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showBulanDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = DormRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            bulanOptions[selectedBulanIndex].third,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showBulanDropdown,
                        onDismissRequest = { showBulanDropdown = false }
                    ) {
                        bulanOptions.forEachIndexed { index, (_, _, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 13.sp) },
                                onClick = {
                                    selectedBulanIndex = index
                                    showBulanDropdown = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showMenuDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = DormRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            selectedMenu.ifBlank { "Pilih Menu" },
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenuDropdown,
                        onDismissRequest = { showMenuDropdown = false }
                    ) {
                        availableMenus.forEach { menu ->
                            DropdownMenuItem(
                                text = { Text(menu, fontSize = 13.sp) },
                                onClick = {
                                    selectedMenu = menu
                                    showMenuDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val s = grafikState) {
                is GrafikUiState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Pilih menu untuk melihat grafik",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                is GrafikUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = DormRed, modifier = Modifier.size(28.dp))
                    }
                }

                is GrafikUiState.Success -> {
                    BarChartContent(data = s.data)
                }

                is GrafikUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Gagal memuat: ${s.message}",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarChartContent(data: List<DataGrafikMingguan>) {
    val normalizedData = (0 until 4).map { i ->
        data.getOrNull(i) ?: DataGrafikMingguan(label = "Mg ${i + 1}", jumlah = 0)
    }

    val maxVal = normalizedData.maxOfOrNull { it.jumlah }?.takeIf { it > 0 } ?: 1

    val nonZeroData = normalizedData.filter { it.jumlah > 0 }
    val trendIcon: androidx.compose.ui.graphics.vector.ImageVector
    val trendColor: Color
    val trendText: String

    if (nonZeroData.size >= 2) {
        val first = nonZeroData.first().jumlah
        val last = nonZeroData.last().jumlah
        when {
            last > first -> {
                trendIcon = Icons.AutoMirrored.Filled.TrendingUp
                trendColor = Color(0xFF4CAF50)
                trendText = "Naik"
            }

            last < first -> {
                trendIcon = Icons.AutoMirrored.Filled.TrendingDown
                trendColor = Color(0xFFF44336)
                trendText = "Turun"
            }

            else -> {
                trendIcon = Icons.AutoMirrored.Filled.TrendingFlat
                trendColor = Color(0xFF9E9E9E)
                trendText = "Stabil"
            }
        }
    } else {
        trendIcon = Icons.AutoMirrored.Filled.TrendingFlat
        trendColor = Color(0xFF9E9E9E)
        trendText = if (nonZeroData.isEmpty()) "Belum Ada Data" else "Data Terbatas"
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                trendIcon,
                contentDescription = null,
                tint = trendColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                trendText,
                fontSize = 12.sp,
                color = trendColor,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Total: ${normalizedData.sumOf { it.jumlah }} porsi",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(4) {
                    HorizontalDivider(
                        color = Color(0xFFF0F0F0),
                        thickness = 0.5.dp
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                normalizedData.forEach { item ->
                    val fraction = item.jumlah.toFloat() / maxVal.toFloat()
                    val isMax = item.jumlah == maxVal
                    val barColor = when {
                        isMax -> DormRed
                        fraction >= 0.6f -> Color(0xFFEF9A9A)
                        fraction > 0f -> Color(0xFFFFCDD2)
                        else -> Color(0xFFF5F5F5)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = if (item.jumlah > 0) "${item.jumlah}" else "0",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMax) DormRed else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(
                                    fraction = if (item.jumlah > 0)
                                        (fraction * 0.85f).coerceAtLeast(0.05f)
                                    else 0.03f
                                )
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            normalizedData.forEach { item ->
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DormRed)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Tertinggi", fontSize = 10.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFFFCDD2))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Lainnya", fontSize = 10.sp, color = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFF5F5F5))
                    .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Tidak ada data", fontSize = 10.sp, color = Color.Gray)
        }
    }
}