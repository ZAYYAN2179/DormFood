package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.zayyan.dormfood.ui.component.BottomBarMahasiswa
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.model.WarungItem
import com.zayyan.dormfood.ui.model.WarungViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.screen.warung.ImagePreviewDialog
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed

@Composable
fun BerandaMahasiswa(
    navController: NavHostController,
    warungViewModel: WarungViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    LaunchedEffect(Unit) {
        warungViewModel.loadAllWarungs()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = "Dorm Food")
        },
        bottomBar = {
            BottomBarMahasiswa(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = Background
    ) { paddingValues ->
        BerandaMahasiswaContent(
            paddingValues = paddingValues,
            navController = navController,
            warungViewModel = warungViewModel
        )
    }
}

@Composable
fun BerandaMahasiswaContent(
    paddingValues: PaddingValues,
    navController: NavHostController,
    warungViewModel: WarungViewModel
) {
    val warungs by warungViewModel.warungList.collectAsState()
    val isLoading by warungViewModel.isLoadingList.collectAsState()

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
                Text(text = "Memuat warung...", color = Color.Gray, fontSize = 14.sp)
            }
        }
    } else if (warungs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada warung tersedia",
                color = Color.Gray,
                fontSize = 15.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Daftar Warung",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(warungs) { warung ->
                WarungCard(
                    warung = warung,
                    onClick = {
                        navController.navigate(
                            Screen.MenuMahasiswaScreen.createRoute(warung.userId)
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun WarungCard(
    warung: WarungItem,
    onClick: () -> Unit
) {
    var showImagePreview by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                if (warung.fotoMenu.isNotEmpty()) {
                    AsyncImage(
                        model = warung.fotoMenu,
                        contentDescription = "Foto Menu ${warung.namaWarung}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showImagePreview = true },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DormRed.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = warung.namaWarung.take(1).uppercase(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DormRed
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = warung.namaWarung,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (warung.alamatWarung.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = DormRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = warung.alamatWarung,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                if (warung.phone.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Phone,
                            contentDescription = null,
                            tint = DormRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = warung.phone,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }

    if (showImagePreview && warung.fotoMenu.isNotEmpty()) {
        ImagePreviewDialog(
            imageUrl = warung.fotoMenu,
            title = "Menu ${warung.namaWarung}",
            onDismiss = { showImagePreview = false }
        )
    }
}