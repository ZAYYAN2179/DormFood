package com.zayyan.dormfood

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.screen.warung.ProfilWarung
import com.zayyan.dormfood.ui.theme.DormFoodTheme
import com.zayyan.dormfood.ui.viewmodel.AuthViewModel
import com.zayyan.dormfood.ui.viewmodel.WarungViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import com.zayyan.dormfood.ui.model.WarungData
import com.zayyan.dormfood.ui.model.WarungProfile
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilWarungScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dummyWarungData = WarungData(
        uid = "uid123",
        name = "Warung Pak Budi",
        email = "pakbudi@email.com",
        phone = "081234567890",
        role = "warung"
    )

    private val dummyWarungProfile = WarungProfile(
        userId = "uid123",
        layananAntar = true,
        ongkirAktif = true,
        ongkir = 3000,
        alamatWarung = "Gedung A Lt.1",
        fotoMenu = "",
        fotoQr = "",
        latitude = -6.9731,
        longitude = 107.6299
    )

    private lateinit var warungViewModel: WarungViewModel
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        warungViewModel = mockk(relaxed = true)
        authViewModel = mockk(relaxed = true)

        every { warungViewModel.warungData } returns MutableStateFlow(dummyWarungData)
        every { warungViewModel.warungProfile } returns MutableStateFlow(dummyWarungProfile)
        every { warungViewModel.isLoading } returns MutableStateFlow(false)
        every { warungViewModel.updateMessage } returns MutableStateFlow(null)

        composeTestRule.setContent {
            DormFoodTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.ProfilWarungScreen.route
                ) {
                    composable(Screen.ProfilWarungScreen.route) {
                        ProfilWarung(
                            navController = navController,
                            authViewModel = authViewModel,
                            warungViewModel = warungViewModel
                        )
                    }
                    composable(Screen.PetaWarungScreen.route) {
                        androidx.compose.material3.Text("Peta Warung")
                    }
                    composable(Screen.LoginScreen.route) {
                        androidx.compose.material3.Text("Login Screen")
                    }
                }
            }
        }
    }

    /**
     * TC-01: Bagian Nama Warung
     * Memastikan nama dan email warung tampil, lalu klik edit nama membuka dialog
     * dengan nilai awal terisi dan validasi input kosong berjalan.
     */
    @Test
    fun namaWarung_tampilDanEditNamaBerfungsi() {
        // Nama dan email tampil di header
        composeTestRule.onNodeWithText("Warung Pak Budi").assertIsDisplayed()
        composeTestRule.onNodeWithText("pakbudi@email.com").assertIsDisplayed()

        // Klik tombol edit nama → dialog terbuka dengan nilai awal terisi
        composeTestRule.onNodeWithContentDescription("Edit Nama").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Edit Nama Warung").assertIsDisplayed()
        // Gunakan hasSetTextAction() untuk menarget text field secara spesifik,
        // menghindari ambiguitas dengan teks yang sama di header card
        composeTestRule.onNode(hasSetTextAction(), useUnmergedTree = true).assertIsDisplayed()

        // Kosongkan field lalu simpan → validasi error muncul
        composeTestRule.onNode(hasSetTextAction(), useUnmergedTree = true).performTextClearance()
        composeTestRule.onNodeWithText("Simpan", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Field tidak boleh kosong", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    /**
     * TC-02: Bagian Media Warung
     * Memastikan kartu Daftar Menu dan QR Pembayaran tampil saat belum ada gambar.
     */
    @Test
    fun mediaWarung_daftarMenuDanQrTampil() {
        composeTestRule.onNodeWithText("Daftar Menu").assertIsDisplayed()
        composeTestRule.onNodeWithText("QR Pembayaran").assertIsDisplayed()

        // Kedua kartu juga menampilkan teks ajakan upload karena imageUrl kosong
        composeTestRule.onAllNodesWithText("Tap untuk upload")
            .assertCountEquals(2)
    }

    /**
     * TC-03: Informasi Warung – Nomor Telepon & Alamat
     * Memastikan data tampil dan dialog edit masing-masing dapat dibuka serta ditutup.
     */
    @Test
    fun informasiWarung_teleponDanAlamatTampilDanBisaDiedit() {
        // Data tampil
        composeTestRule.onNodeWithText("Nomor Telepon").assertIsDisplayed()
        composeTestRule.onNodeWithText("081234567890").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alamat Warung").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gedung A Lt.1").assertIsDisplayed()

        // Edit nomor telepon: index 0 di antara semua ikon "Edit"
        composeTestRule.onAllNodesWithContentDescription("Edit")[0].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit Nomor Telepon").assertIsDisplayed()
        composeTestRule.onNodeWithText("Batal").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit Nomor Telepon").assertDoesNotExist()

        // Edit alamat: index 1
        composeTestRule.onAllNodesWithContentDescription("Edit")[1].performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit Alamat Warung").assertIsDisplayed()
        composeTestRule.onNodeWithText("Batal").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Edit Alamat Warung").assertDoesNotExist()
    }

    /**
     * TC-04: Informasi Warung – Biaya Pengantaran
     * Memastikan nilai ongkir tampil dan validasi nominal kosong saat ongkir aktif berjalan.
     */
    @Test
    fun informasiWarung_biayaPengantaranTampilDanValidasiNominalKosong() {
        // Nilai ongkir tampil
        composeTestRule.onNodeWithText("Biaya Pengantaran").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rp 3000").assertIsDisplayed()

        // Buka dialog edit ongkir (index 2)
        composeTestRule.onAllNodesWithContentDescription("Edit")[2].performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Aktifkan Ongkir", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Nominal Ongkir", useUnmergedTree = true).assertIsDisplayed()

        // Kosongkan nominal lalu simpan → error validasi muncul
        composeTestRule.onNode(hasSetTextAction()).performTextClearance()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Simpan").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Nominal ongkir wajib diisi").assertIsDisplayed()
    }

    /**
     * TC-05: Informasi Warung – Titik Lokasi Peta
     * Memastikan koordinat tampil dan klik kartu navigasi ke layar peta.
     */
    @Test
    fun informasiWarung_titikLokasiTampilDanNavigasiKePeta() {
        composeTestRule.onNodeWithText("Titik Lokasi Peta").assertIsDisplayed()
        composeTestRule.onNodeWithText("-6.97310, 107.62990").assertIsDisplayed()

        // Klik kartu lokasi → navigasi ke PetaWarungScreen
        composeTestRule.onNodeWithText("Titik Lokasi Peta").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Peta Warung").assertIsDisplayed()
    }
}