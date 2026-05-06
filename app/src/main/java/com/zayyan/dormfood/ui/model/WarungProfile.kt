package com.zayyan.dormfood.ui.model

data class WarungProfile(
    val userId: String = "",
    val layananAntar: Boolean = false,
    val alamatWarung: String = "",
    val fotoMenu: String = "",
    val fotoQr: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)