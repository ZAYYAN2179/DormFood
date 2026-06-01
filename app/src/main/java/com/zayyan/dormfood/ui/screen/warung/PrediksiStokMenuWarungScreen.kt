package com.zayyan.dormfood.ui.screen.warung

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.model.StatusPesanan
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel
import com.zayyan.dormfood.ui.viewmodel.PrediksiViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrediksiStokMenuWarungScreen(
    navController: NavHostController,
    pesananViewModel: PesananViewModel = viewModel(),
    prediksiViewModel: PrediksiViewModel = viewModel()
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
            pesananViewModel = pesananViewModel,
            prediksiViewModel = prediksiViewModel
        )
    }
}

@Composable
fun PrediksiStokMenuWarungContent(
    paddingValues: PaddingValues,
    pesananViewModel: PesananViewModel,
    prediksiViewModel: PrediksiViewModel
) {

    val warungOrders by pesananViewModel.warungOrders.collectAsState()
    val completedOrders = remember(warungOrders) {
        warungOrders.filter {
            it.statusPesanan == StatusPesanan.SELESAI.label
        }
    }
    var selectedPeriode by remember {
        mutableStateOf(
            PrediksiViewModel.Periode.HARI_30
        )
    }
    val filteredOrders = remember(
        completedOrders,
        selectedPeriode
    ) {

        prediksiViewModel.filterOrders(
            completedOrders,
            selectedPeriode
        )
    }
    val topMenus = remember(filteredOrders) {
        prediksiViewModel.getMenuTerlaris(filteredOrders)
    }
    val menuKurangLaris = remember(filteredOrders) {
        prediksiViewModel.getMenuKurangLaris(
            filteredOrders
        )
    }
    val rekomendasiStok = remember(
        filteredOrders,
        selectedPeriode
    ) {
        prediksiViewModel.getRekomendasiStok(
            filteredOrders,
            selectedPeriode
        )
    }
    val rataRataPenjualan = remember(
        filteredOrders,
        selectedPeriode
    ) {
        prediksiViewModel.getRataRataPenjualanHarian(
            filteredOrders,
            selectedPeriode
        )
    }

    LaunchedEffect(warungOrders) {
        Log.d("PREDIKSI", warungOrders.toString())
    }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(
                    selected = selectedPeriode ==
                            PrediksiViewModel.Periode.HARI_7,
                    onClick = {
                        selectedPeriode =
                            PrediksiViewModel.Periode.HARI_7
                    },
                    label = {
                        Text("7 Hari")
                    }
                )

                FilterChip(
                    selected = selectedPeriode ==
                            PrediksiViewModel.Periode.HARI_30,
                    onClick = {
                        selectedPeriode =
                            PrediksiViewModel.Periode.HARI_30
                    },
                    label = {
                        Text("30 Hari")
                    }
                )

                FilterChip(
                    selected = selectedPeriode ==
                            PrediksiViewModel.Periode.SEMUA,
                    onClick = {
                        selectedPeriode =
                            PrediksiViewModel.Periode.SEMUA
                    },
                    label = {
                        Text("Semua")
                    }
                )

            }

        }
        item {

            if (filteredOrders.isEmpty()) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {

                    Text(
                        text = "Belum ada data penjualan pada periode ini",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Ringkasan Prediksi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    if (rekomendasiStok.isNotEmpty()) {

                        Text(
                            text =
                                "Menu yang perlu diprioritaskan adalah ${rekomendasiStok.first().first} dengan estimasi stok ${rekomendasiStok.first().second} porsi."
                        )
                    }
                }
            }
        }

        item {
            InfoCard(
                title = "Total Pesanan",
                value = filteredOrders.size.toString()
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Menu Terlaris",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    topMenus.forEachIndexed { index, menu ->

                        Text(
                            "${index + 1}. ${menu.first} (${menu.second})"
                        )
                    }
                }
            }
        }

        item {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Menu Kurang Laris",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    menuKurangLaris.forEachIndexed { index, menu ->

                        Text(
                            text =
                                "${index + 1}. ${menu.first} (${menu.second})"
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Rekomendasi Stok Hari Ini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    rekomendasiStok.forEach { menu ->

                        Text(
                            text = "${menu.first} : ${menu.second} porsi"
                        )
                    }
                }
            }
        }

        if (selectedPeriode != PrediksiViewModel.Periode.SEMUA) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "Rata-rata Penjualan Harian",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    rataRataPenjualan.forEach { menu ->

                        Text(
                            text = "${menu.first} : ${
                                String.format(
                                    Locale.forLanguageTag("id-ID"),
                                    "%.1f",
                                    menu.second
                                )
                            } porsi/hari"
                        )
                    }
                }
            }
        }
            }

        item {
            Button(
                onClick = {
                    pesananViewModel.refreshWarungOrders()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DormRed
                )
            ) {
                Text("Refresh Prediksi")
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

