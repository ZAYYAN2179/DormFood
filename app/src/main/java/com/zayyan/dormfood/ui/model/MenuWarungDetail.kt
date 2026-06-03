package com.zayyan.dormfood.ui.model

data class MenuWarungDetail(
    val namaWarung: String = "",
    val alamatWarung: String = "",
    val phone: String = "",
    val fotoMenu: String = "",
    val fotoQr: String = "",
    val layananAntar: Boolean = false,
    val ongkirAktif: Boolean = false,
    val ongkir: Int = 0
)