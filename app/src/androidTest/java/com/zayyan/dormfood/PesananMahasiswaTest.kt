package com.zayyan.dormfood

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Timestamp
import com.zayyan.dormfood.ui.model.ItemPesanan
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.screen.mahasiswa.OrderCard
import com.zayyan.dormfood.ui.theme.DormFoodTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PesananMahasiswaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dummyOrder = OrderItem(
        orderId = "1",
        userId = "user1",
        warungId = "warung1",
        namaWarung = "Warung Pak Budi",
        namaUser = "Budi",
        building = "Gedung A",
        roomNumber = "101",
        items = listOf(
            ItemPesanan(
                namaMenu = "Nasi Goreng",
                harga = 10000,
                quantity = 2
            )
        ),
        subtotal = 20000,
        ongkir = 3000,
        totalHarga = 23000,
        statusPesanan = "Menunggu Konfirmasi",
        createdAt = Timestamp.now()
    )

    private val dummyOrderSelesai = dummyOrder.copy(
        statusPesanan = "Selesai"
    )

    @Test
    fun informasiPesanan_tampilDenganBenar() {

        composeTestRule.setContent {
            DormFoodTheme {
                OrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    showReviewButton = false,
                    navController = mockk(relaxed = true),
                    ratingViewModel = mockk(relaxed = true)
                )
            }
        }

        composeTestRule
            .onNodeWithText("Warung Pak Budi")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Menunggu Konfirmasi")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Total Harga")
            .assertIsDisplayed()
    }

    @Test
    fun detailMenuPesanan_tampilDenganBenar() {

        composeTestRule.setContent {
            DormFoodTheme {
                OrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    showReviewButton = false,
                    navController = mockk(relaxed = true),
                    ratingViewModel = mockk(relaxed = true)
                )
            }
        }

        composeTestRule
            .onNodeWithText("2x  Nasi Goreng")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Rp 20000")
            .assertIsDisplayed()
    }

    @Test
    fun totalHargaPesanan_tampilDenganBenar() {

        composeTestRule.setContent {
            DormFoodTheme {
                OrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    showReviewButton = false,
                    navController = mockk(relaxed = true),
                    ratingViewModel = mockk(relaxed = true)
                )
            }
        }

        composeTestRule
            .onNodeWithText("Rp 23000")
            .assertIsDisplayed()
    }

    @Test
    fun pesananSelesai_menampilkanTombolPesanLagi() {

        composeTestRule.setContent {
            DormFoodTheme {
                OrderCard(
                    order = dummyOrderSelesai,
                    formatRupiah = { "Rp $it" },
                    showReviewButton = true,
                    navController = mockk(relaxed = true),
                    ratingViewModel = mockk(relaxed = true)
                )
            }
        }

        composeTestRule
            .onNodeWithText("Pesan Lagi")
            .assertIsDisplayed()
    }
}