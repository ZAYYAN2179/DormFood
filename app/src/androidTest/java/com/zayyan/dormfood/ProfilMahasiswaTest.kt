package com.zayyan.dormfood

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zayyan.dormfood.ui.model.MahasiswaData
import com.zayyan.dormfood.ui.screen.mahasiswa.ProfilMahasiswaContent
import com.zayyan.dormfood.ui.viewmodel.AuthViewModel
import com.zayyan.dormfood.ui.viewmodel.MahasiswaViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilMahasiswaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent {

            val navController = mockk<NavHostController>(relaxed = true)
            val authViewModel = mockk<AuthViewModel>(relaxed = true)
            val mahasiswaViewModel = mockk<MahasiswaViewModel>(relaxed = true)

            every { mahasiswaViewModel.mahasiswaData } returns MutableStateFlow(
                MahasiswaData(
                    uid = "1",
                    name = "Zayyan",
                    email = "test@email.com",
                    building = "1",
                    roomNumber = "101",
                    role = "mahasiswa"
                )
            )

            every { mahasiswaViewModel.isLoading } returns MutableStateFlow(false)

            ProfilMahasiswaContent(
                paddingValues = PaddingValues(),
                navController = navController,
                authViewModel = authViewModel,
                mahasiswaViewModel = mahasiswaViewModel,
                isLoading = false
            )
        }
    }

    @Test
    fun profil_editNamaBisaDiklik() {
        composeTestRule.onNodeWithTag("editNama")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun profil_editGedungBisaDiklik() {
        composeTestRule.onNodeWithTag("editGedung")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun profil_editKamarBisaDiklik() {
        composeTestRule.onNodeWithTag("editKamar")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun profil_logoutBerfungsi() {

        composeTestRule.onNodeWithTag("btnLogout")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithText("Ya, Keluar")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Ya, Keluar")
            .performClick()
    }
}