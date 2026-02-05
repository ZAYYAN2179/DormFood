package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.component.BottomBarWarung
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.model.AuthViewModel
import com.zayyan.dormfood.ui.model.WarungViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.vectorResource
import coil.compose.AsyncImage
import com.zayyan.dormfood.R

@Composable
fun ProfilWarung(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    warungViewModel: WarungViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val isLoading by warungViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = "Profil Warung")
        },
        bottomBar = {
            BottomBarWarung(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = Background
    ) { paddingValues ->
        ProfilWarungContent(
            paddingValues = paddingValues,
            navController = navController,
            authViewModel = authViewModel,
            warungViewModel = warungViewModel,
            isLoading = isLoading
        )
    }
}

@Composable
fun ProfilWarungContent(
    paddingValues: PaddingValues,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    warungViewModel: WarungViewModel,
    isLoading: Boolean
) {
    val warungData by warungViewModel.warungData.collectAsState()
    val warungProfile by warungViewModel.warungProfile.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditPhoneDialog by remember { mutableStateOf(false) }
    var showEditAlamatDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar(
                message = successMessage,
                duration = SnackbarDuration.Short
            )
            showSuccessSnackbar = false
        }
    }

    val menuLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            warungViewModel.uploadMenuImage(it)
            successMessage = "Foto menu berhasil diupload"
            showSuccessSnackbar = true
        }
    }

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            warungViewModel.uploadQrImage(it)
            successMessage = "QR Pembayaran berhasil diupload"
            showSuccessSnackbar = true
        }
    }

    SnackbarHost(snackbarHostState)

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = warungData?.name ?: "Nama Warung",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = warungData?.email ?: "email@example.com",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { showEditNameDialog = true }
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
                icon = ImageVector.vectorResource(id = R.drawable.media),
                title = "Media Warung"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImprovedUploadCard(
                    title = "Daftar Menu",
                    imageUrl = warungProfile?.fotoMenu,
                    onClick = { menuLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )
                ImprovedUploadCard(
                    title = "QR Pembayaran",
                    imageUrl = warungProfile?.fotoQr,
                    onClick = { qrLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle(
                icon = Icons.Default.Info,
                title = "Informasi Warung"
            )
            Spacer(modifier = Modifier.height(12.dp))

            ImprovedInfoCard(
                icon = Icons.Outlined.Phone,
                title = "Nomor Telepon",
                value = warungData?.phone?.takeIf { it.isNotEmpty() } ?: "Belum diisi",
                onEditClick = { showEditPhoneDialog = true }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ImprovedInfoCard(
                icon = Icons.Outlined.LocationOn,
                title = "Alamat Warung",
                value = warungProfile?.alamatWarung?.takeIf { it.isNotEmpty() } ?: "Belum diisi",
                onEditClick = { showEditAlamatDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DormRed
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
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
        ImprovedAlertDialog(
            icon = Icons.AutoMirrored.Outlined.ExitToApp,
            title = "Keluar dari Akun?",
            message = "Anda akan keluar dari akun warung ini. Pastikan semua perubahan telah tersimpan.",
            confirmText = "Ya, Keluar",
            dismissText = "Batal",
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                authViewModel.logout()
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    if (showEditNameDialog) {
        EditTextDialog(
            title = "Edit Nama Warung",
            label = "Nama Warung",
            initialValue = warungData?.name ?: "",
            placeholder = "Masukkan nama warung",
            icon = Icons.Default.Person,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                warungViewModel.updateName(
                    newName = newName,
                    onSuccess = {
                        showEditNameDialog = false
                        successMessage = "Nama warung berhasil diperbarui"
                        showSuccessSnackbar = true
                    },
                    onError = { error ->
                        successMessage = "Gagal memperbarui: $error"
                        showSuccessSnackbar = true
                    }
                )
            }
        )
    }

    if (showEditAlamatDialog) {
        EditTextDialog(
            title = "Edit Alamat Warung",
            label = "Alamat",
            initialValue = warungProfile?.alamatWarung ?: "",
            placeholder = "Masukkan alamat warung",
            icon = Icons.Default.LocationOn,
            onDismiss = { showEditAlamatDialog = false },
            onConfirm = { newAlamat ->
                warungViewModel.updateAlamatWarung(
                    newAlamat = newAlamat,
                    onSuccess = {
                        showEditAlamatDialog = false
                        successMessage = "Alamat warung berhasil diperbarui"
                        showSuccessSnackbar = true
                    },
                    onError = { error ->
                        successMessage = "Gagal memperbarui: $error"
                        showSuccessSnackbar = true
                    }
                )
            }
        )
    }

    if (showEditPhoneDialog) {
        EditTextDialog(
            title = "Edit Nomor Telepon",
            label = "Nomor Telepon",
            initialValue = warungData?.phone ?: "",
            placeholder = "Contoh: 081234567890",
            icon = Icons.Default.Phone,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
            onDismiss = { showEditPhoneDialog = false },
            onConfirm = { newPhone ->
                warungViewModel.updatePhone(
                    newPhone = newPhone,
                    onSuccess = {
                        showEditPhoneDialog = false
                        successMessage = "Nomor telepon berhasil diperbarui"
                        showSuccessSnackbar = true
                    },
                    onError = { error ->
                        successMessage = "Gagal memperbarui: $error"
                        showSuccessSnackbar = true
                    }
                )
            }
        )
    }
}

@Composable
fun SectionTitle(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DormRed,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EditTextDialog(
    title: String,
    label: String,
    initialValue: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(initialValue) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DormRed,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        isError = it.isBlank()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(label) },
                    placeholder = { Text(placeholder, color = Color.Gray) },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = keyboardType
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DormRed,
                        focusedLabelColor = DormRed
                    )
                )
                if (isError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Field tidak boleh kosong",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textValue.isNotBlank()) {
                        onConfirm(textValue)
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DormRed
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ImprovedUploadCard(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DormRed,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = DormRed.copy(alpha = 0.1f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = DormRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap untuk upload",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ImprovedInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    onEditClick: () -> Unit
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
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ✅ HANYA ICON YANG CLICKABLE
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = DormRed,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onEditClick() }
            )
        }
    }
}

@Composable
fun ImprovedAlertDialog(
    icon: ImageVector,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DormRed,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Gray,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DormRed
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(dismissText, color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}