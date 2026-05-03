package com.zayyan.dormfood

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
import com.zayyan.dormfood.ui.screen.mahasiswa.RegisterScreen
import com.zayyan.dormfood.ui.theme.DormFoodTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent {
            DormFoodTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.RegisterScreen.route
                ) {
                    composable(Screen.RegisterScreen.route) {
                        RegisterScreen(navController = navController)
                    }
                    composable(Screen.LoginScreen.route) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Text("Login Screen")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun registerScreen_komponenTampilDenganBenar() {
        composeTestRule.onNodeWithText("Buat Akun Baru").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nama").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gedung").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nomor kamar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Kata Sandi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Konfirmasi Kata Sandi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daftar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sudahPunyaAkun")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Masuk")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun registerScreen_inputSemuaField() {
        composeTestRule.onNodeWithTag("inputNama")
            .performTextInput("Zayyan")
        composeTestRule.onNodeWithTag("inputGedung")
            .performTextInput("Gedung A")
        composeTestRule.onNodeWithTag("inputNomorKamar")
            .performTextInput("101")
        composeTestRule.onNodeWithTag("inputEmailRegister")
            .performTextInput("zayyan@email.com")
        composeTestRule.onNodeWithTag("inputPasswordRegister")
            .performTextInput("password123")
        composeTestRule.onNodeWithTag("inputKonfirmasiPassword")
            .performTextInput("password123")
        composeTestRule.onNodeWithTag("inputNama")
            .assertTextContains("Zayyan")
        composeTestRule.onNodeWithTag("inputEmailRegister")
            .assertTextContains("zayyan@email.com")
    }

    @Test
    fun registerScreen_tombolDaftarBisaDiklik() {
        composeTestRule.onNodeWithTag("inputNama")
            .performTextInput("Zayyan")
        composeTestRule.onNodeWithTag("inputGedung")
            .performTextInput("Gedung A")
        composeTestRule.onNodeWithTag("inputNomorKamar")
            .performTextInput("101")
        composeTestRule.onNodeWithTag("inputEmailRegister")
            .performTextInput("zayyan@email.com")
        composeTestRule.onNodeWithTag("inputPasswordRegister")
            .performTextInput("password123")
        composeTestRule.onNodeWithTag("inputKonfirmasiPassword")
            .performTextInput("password123")
        composeTestRule.onNodeWithTag("tombolDaftar").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun registerScreen_tombolDaftarTampilDanEnabled() {
        composeTestRule.onNodeWithTag("tombolDaftar")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun registerScreen_klikMasukPindahKeLogin() {
        composeTestRule.onNodeWithText("Masuk").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Login Screen").assertIsDisplayed()
    }
}