package com.zayyan.dormfood.ui.model

data class WarungItem(
    val userId: String = "",
    val namaWarung: String = "",
    val alamatWarung: String = "",
    val phone: String = "",
    val fotoMenu: String = "",
    val layananAntar: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)