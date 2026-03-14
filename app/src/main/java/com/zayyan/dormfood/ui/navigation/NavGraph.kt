package com.zayyan.dormfood.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zayyan.dormfood.ui.model.MahasiswaViewModel
import com.zayyan.dormfood.ui.model.MenuViewModel
import com.zayyan.dormfood.ui.model.PesananViewModel
import com.zayyan.dormfood.ui.model.WarungViewModel
import com.zayyan.dormfood.ui.screen.LandingPage
import com.zayyan.dormfood.ui.screen.LoginScreen
import com.zayyan.dormfood.ui.screen.SplashScreen
import com.zayyan.dormfood.ui.screen.UserRoleScreen
import com.zayyan.dormfood.ui.screen.mahasiswa.BerandaMahasiswa
import com.zayyan.dormfood.ui.screen.mahasiswa.MenuMahasiswa
import com.zayyan.dormfood.ui.screen.mahasiswa.PembayaranMahasiswaScreen
import com.zayyan.dormfood.ui.screen.mahasiswa.PesananMahasiswa
import com.zayyan.dormfood.ui.screen.mahasiswa.ProfilMahasiswa
import com.zayyan.dormfood.ui.screen.mahasiswa.RegisterScreen
import com.zayyan.dormfood.ui.screen.warung.BerandaWarung
import com.zayyan.dormfood.ui.screen.warung.MenuWarung
import com.zayyan.dormfood.ui.screen.warung.PesananWarung
import com.zayyan.dormfood.ui.screen.warung.ProfilWarung

@Composable
fun SetupNavGraph(navController: NavHostController = rememberNavController()) {
    val pesananViewModel: PesananViewModel = viewModel()
    val warungViewModel: WarungViewModel = viewModel()
    val mahasiswaViewModel: MahasiswaViewModel = viewModel()
    val menuViewModel: MenuViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {
        composable(route = Screen.LandingPage.route) {
            LandingPage(navController)
        }
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(
                navController = navController,
                pesananViewModel = pesananViewModel
            )
        }
        composable(route = Screen.RegisterScreen.route) {
            RegisterScreen(navController)
        }
        composable(route = Screen.BerandaMahasiswaScreen.route) {
            BerandaMahasiswa(
                navController = navController,
                warungViewModel = warungViewModel
            )
        }
        composable(route = Screen.PesananMahasiswaScreen.route) {
            PesananMahasiswa(
                navController = navController,
                pesananViewModel = pesananViewModel
            )
        }
        composable(route = Screen.ProfilMahasiswaScreen.route) {
            ProfilMahasiswa(
                navController = navController,
                mahasiswaViewModel = mahasiswaViewModel
            )
        }
        composable(route = Screen.BerandaWarungScreen.route) {
            BerandaWarung(navController)
        }
        composable(route = Screen.MenuWarungScreen.route) {
            MenuWarung(navController)
        }
        composable(route = Screen.PesananWarungScreen.route) {
            PesananWarung(navController)
        }
        composable(route = Screen.ProfilWarungScreen.route) {
            ProfilWarung(
                navController = navController,
                warungViewModel = warungViewModel
            )
        }
        composable(route = Screen.UserRoleScreen.route) {
            UserRoleScreen(navController)
        }
        composable(route = Screen.SplashScreen.route) {
            SplashScreen(navController)
        }
        composable(
            route = Screen.MenuMahasiswaScreen.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MenuMahasiswa(
                navController = navController,
                userId = userId,
                menuViewModel = menuViewModel,
                pesananViewModel = pesananViewModel
            )
        }
        composable(route = Screen.PembayaranMahasiswaScreen.route) {
            PembayaranMahasiswaScreen(
                navController = navController,
                pesananViewModel = pesananViewModel
            )
        }
    }
}