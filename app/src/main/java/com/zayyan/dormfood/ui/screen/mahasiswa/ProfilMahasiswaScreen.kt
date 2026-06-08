package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.R
import com.zayyan.dormfood.ui.component.BottomBarMahasiswa
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.viewmodel.AuthViewModel
import com.zayyan.dormfood.ui.viewmodel.MahasiswaViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.screen.warung.AlertDialog
import com.zayyan.dormfood.ui.screen.warung.EditTextDialog
import com.zayyan.dormfood.ui.screen.warung.SectionTitle
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed

@Composable
fun ProfilMahasiswa(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    mahasiswaViewModel: MahasiswaViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        mahasiswaViewModel.fetchMahasiswaData()
    }

    DisposableEffect(Unit) {
        onDispose {
            mahasiswaViewModel.clearUpdateMessage()
        }
    }

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val isLoading by mahasiswaViewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val updateMessage by mahasiswaViewModel.updateMessage.collectAsState()
    LaunchedEffect(updateMessage) {
        updateMessage?.let { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            mahasiswaViewModel.clearUpdateMessage()
        }
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = Background
    ) { paddingValues ->
        ProfilMahasiswaContent(
            paddingValues = paddingValues,
            navController = navController,
            authViewModel = authViewModel,
            mahasiswaViewModel = mahasiswaViewModel,
            isLoading = isLoading
        )
    }
}

@Composable
fun ProfilMahasiswaContent(
    paddingValues: PaddingValues,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mahasiswaViewModel: MahasiswaViewModel,
    isLoading: Boolean
) {
    val mahasiswaData by mahasiswaViewModel.mahasiswaData.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditBuildingDialog by remember { mutableStateOf(false) }
    var showEditRoomDialog by remember { mutableStateOf(false) }

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
                Text(
                    text = "Memuat data...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mahasiswaData?.name ?: "Nama Mahasiswa",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mahasiswaData?.email ?: "email@example.com",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { showEditNameDialog = true },
                            modifier = Modifier.testTag("editNama")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Nama",
                                tint = DormRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(
                icon = Icons.Default.Info,
                title = "Informasi Mahasiswa"
            )
            Spacer(modifier = Modifier.height(12.dp))

            MahasiswaInfoCard(
                icon = Icons.Outlined.Home,
                title = "Gedung",
                value = mahasiswaData?.building?.takeIf { it.isNotEmpty() } ?: "Belum diisi",
                onEditClick = { showEditBuildingDialog = true },
                testTag = "editGedung"
            )

            Spacer(modifier = Modifier.height(12.dp))

            MahasiswaInfoCard(
                icon = ImageVector.vectorResource(id = R.drawable.nomorkamar),
                title = "Nomor Kamar",
                value = mahasiswaData?.roomNumber?.takeIf { it.isNotEmpty() } ?: "Belum diisi",
                onEditClick = { showEditRoomDialog = true },
                testTag = "editKamar"
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btnLogout"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DormRed),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    width = 1.5.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(DormRed)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Keluar dari Akun",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            icon = Icons.AutoMirrored.Outlined.ExitToApp,
            title = "Keluar dari Akun?",
            message = "Anda akan keluar dari akun mahasiswa ini.",
            confirmText = "Ya, Keluar",
            dismissText = "Batal",
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                mahasiswaViewModel.clearData()
                authViewModel.logout()
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    if (showEditNameDialog) {
        EditTextDialog(
            title = "Edit Nama",
            label = "Nama",
            initialValue = mahasiswaData?.name ?: "",
            placeholder = "Masukkan nama lengkap",
            icon = Icons.Default.Person,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                mahasiswaViewModel.updateName(
                    newName = newName,
                    onSuccess = {
                        showEditNameDialog = false
                        mahasiswaViewModel.setUpdateMessage("Nama berhasil diperbarui")
                    },
                    onError = { error ->
                        mahasiswaViewModel.setUpdateMessage("Gagal memperbarui: $error")
                    }
                )
            }
        )
    }

    if (showEditBuildingDialog) {
        EditTextDialog(
            title = "Edit Gedung",
            label = "Gedung",
            initialValue = mahasiswaData?.building ?: "",
            placeholder = "Contoh: 6",
            icon = Icons.Default.Home,
            onDismiss = { showEditBuildingDialog = false },
            onConfirm = { newBuilding ->
                mahasiswaViewModel.updateBuilding(
                    newBuilding = newBuilding,
                    onSuccess = {
                        showEditBuildingDialog = false
                        mahasiswaViewModel.setUpdateMessage("Gedung berhasil diperbarui")
                    },
                    onError = { error ->
                        mahasiswaViewModel.setUpdateMessage("Gagal memperbarui: $error")
                    }
                )
            }
        )
    }

    if (showEditRoomDialog) {
        EditTextDialog(
            title = "Edit Nomor Kamar",
            label = "Nomor Kamar",
            initialValue = mahasiswaData?.roomNumber ?: "",
            placeholder = "Contoh: 418",
            icon = ImageVector.vectorResource(id = R.drawable.nomorkamar),
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
            onDismiss = { showEditRoomDialog = false },
            onConfirm = { newRoom ->
                mahasiswaViewModel.updateRoomNumber(
                    newRoomNumber = newRoom,
                    onSuccess = {
                        showEditRoomDialog = false
                        mahasiswaViewModel.setUpdateMessage("Nomor kamar berhasil diperbarui")
                    },
                    onError = { error ->
                        mahasiswaViewModel.setUpdateMessage("Gagal memperbarui: $error")
                    }
                )
            }
        )
    }
}

@Composable
fun MahasiswaInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    onEditClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = DormRed.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DormRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onEditClick,
                modifier = Modifier.testTag(testTag)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = DormRed
                )
            }
        }
    }
}