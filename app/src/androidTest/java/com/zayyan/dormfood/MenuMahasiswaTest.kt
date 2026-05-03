package com.zayyan.dormfood

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zayyan.dormfood.ui.model.*
import com.zayyan.dormfood.ui.screen.mahasiswa.MenuMahasiswaContent
import com.zayyan.dormfood.ui.viewmodel.MahasiswaViewModel
import com.zayyan.dormfood.ui.viewmodel.MenuViewModel
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel
import com.zayyan.dormfood.ui.viewmodel.RatingViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuMahasiswaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        composeTestRule.setContent {

            val navController = mockk<NavHostController>(relaxed = true)

            val detail = MenuWarungDetail(
                namaWarung = "Warung Test",
                fotoMenu = "https://test.com/image.jpg",
                phone = "08123456789",
                fotoQr = "",
            )

            val menuViewModel = mockk<MenuViewModel>()
            val pesananViewModel = mockk<PesananViewModel>(relaxed = true)
            val mahasiswaViewModel = mockk<MahasiswaViewModel>()
            val ratingViewModel = mockk<RatingViewModel>(relaxed = true)

            every { menuViewModel.menuListByWarung } returns MutableStateFlow(
                listOf(
                    MenuItem(
                        namaMenu = "Ayam",
                        harga = 10000,
                        tipe = "makanan",
                        ketersediaan = true
                    )
                )
            )

            every { mahasiswaViewModel.mahasiswaData } returns MutableStateFlow(null)
            every { ratingViewModel.ratingSummaryMap } returns MutableStateFlow(emptyMap())

            MenuMahasiswaContent(
                paddingValues = PaddingValues(),
                detail = detail,
                menuViewModel = menuViewModel,
                navController = navController,
                pesananViewModel = pesananViewModel,
                mahasiswaViewModel = mahasiswaViewModel,
                userId = "1",
                ratingViewModel = ratingViewModel
            )
        }
    }

    @Test
    fun menu_previewBisaDiklik() {
        composeTestRule.onNodeWithTag("previewMenu")
            .assertExists()
            .performClick()
    }

    @Test
    fun menu_tombolUlasanBisaDiklik() {
        composeTestRule.onNodeWithTag("btnUlasan")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun menu_tombolWABisaDiklik() {
        composeTestRule.onNodeWithTag("btnWhatsapp")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun menu_quantityBerfungsi() {

        val tambah = composeTestRule.onNodeWithTag("btnTambah")
        val kurang = composeTestRule.onNodeWithTag("btnKurang")
        val qty = composeTestRule.onNodeWithTag("textQuantity")

        tambah.performClick()
        qty.assertTextEquals("1")

        tambah.performClick()
        qty.assertTextEquals("2")

        kurang.performClick()
        qty.assertTextEquals("1")
    }
}