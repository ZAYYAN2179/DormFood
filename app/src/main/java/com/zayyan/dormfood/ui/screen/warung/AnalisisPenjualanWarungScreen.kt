package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.model.RingkasanPenjualan
import com.zayyan.dormfood.ui.model.StatusPesanan
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import com.zayyan.dormfood.ui.utils.RekapPdfHelper
import com.zayyan.dormfood.ui.viewmodel.AnalisisPenjualanViewModel
import com.zayyan.dormfood.ui.viewmodel.MenuViewModel
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel

private val CardBg = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalisisPenjualanWarungScreen(
    navController: NavHostController,
    pesananViewModel: PesananViewModel = viewModel(),
    analisisPenjualanViewModel: AnalisisPenjualanViewModel = viewModel(),
    menuViewModel: MenuViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        pesananViewModel.refreshWarungOrders()
    }

    val context = LocalContext.current
    var selectedPeriode by remember { mutableStateOf(AnalisisPenjualanViewModel.Periode.SEMUA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Analisis Penjualan",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DormRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { padding ->
        AnalisisPenjualanWarungContent(
            paddingValues = padding,
            pesananViewModel = pesananViewModel,
            analisisPenjualanViewModel = analisisPenjualanViewModel,
            menuViewModel = menuViewModel,
            selectedPeriode = selectedPeriode,
            onPeriodeChange = { selectedPeriode = it },
            context = context,
            navController = navController
        )
    }
}

