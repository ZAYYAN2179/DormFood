package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.model.StatusPesanan
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import com.zayyan.dormfood.ui.viewmodel.AnalisisPenjualanViewModel
import com.zayyan.dormfood.ui.viewmodel.MenuViewModel
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel

private val CardBg   = Color.White

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
            menuViewModel = menuViewModel
        )
    }
}

@Composable
fun AnalisisPenjualanWarungContent(
    paddingValues: PaddingValues,
    pesananViewModel: PesananViewModel,
    analisisPenjualanViewModel: AnalisisPenjualanViewModel,
    menuViewModel: MenuViewModel
) {
    val warungOrders by pesananViewModel.warungOrders.collectAsState()
    val isLoading    by pesananViewModel.isLoadingWarungOrders.collectAsState()
    val allMenus     by menuViewModel.menuList.collectAsState()

    var selectedPeriode by remember { mutableStateOf(AnalisisPenjualanViewModel.Periode.SEMUA) }

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

    val ringkasan = remember(
        filteredOrders,
        allMenus
    ) {
        analisisPenjualanViewModel.getRingkasanPrediksi(
            filteredOrders,
            allMenus
        )
    }

    val insights = remember(
        filteredOrders,
        allMenus
    ) {
        analisisPenjualanViewModel.getInsight(
            filteredOrders,
            allMenus
        )
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
                onSelect = { selectedPeriode = it }
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

            SectionCard {

                Text(
                    "Ringkasan Penjualan",
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Text("📦 Total Pesanan : ${ringkasan.totalPesanan}")
                Text("🍽️ Total Menu : ${ringkasan.totalMenu}")
                Text("🔥 Menu Pernah Terjual : ${ringkasan.menuTerjual}")
                Text("🚫 Menu Belum Terjual : ${ringkasan.menuBelumTerjual}")
            }
        }

        item {

            SectionLabel(
                title = "Insight",
                icon = {
                    Icon(
                        Icons.Default.AutoGraph,
                        null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        item {

            SectionCard {

                insights.forEach {

                    Text("• $it")

                    Spacer(
                        Modifier.height(6.dp)
                    )
                }
            }
        }

        item {
            SectionLabel(
                title = "Menu Terlaris",
                icon = { Icon(Icons.Default.Star, null, tint = Color(0xFFF57F17), modifier = Modifier.size(18.dp)) }
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
                icon = { Icon(Icons.AutoMirrored.Filled.TrendingDown, null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp)) }
            )
        }
        item {
            SectionCard {
                if (menuKurangLaris.isEmpty()) {
                    EmptyText("Semua menu pernah terjual")
                } else {
                    menuKurangLaris.forEachIndexed { index, (nama, qty) ->
                        MenuKurangLarisRow(
                            rank = index + 1,
                            namaMenu = nama,
                            qty = qty,
                            belumPernah = qty == 0,
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
private fun MenuKurangLarisRow(
    rank: Int,
    namaMenu: String,
    qty: Int,
    belumPernah: Boolean,
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
                color = Color.Gray,
                modifier = Modifier.width(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(namaMenu, fontWeight = FontWeight.Medium, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (belumPernah) "Belum pernah dipesan" else "$qty terjual",
                    fontSize = 12.sp,
                    color = if (belumPernah) Color(0xFFC62828) else Color.Gray
                )
            }
            if (belumPernah) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("0 pesanan", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Medium)
                }
            }
        }
        if (showDivider) HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
    }
}

@Composable
private fun EmptyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
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
private fun PeriodeFilterRow(
    selected: AnalisisPenjualanViewModel.Periode,
    onSelect: (AnalisisPenjualanViewModel.Periode) -> Unit
) {
    val options = listOf(
        AnalisisPenjualanViewModel.Periode.HARI_7  to "7 Hari",
        AnalisisPenjualanViewModel.Periode.HARI_30 to "30 Hari",
        AnalisisPenjualanViewModel.Periode.SEMUA   to "Semua"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { (periode, label) ->
            val isActive = selected == periode
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isActive) DormRed else Color(0xFFF5F5F5))
                    .clickable { onSelect(periode) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) Color.White else Color(0xFF616161)
                )
            }
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(text, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
}