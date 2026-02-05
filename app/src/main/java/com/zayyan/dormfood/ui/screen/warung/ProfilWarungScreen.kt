package com.zayyan.dormfood.ui.screen.warung

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun ProfilWarung(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    warungViewModel: WarungViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val warungData by warungViewModel.warungData.collectAsState()
    val warungProfile by warungViewModel.warungProfile.collectAsState()
    val isLoading by warungViewModel.isLoading.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditPhoneDialog by remember { mutableStateOf(false) }

    val menuLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            warungViewModel.uploadMenuImage(it)
        }
    }

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            warungViewModel.uploadQrImage(it)
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
        containerColor = Background
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DormRed)
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
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = warungData?.name ?: "Nama Warung",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = DormRed,
                                modifier = Modifier.clickable {
                                    showEditNameDialog = true
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = warungData?.email ?: "email@example.com",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Daftar Menu dan QR Pembayaran",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                UploadCard(
                    title = "Upload Daftar Menu",
                    onClick = { menuLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(12.dp))

                UploadCard(
                    title = "Upload QR Pembayaran",
                    onClick = { qrLauncher.launch("image/*") }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { /* simpan */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DormRed,
                        disabledContainerColor = Color.LightGray
                    )
                ) {
                    Text("Simpan")
                }

                Spacer(modifier = Modifier.height(24.dp))

                InfoCard(
                    icon = Icons.Default.Phone,
                    text = warungData?.phone ?: "Nomor Telepon",
                    trailingIcon = Icons.Default.Edit,
                    onClick = {
                        showEditPhoneDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                var showEditAlamatDialog by remember { mutableStateOf(false) }

                InfoCard(
                    icon = Icons.Default.LocationOn,
                    text = warungProfile?.alamatWarung ?: "Alamat Warung",
                    trailingIcon = Icons.Default.Edit,
                    onClick = {
                        showEditAlamatDialog = true
                    }
                )

                var showLogoutDialog by remember { mutableStateOf(false) }

                InfoCard(
                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                    text = "Keluar",
                    trailingIcon = Icons.AutoMirrored.Outlined.ExitToApp,
                    onClick = {
                        showLogoutDialog = true
                    }
                )

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Keluar") },
                        text = { Text("Apakah Anda yakin ingin keluar?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutDialog = false
                                authViewModel.logout()
                                navController.navigate(Screen.LoginScreen.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }) {
                                Text("Ya", color = DormRed)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Batal")
                            }
                        }
                    )
                }

                // Dialog Edit Nama Warung
                if (showEditNameDialog) {
                    EditTextDialog(
                        title = "Edit Nama Warung",
                        initialValue = warungData?.name ?: "",
                        onDismiss = { showEditNameDialog = false },
                        onConfirm = { newName ->
                            warungViewModel.updateName(
                                newName = newName,
                                onSuccess = {
                                    showEditNameDialog = false
                                },
                                onError = { error ->
                                    // Show error toast or message
                                }
                            )
                        }
                    )
                }

                if (showEditAlamatDialog) {
                    EditTextDialog(
                        title = "Edit Alamat Warung",
                        initialValue = warungProfile?.alamatWarung ?: "",
                        onDismiss = { showEditAlamatDialog = false },
                        onConfirm = { newAlamat ->
                            warungViewModel.updateAlamatWarung(
                                newAlamat = newAlamat,
                                onSuccess = {
                                    showEditAlamatDialog = false
                                },
                                onError = {}
                            )
                        }
                    )
                }

                // Dialog Edit No Telepon
                if (showEditPhoneDialog) {
                    EditTextDialog(
                        title = "Edit Nomor Telepon",
                        initialValue = warungData?.phone ?: "",
                        onDismiss = { showEditPhoneDialog = false },
                        onConfirm = { newPhone ->
                            warungViewModel.updatePhone(
                                newPhone = newPhone,
                                onSuccess = {
                                    showEditPhoneDialog = false
                                },
                                onError = { error ->
                                    // Show error toast or message
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditTextDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (textValue.isNotBlank()) {
                        onConfirm(textValue)
                    }
                }
            ) {
                Text("Simpan", color = DormRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun UploadCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, null, tint = DormRed, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title)
        }
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    text: String,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) {
                onClick?.invoke()
            },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DormRed
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                modifier = Modifier.weight(1f)
            )

            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = DormRed
                )
            }
        }
    }
}