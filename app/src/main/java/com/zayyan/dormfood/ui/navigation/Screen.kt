package com.zayyan.dormfood.ui.navigation

sealed class Screen (val route: String) {
    data object LandingPage: Screen("landingpage")
}