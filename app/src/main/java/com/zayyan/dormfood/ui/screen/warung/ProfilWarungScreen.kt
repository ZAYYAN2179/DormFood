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
import com.zayyan.dormfood.ui.viewmodel.AuthViewModel
import com.zayyan.dormfood.ui.viewmodel.WarungViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.zayyan.dormfood.R

@Composable
fun ProfilWarung(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    warungViewModel: WarungViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        warungViewModel.loadWarungData()
    }

    DisposableEffect(Unit) {
        onDispose {
            warungViewModel.clearUpdateMessage()
        }
    }

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val isLoading by warungViewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val updateMessage by warungViewModel.updateMessage.collectAsState()
    LaunchedEffect(updateMessage) {
        updateMessage?.let { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            warungViewModel.clearUpdateMessage()
        }
    }

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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
    var showEditOngkirDialog by remember {
        mutableStateOf(false)
    }

    val menuLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { warungViewModel.uploadMenuImage(it) }
    }

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { warungViewModel.uploadQrImage(it) }
    }

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
                UploadCard(
                    title = "Daftar Menu",
                    imageUrl = warungProfile?.fotoMenu,
                    onClick = { menuLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                )
                UploadCard(
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

            InfoCard(
                icon = Icons.Outlined.Phone,
                title = "Nomor Telepon",
                value = warungData?.phone?.takeIf { it.isNotEmpty() } ?: "Belum diisi",
                onEditClick = { showEditPhoneDialog = true }
            )
            Spacer(modifier = Modifier.height(12.dp))
            InfoCard(
                icon = Icons.Outlined.LocationOn,
                title = "Alamat Warung",
                value = warungProfile?.alamatWarung?.takeIf { it.isNotEmpty() } ?: "Belum diisi",
                onEditClick = { showEditAlamatDialog = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoCard(
                icon = Icons.Default.LocalShipping,
                title = "Biaya Pengantaran",
                value = if (warungProfile?.ongkirAktif == true)
                    "Rp ${warungProfile?.ongkir}"
                else
                    "Tidak Aktif",
                onEditClick = {
                    showEditOngkirDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            val hasLocation = (warungProfile?.latitude ?: 0.0) != 0.0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.PetaWarungScreen.route)
                    },
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
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = DormRed,
                                modifier = Modifier.size(20.dp)
                            )

                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text = "Titik Lokasi Peta",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (hasLocation)
                                "%.5f, %.5f".format(
                                    warungProfile?.latitude,
                                    warungProfile?.longitude
                                )
                            else
                                "Belum diatur",

                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Atur Lokasi",
                        tint = DormRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

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
                border = ButtonDefaults.outlinedButtonBorder(
                    enabled = true
                ).copy(
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
            message = "Anda akan keluar dari akun warung ini. Pastikan semua perubahan telah tersimpan.",
            confirmText = "Ya, Keluar",
            dismissText = "Batal",
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                warungViewModel.clearData()
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
            maxLength = 50,
            initialValue = warungData?.name ?: "",
            placeholder = "Masukkan nama warung",
            icon = Icons.Default.Person,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                warungViewModel.updateName(
                    newName = newName,
                    onSuccess = {
                        showEditNameDialog = false
                        warungViewModel.setUpdateMessage("Nama warung berhasil diperbarui")
                    },
                    onError = { error ->
                        warungViewModel.setUpdateMessage("Gagal memperbarui: $error")
                    }
                )
            }
        )
    }

    if (showEditAlamatDialog) {
        EditTextDialog(
            title = "Edit Alamat Warung",
            label = "Alamat",
            maxLength = 50,
            initialValue = warungProfile?.alamatWarung ?: "",
            placeholder = "Masukkan alamat warung",
            icon = Icons.Default.LocationOn,
            onDismiss = { showEditAlamatDialog = false },
            onConfirm = { newAlamat ->
                warungViewModel.updateAlamatWarung(
                    newAlamat = newAlamat,
                    onSuccess = {
                        showEditAlamatDialog = false
                        warungViewModel.setUpdateMessage("Alamat warung berhasil diperbarui")
                    },
                    onError = { error ->
                        warungViewModel.setUpdateMessage("Gagal memperbarui: $error")
                    }
                )
            }
        )
    }

    if (showEditOngkirDialog) {
        EditOngkirDialog(
            initialEnabled = warungProfile?.ongkirAktif ?: false,
            initialOngkir = warungProfile?.ongkir ?: 0,
            onDismiss = {
                showEditOngkirDialog = false
            },
            onConfirm = { aktif, nominal ->

                warungViewModel.updateOngkir(
                    ongkirAktif = aktif,
                    ongkir = nominal,
                    onSuccess = {
                        showEditOngkirDialog = false

                        warungViewModel.setUpdateMessage(
                            "Biaya pengantaran berhasil diperbarui"
                        )
                    },
                    onError = { error ->
                        warungViewModel.setUpdateMessage(
                            "Gagal memperbarui: $error"
                        )
                    }
                )
            }
        )
    }

    if (showEditPhoneDialog) {
        EditTextDialog(
            title = "Edit Nomor Telepon",
            label = "Nomor Telepon",
            maxLength = 15,
            initialValue = warungData?.phone ?: "",
            placeholder = "Contoh: 081234567890",
            icon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            onDismiss = { showEditPhoneDialog = false },
            onConfirm = { newPhone ->
                warungViewModel.updatePhone(
                    newPhone = newPhone,
                    onSuccess = {
                        showEditPhoneDialog = false
                        warungViewModel.setUpdateMessage("Nomor telepon berhasil diperbarui")
                    },
                    onError = { error ->
                        warungViewModel.setUpdateMessage("Gagal memperbarui: $error")
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
    keyboardType: KeyboardType = KeyboardType.Text,
    maxLength: Int = 50,
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
                        if (it.length <= maxLength) {
                            textValue = it
                            isError = it.isBlank()
                        }
                    },
                    supportingText = {
                        Text("${textValue.length}/$maxLength")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(label) },
                    placeholder = { Text(placeholder, color = Color.Gray) },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
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
fun UploadCard(
    title: String,
    imageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showImagePreview by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .height(180.dp)
            .clickable {
                if (!imageUrl.isNullOrEmpty()) {
                    showImagePreview = true
                } else {
                    onClick()
                }
            },
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
                        modifier = Modifier
                            .size(36.dp)
                            .clickable(
                                onClick = { onClick() },
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                            )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
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
                        textAlign = TextAlign.Center
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

    if (showImagePreview && !imageUrl.isNullOrEmpty()) {
        ImagePreviewDialog(
            imageUrl = imageUrl,
            title = title,
            onDismiss = { showImagePreview = false }
        )
    }
}

@Composable
fun ImagePreviewDialog(
    imageUrl: String,
    title: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentScale = ContentScale.Fit
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp),
                shape = CircleShape,
                color = DormRed,
                shadowElevation = 8.dp
            ) {
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.75f)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun EditOngkirDialog(
    initialEnabled: Boolean,
    initialOngkir: Int,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Int) -> Unit
) {
    var ongkirAktif by remember {
        mutableStateOf(initialEnabled)
    }

    var nominal by remember {
        mutableStateOf(
            if (initialOngkir == 0)
                ""
            else
                initialOngkir.toString()
        )
    }

    var isError by remember {
        mutableStateOf(false)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        icon = {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = DormRed
            )
        },

        title = {
            Text(
                "Biaya Pengantaran",
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aktifkan Ongkir",
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = ongkirAktif,
                        onCheckedChange = {
                            ongkirAktif = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DormRed,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFBDBDBD)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nominal,
                    onValueChange = {
                        nominal = it.filter { c ->
                            c.isDigit()
                        }

                        isError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = ongkirAktif,
                    label = {
                        Text("Nominal Ongkir")
                    },
                    placeholder = {
                        Text(
                            "Contoh: 3000",
                            color = Color.Gray
                        )
                    },
                    prefix = {
                        Text("Rp ")
                    },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
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
                        text = "Nominal ongkir wajib diisi",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {

                    if (ongkirAktif) {

                        val nilai = nominal.toIntOrNull()

                        if (nilai == null || nilai <= 0) {
                            isError = true
                            return@Button
                        }

                        onConfirm(
                            true,
                            nilai
                        )
                    } else {

                        onConfirm(
                            false,
                            0
                        )
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
                onClick = onDismiss
            ) {
                Text("Batal", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun InfoCard(
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
fun AlertDialog(
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