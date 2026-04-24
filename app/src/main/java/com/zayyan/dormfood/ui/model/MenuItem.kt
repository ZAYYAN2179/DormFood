package com.zayyan.dormfood.ui.model

data class MenuItem(
    val menuId: String = "",
    val warungId: String = "",
    val namaMenu: String = "",
    val harga: Int = 0,
    val ketersediaan: Boolean = true,
    val tipe: String = "Makanan"
)