@Composable
fun AnalisisPenjualanWarungContent(
    paddingValues: PaddingValues,
    pesananViewModel: PesananViewModel,
    analisisPenjualanViewModel: AnalisisPenjualanViewModel,
    menuViewModel: MenuViewModel,
    selectedPeriode: AnalisisPenjualanViewModel.Periode,
    onPeriodeChange: (AnalisisPenjualanViewModel.Periode) -> Unit,
    context: android.content.Context,
    navController: NavHostController
) {
    val warungOrders by pesananViewModel.warungOrders.collectAsState()
    val isLoading    by pesananViewModel.isLoadingWarungOrders.collectAsState()
    val allMenus     by menuViewModel.menuList.collectAsState()

    val completedOrders = remember(warungOrders) {
        warungOrders.filter { it.statusPesanan == StatusPesanan.SELESAI.label }
    }

    val filteredOrders = remember(completedOrders, selectedPeriode) {
        analisisPenjualanViewModel.filterOrders(completedOrders, selectedPeriode)
    }

    val topMenus = remember(filteredOrders) {
        analisisPenjualanViewModel.getMenuTerlaris(filteredOrders)
    }

    val menuKurangLaris = remember(filteredOrders, allMenus) {
        analisisPenjualanViewModel.getMenuKurangLaris(filteredOrders, allMenus)
    }

    val ringkasan = remember(filteredOrders, allMenus) {
        analisisPenjualanViewModel.getRingkasanPrediksi(filteredOrders, allMenus)
    }

    val insights = remember(filteredOrders, allMenus) {
        analisisPenjualanViewModel.getInsight(filteredOrders, allMenus)
    }

    val periodeLabel = when (selectedPeriode) {
        AnalisisPenjualanViewModel.Periode.HARI_7  -> "7 Hari Terakhir"
        AnalisisPenjualanViewModel.Periode.HARI_30 -> "30 Hari Terakhir"
        AnalisisPenjualanViewModel.Periode.SEMUA   -> "Semua Waktu"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {

        item {
            PeriodeFilterRow(
                selected = selectedPeriode,
                onSelect = { onPeriodeChange(it) }
            )
        }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DormRed)
                }
            }
            return@LazyColumn
        }

        if (filteredOrders.isEmpty()) {
            item { EmptyCard() }
            return@LazyColumn
        }

        item {
            RingkasanCard(ringkasan = ringkasan)
        }

        item {
            Button(
                onClick = {
                    RekapPdfHelper.cetakRekap(
                        context = context,
                        orders = filteredOrders,
                        periodeLabel = periodeLabel
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DormRed)
            ) {
                Icon(
                    Icons.Default.Print,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Cetak Laporan Penjualan $periodeLabel",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        item {
            Button(
                onClick = { navController.navigate(Screen.PrediksiStokWarungScreen.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DormRed)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Prediksi Stok Menu",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        item {
            SectionLabel(
                title = "Analisis",
                icon = {
                    Icon(
                        Icons.Default.Lightbulb,
                        null,
                        tint = Color(0xFFF9A825),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        item {
            SectionCard {
                insights.forEach {
                    Text("• $it", fontSize = 13.sp, lineHeight = 20.sp)
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        item {
            SectionLabel(
                title = "Menu Terlaris",
                icon = {
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = Color(0xFFF57F17),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        item {
            SectionCard {
                if (topMenus.isEmpty()) {
                    EmptyText("Belum ada data")
                } else {
                    topMenus.forEachIndexed { index, (nama, qty) ->
                        MenuRankRow(
                            rank = index + 1,
                            namaMenu = nama,
                            nilai = "$qty terjual",
                            showDivider = index < topMenus.lastIndex
                        )
                    }
                }
            }
        }

        item {
            SectionLabel(
                title = "Menu Belum Pernah Dipesan",
                icon = {
                    Icon(
                        Icons.Default.RemoveShoppingCart,
                        null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        item {
            SectionCard {
                if (menuKurangLaris.isEmpty()) {
                    EmptyText("Semua menu pernah terjual")
                } else {
                    menuKurangLaris.forEachIndexed { index, (nama, _) ->
                        MenuBelumDipesanRow(
                            rank = index + 1,
                            namaMenu = nama,
                            showDivider = index < menuKurangLaris.lastIndex
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}


@Composable
fun RingkasanCard(ringkasan: RingkasanPenjualan) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Ringkasan Penjualan",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF212121)
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RingkasanItem(
                    modifier = Modifier.weight(1f),
                    label    = "Total Pesanan",
                    value    = ringkasan.totalPesanan.toString(),
                    bgColor  = Color(0xFFFFF3E0),
                    valueColor = Color(0xFFE65100)
                )
                RingkasanItem(
                    modifier = Modifier.weight(1f),
                    label    = "Total Menu",
                    value    = ringkasan.totalMenu.toString(),
                    bgColor  = Color(0xFFE8F5E9),
                    valueColor = Color(0xFF2E7D32)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RingkasanItem(
                    modifier = Modifier.weight(1f),
                    label = "Menu Terjual",
                    value = ringkasan.menuTerjual.toString(),
                    subtitle = "${ringkasan.menuTerjual} dari ${ringkasan.totalMenu} menu",
                    bgColor = Color(0xFFE3F2FD),
                    valueColor = Color(0xFF1565C0)
                )

                RingkasanItem(
                    modifier = Modifier.weight(1f),
                    label = "Belum Terjual",
                    value = ringkasan.menuBelumTerjual.toString(),
                    subtitle = "${ringkasan.menuBelumTerjual} dari ${ringkasan.totalMenu} menu",
                    bgColor = Color(0xFFFFEBEE),
                    valueColor = Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
private fun RingkasanItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    subtitle: String = "",
    bgColor: Color,
    valueColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = valueColor
            )

            Text(
                label,
                fontSize = 11.sp,
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )

            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontSize = 10.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, icon: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        icon()
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun MenuRankRow(
    rank: Int,
    namaMenu: String,
    nilai: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val (badgeBg, badgeColor) = when (rank) {
                1    -> Color(0xFFFFD700) to Color(0xFF5D4037)
                2    -> Color(0xFFB0BEC5) to Color(0xFF37474F)
                3    -> Color(0xFFCD7F32) to Color.White
                else -> Color(0xFFF5F5F5) to Color(0xFF616161)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Text(rank.toString(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = badgeColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(namaMenu, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(nilai, fontSize = 12.sp, color = Color.Gray)
            }
        }
        if (showDivider) HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
    }
}

@Composable
private fun MenuBelumDipesanRow(
    rank: Int,
    namaMenu: String,
    showDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "$rank.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828),
                modifier = Modifier.width(24.dp)
            )
            Text(
                namaMenu,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF212121)
            )
        }
        if (showDivider) HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
    }
}

@Composable
fun EmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
            Spacer(Modifier.height(12.dp))
            Text("Belum ada data penjualan", fontWeight = FontWeight.Medium, color = Color.Gray)
            Text("pada periode ini", fontSize = 13.sp, color = Color.LightGray)
        }
    }
}

@Composable
fun PeriodeFilterRow(
    selected: AnalisisPenjualanViewModel.Periode,
    onSelect: (AnalisisPenjualanViewModel.Periode) -> Unit
) {
    val options = listOf(
        AnalisisPenjualanViewModel.Periode.HARI_7 to "7 Hari",
        AnalisisPenjualanViewModel.Periode.HARI_30 to "30 Hari",
        AnalisisPenjualanViewModel.Periode.SEMUA to "Semua"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { (periode, label) ->
            val isActive = selected == periode

            Surface(
                onClick = { onSelect(periode) },
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
}

@Composable
fun EmptyText(text: String) {
    Text(text, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
}