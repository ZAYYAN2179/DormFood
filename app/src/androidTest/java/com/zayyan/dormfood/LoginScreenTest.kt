package com.zayyan.dormfood

import com.zayyan.dormfood.ui.screen.LoginScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.DormFoodTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent {
            DormFoodTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(navController = navController)
                    }
                    composable(Screen.RegisterScreen.route) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Daftar Akun")
                        }
                    }
                    composable(Screen.LandingPage.route) {
                        Text("Landing Page")
                    }
                }
            }
        }
    }

    @Test
    fun loginScreen_komponenTampilDenganBenar() {
        composeTestRule.onNodeWithText("Selamat Datang Kembali").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kata Sandi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Masuk").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daftar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lupa Kata Sandi?").assertIsDisplayed()
    }

    @Test
    fun loginScreen_inputEmailDanPassword() {
        composeTestRule.onNodeWithTag("inputEmail")
            .performTextInput("test@email.com")

        composeTestRule.onNodeWithTag("inputPassword")
            .performTextInput("password123")

        composeTestRule.onNodeWithTag("inputEmail")
            .assertTextContains("test@email.com")
    }

    @Test
    fun loginScreen_tombolMasukBisaDiklik() {
        composeTestRule.onNodeWithTag("inputEmail")
            .performTextInput("test@email.com")

        composeTestRule.onNodeWithTag("inputPassword")
            .performTextInput("password123")

        composeTestRule.onNodeWithTag("tombolMasuk")
            .performClick()

        composeTestRule.waitForIdle()
    }

    @Test
    fun loginScreen_klikDaftarPindahKeRegister() {
        composeTestRule.onNodeWithText("Daftar").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun loginScreen_klikLupaPasswordPindahKeLanding() {
        composeTestRule.onNodeWithTag("btnForgotPassword")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Landing Page")
            .assertIsDisplayed()
    }
}