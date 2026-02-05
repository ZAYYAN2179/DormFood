package com.zayyan.dormfood.ui.navigation

sealed class Screen (val route: String) {
    data object LandingPage: Screen("landingpage")
    data object LoginScreen: Screen("loginscreen")
    data object RegisterScreen: Screen("registerscreen")
    data object BerandaMahasiswaScreen: Screen("berandamahasiswascreen")
    data object PesananMahasiswaScreen: Screen("pesananmahasiswascreen")
    data object ProfilMahasiswaScreen: Screen("profilmahasiswascreen")
    data object BerandaWarungScreen: Screen("berandawarungscreen")
    data object MenuWarungScreen: Screen("menuwarungscreen")
    data object PesananWarungScreen: Screen("pesananwarungscreen")
    data object ProfilWarungScreen: Screen("profilwarungscreen")
    data object UserRoleScreen: Screen("userrolescreen")
    data object SplashScreen: Screen("splashscreen")
}