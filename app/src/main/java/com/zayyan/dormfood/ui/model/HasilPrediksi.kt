package com.zayyan.dormfood.ui.model

data class HasilPrediksi(
    val menu: String = "",
    val prediksi: Int? = null,
    val movingAverage7: Int? = null,
    val status: String = "",
    val dataTersedia: Int = 0,
    val dataMinimum: Int = 14,
    val rataHarian: Float = 0f
)