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
    data object MenuMahasiswaScreen : Screen("menumahasiswascreen/{userId}") {
        fun createRoute(userId: String) = "menumahasiswascreen/$userId"
    }
    data object PembayaranMahasiswaScreen: Screen("pembayaranmahasiswascreen")
    data object RatingUlasanMahasiswaScreen : Screen(
        "ratingulasanmahasiswascreen/{orderId}/{warungId}/{namaWarung}"
    ) {
        fun createRoute(
            orderId: String,
            warungId: String,
            namaWarung: String
        ): String {
            val encodedNama = java.net.URLEncoder.encode(namaWarung, "UTF-8")
            return "ratingulasanmahasiswascreen/$orderId/$warungId/$encodedNama"
        }
    }
    data object RatingUlasanWarungScreen : Screen(
        "ratingulasanwarungscreen/{warungId}"
    ) {
        fun createRoute(warungId: String) = "ratingulasanwarungscreen/$warungId"
    }
}