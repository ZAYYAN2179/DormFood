package com.zayyan.dormfood.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zayyan.dormfood.ui.screen.LandingPage
import com.zayyan.dormfood.ui.screen.LoginScreen
import com.zayyan.dormfood.ui.screen.mahasiswa.BerandaMahasiswa
import com.zayyan.dormfood.ui.screen.mahasiswa.RegisterScreen

@Composable
fun SetupNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.LandingPage.route
    ) {
        composable(route = Screen.LandingPage.route) {
            LandingPage(navController)
        }
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(route = Screen.RegisterScreen.route) {
            RegisterScreen(navController)
        }
        composable(route = Screen.BerandaMahasiswaScreen.route) {
            BerandaMahasiswa()
        }
    }
}