package com.zayyan.dormfood

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.zayyan.dormfood.ui.model.MenuItem
import com.zayyan.dormfood.ui.screen.warung.*
import com.zayyan.dormfood.ui.theme.DormFoodTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MenuWarungTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchMenu_bisaInput() {
        composeTestRule.setContent {

            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ) {
                SearchBar(
                    query = "",
                    onQueryChange = {}
                )
            }
        }
        composeTestRule
            .onNodeWithTag("searchMenu")
            .assertIsDisplayed()
            .performTextInput("Es Teh")
    }
    @Test
    fun tabMenu_bisaDiklik(){
        composeTestRule.setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ){

                TabSelector(
                    selectedTab = 0,
                    tabs = listOf(
                        "Semua",
                        "Makanan",
                        "Minuman"
                    ),
                    onTabSelected = {}
                )
            }
        }
        composeTestRule
            .onNodeWithText("Makanan")
            .performClick()
        composeTestRule
            .onNodeWithText("Minuman")
            .performClick()
    }
    @Test
    fun tambahMenu_inputBerhasil(){

        composeTestRule.setContent {

            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ){

                AddMenuDialog(
                    onDismiss = {},
                    onConfirm = {_,_,_->}
                )
            }
        }
        composeTestRule
            .onNodeWithTag("inputNamaMenu")
            .performTextInput("Ayam Geprek")
        composeTestRule
            .onNodeWithTag("inputHargaMenu")
            .performTextInput("15000")
        composeTestRule
            .onNodeWithText("Ayam Geprek")
            .assertExists()
        composeTestRule
            .onNodeWithText("15000")
            .assertExists()

    }
    @Test
    fun tombolSimpan_bisaKlik(){
        composeTestRule.setContent {
            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ){
                AddMenuDialog(
                    onDismiss = {},
                    onConfirm = {_,_,_->}
                )
            }
        }
        composeTestRule
            .onNodeWithTag("inputNamaMenu")
            .performTextInput("Nasi Goreng")
        composeTestRule
            .onNodeWithTag("inputHargaMenu")
            .performTextInput("10000")
        composeTestRule
            .onNodeWithTag("btnSimpanMenu")
            .assertIsEnabled()
            .performClick()
    }
    fun cardMenu_tampil(){

        composeTestRule.setContent {

            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ){

                MenuItemCard(
                    menuItem = MenuItem(
                        menuId = "1",
                        namaMenu = "Es Teh",
                        harga = 5000,
                        tipe = "Minuman",
                        ketersediaan = true
                    ),
                    onToggleAvailability = {},
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
        composeTestRule
            .onNodeWithTag("menuCard")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Es Teh")
            .assertIsDisplayed()
    }
    @Test
    fun switchMenu_bisaKlik(){

        composeTestRule.setContent {

            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ){

                MenuItemCard(
                    menuItem = MenuItem(
                        menuId = "2",
                        namaMenu = "Bakso",
                        harga = 12000,
                        tipe = "Makanan",
                        ketersediaan = true
                    ),
                    onToggleAvailability = {},
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
        composeTestRule
            .onNodeWithTag("switchMenu")
            .performClick()
    }
    @Test
    fun moreMenu_bisaKlik(){

        composeTestRule.setContent {

            DormFoodTheme(
                darkTheme = false,
                dynamicColor = false
            ){

                MenuItemCard(
                    menuItem = MenuItem(
                        menuId = "3",
                        namaMenu = "Bakso",
                        harga = 12000,
                        tipe = "Makanan",
                        ketersediaan = true
                    ),
                    onToggleAvailability = {},
                    onEditClick = {},
                    onDeleteClick = {}
                )

            }
        }
        composeTestRule
            .onNodeWithTag("btnMoreMenu")
            .performClick()
        composeTestRule
            .onNodeWithText("Edit")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Hapus")
            .assertIsDisplayed()
    }
}