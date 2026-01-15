package com.zayyan.dormfood.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zayyan.dormfood.ui.screen.LandingPage
import com.zayyan.dormfood.ui.screen.LoginScreen

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
    }
}