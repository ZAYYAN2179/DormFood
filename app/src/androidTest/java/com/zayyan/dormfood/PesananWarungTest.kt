package com.zayyan.dormfood

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zayyan.dormfood.ui.model.ItemPesanan
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.model.StatusPesanan
import com.zayyan.dormfood.ui.screen.warung.WarungOrderCard
import com.zayyan.dormfood.ui.theme.DormFoodTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PesananWarungTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dummyOrder = OrderItem(
        orderId = "1",
        userId = "user1",
        warungId = "warung1",
        namaWarung = "Warung Pak Budi",
        namaUser = "Budi",
        building = "A",
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
        statusPesanan = StatusPesanan.MENUNGGU_KONFIRMASI.label,
        buktiPembayaranUrl = "https://example.com/bukti.jpg"
    )

    @Test
    fun pesanan_tampilDenganBenar() {

        composeTestRule.setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                WarungOrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    onStatusChange = {},
                    onLihatBukti = {},
                    onBatalkan = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Budi")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Menunggu Konfirmasi")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Total Harga")
            .assertIsDisplayed()
    }

    @Test
    fun detailPesanan_tampilDenganBenar() {

        composeTestRule.setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                WarungOrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    onStatusChange = {},
                    onLihatBukti = {},
                    onBatalkan = {}
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
    fun tombolKonfirmasiPesanan_menampilkanDialog() {

        composeTestRule.setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                WarungOrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    onStatusChange = {},
                    onLihatBukti = {},
                    onBatalkan = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Konfirmasi Pesanan")
            .performClick()

        composeTestRule
            .onNodeWithText("Ubah Status Pesanan")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Ya, Ubah")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Batal")
            .assertIsDisplayed()
    }

    @Test
    fun konfirmasiPesanan_memanggilCallbackStatusChange() {

        var callbackCalled = false

        composeTestRule.setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                WarungOrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    onStatusChange = {
                        callbackCalled = true
                    },
                    onLihatBukti = {},
                    onBatalkan = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Konfirmasi Pesanan")
            .performClick()

        composeTestRule
            .onNodeWithText("Ya, Ubah")
            .performClick()

        assertTrue(callbackCalled)
    }

    @Test
    fun tombolTolak_menampilkanDialogKonfirmasi() {

        composeTestRule.setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                WarungOrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    onStatusChange = {},
                    onLihatBukti = {},
                    onBatalkan = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Tolak")
            .performClick()

        composeTestRule
            .onNodeWithText("Batalkan Pesanan")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Ya, Batalkan")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Tidak")
            .assertIsDisplayed()
    }

    @Test
    fun tombolBuktiPembayaran_bisaDiklik() {

        var callbackCalled = false

        composeTestRule.setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                WarungOrderCard(
                    order = dummyOrder,
                    formatRupiah = { "Rp $it" },
                    onStatusChange = {},
                    onLihatBukti = {
                        callbackCalled = true
                    },
                    onBatalkan = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Bukti Bayar")
            .performClick()

        assertTrue(callbackCalled)
    }
}