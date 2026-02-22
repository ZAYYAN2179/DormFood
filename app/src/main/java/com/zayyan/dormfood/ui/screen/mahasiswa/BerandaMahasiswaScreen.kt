package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.core.net.toUri

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
    val context = androidx.compose.ui.platform.LocalContext.current
    var showImagePreview by remember { mutableStateOf(false) }

    val isOpen = warung.layananAntar

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isOpen) { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 100.dp)
            ) {
                if (warung.fotoMenu.isNotEmpty()) {
                    AsyncImage(
                        model = warung.fotoMenu,
                        contentDescription = warung.namaWarung,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showImagePreview = true },
                        contentScale = ContentScale.Crop,
                        alpha = if (isOpen) 1f else 0.4f
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = warung.namaWarung,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOpen) Color.Black else Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (warung.alamatWarung.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = DormRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = warung.alamatWarung,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOpen) Color(0xFF16A34A) else Color(0xFFE53935).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isOpen) "BUKA" else "TUTUP",
                            color = if (isOpen) Color.White else Color(0xFFE53935),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    if (warung.phone.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF25D366),
                            modifier = Modifier.clickable {
                                val formatted = warung.phone.trim().let {
                                    if (it.startsWith("0")) "+62" + it.drop(1) else it
                                }
                                val uri = "https://wa.me/$formatted".toUri()
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW, uri
                                )
                                context.startActivity(intent)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = warung.phone,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showImagePreview && warung.fotoMenu.isNotEmpty()) {
        ImagePreviewDialog(
            imageUrl = warung.fotoMenu,
            title = warung.namaWarung,
            onDismiss = { showImagePreview = false }
        )
    }
}