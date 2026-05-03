package com.zayyan.dormfood

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zayyan.dormfood.ui.model.WarungItem
import com.zayyan.dormfood.ui.screen.mahasiswa.BerandaMahasiswaContent
import com.zayyan.dormfood.ui.viewmodel.WarungViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BerandaMahasiswaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: NavHostController
    private lateinit var warungViewModel: WarungViewModel

    @Before
    fun setup() {
        composeTestRule.setContent {

            navController = rememberNavController()

            warungViewModel = mockk()

            every { warungViewModel.warungList } returns MutableStateFlow(
                listOf(
                    WarungItem(
                        userId = "1",
                        namaWarung = "Warung Ayam",
                        alamatWarung = "Jl. Test",
                        phone = "08123456789",
                        fotoMenu = "",
                        layananAntar = true
                    )
                )
            )

            every { warungViewModel.isLoadingList } returns MutableStateFlow(false)

            BerandaMahasiswaContent(
                paddingValues = PaddingValues(),
                navController = navController,
                warungViewModel = warungViewModel
            )
        }
    }

    @Test
    fun beranda_daftarWarungTampil() {
        composeTestRule.onNodeWithText("Daftar Warung")
            .assertIsDisplayed()
    }

    @Test
    fun beranda_searchBarBisaDigunakan() {
        composeTestRule.onNodeWithTag("searchWarung")
            .assertIsDisplayed()
            .performTextInput("Ayam")

        composeTestRule.onNodeWithTag("searchWarung")
            .assertTextContains("Ayam")
    }

    @Test
    fun beranda_filterBisaDiklik() {
        composeTestRule.onNodeWithTag("filterBuka")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.onNodeWithTag("filterTutup")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun beranda_cardWarungBisaDiklik() {

        composeTestRule.waitUntil {
            composeTestRule.onAllNodesWithTag("warungCard")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithTag("warungCard")
            .onFirst()
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun beranda_tombolWhatsappBisaDiklik() {

        composeTestRule.waitUntil {
            composeTestRule.onAllNodesWithTag("tombolWhatsapp")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithTag("tombolWhatsapp")
            .onFirst()
            .assertIsDisplayed()
            .performClick()
    }
}