package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
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
import com.zayyan.dormfood.ui.model.HasilPrediksi
import com.zayyan.dormfood.ui.model.PrediksiMenuResponse
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import com.zayyan.dormfood.ui.viewmodel.PrediksiStokViewModel
import com.zayyan.dormfood.ui.viewmodel.PrediksiUiState
import com.zayyan.dormfood.ui.viewmodel.WarningLevel

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
                    warningLevel = currentState.warningLevel
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
    warningLevel: WarningLevel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BannerDataWarning(warningLevel)
        }
        item {
            SummaryCard(data)
        }
        item {
            Text(
                "Detail Per Menu",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        items(data.hasil) { menu ->
            PrediksiMenuCard(menu)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
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