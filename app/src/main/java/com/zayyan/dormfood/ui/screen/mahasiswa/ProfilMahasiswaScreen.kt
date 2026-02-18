package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.component.BottomBarMahasiswa
import com.zayyan.dormfood.ui.component.TopAppBar
import com.zayyan.dormfood.ui.model.AuthViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.screen.warung.AlertDialog
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed

@Composable
fun ProfilMahasiswa(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(title = "Profil Mahasiswa")
        },
        bottomBar = {
            BottomBarMahasiswa(
                navController = navController,
                currentRoute = currentRoute
            )
        },
        containerColor = Background
    ) { paddingValues ->
        ProfilMahasiswaContent(
            paddingValues = paddingValues,
            navController = navController,
            authViewModel = authViewModel
        )
    }
}

@Composable
fun ProfilMahasiswaContent(
    paddingValues: PaddingValues,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {

        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = DormRed
            ),
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
    }

    // ===== DIALOG LOGOUT =====
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

                // logout firebase/auth
                authViewModel.logout()

                // pindah ke login & clear backstack
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
