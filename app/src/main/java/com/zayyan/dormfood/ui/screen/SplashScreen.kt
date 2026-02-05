package com.zayyan.dormfood.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.model.AuthViewModel
import com.zayyan.dormfood.ui.navigation.Screen

@Composable
fun SplashScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {

    LaunchedEffect(Unit) {

        if (authViewModel.isLoggedIn()) {

            authViewModel.checkUserRole { role ->
                when (role) {

                    "mahasiswa" -> {
                        navController.navigate(Screen.BerandaMahasiswaScreen.route) {
                            popUpTo(0)
                        }
                    }

                    "warung" -> {
                        navController.navigate(Screen.BerandaWarungScreen.route) {
                            popUpTo(0)
                        }
                    }

                    else -> {
                        navController.navigate(Screen.LandingPage.route) {
                            popUpTo(0)
                        }
                    }
                }
            }

        } else {
            navController.navigate(Screen.LandingPage.route) {
                popUpTo(0)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
