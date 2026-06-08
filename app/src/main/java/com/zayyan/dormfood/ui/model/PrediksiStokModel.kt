package com.zayyan.dormfood.ui.model

data class PrediksiMenuResponse(
    val warungId: String = "",
    val status: String = "",
    val isDummy: Boolean = false,
    val totalMenu: Int = 0,
    val menuSiapPrediksi: Int = 0,
    val menuDataKurang: Int = 0,
    val hasil: List<HasilPrediksi> = emptyList()
)

data class HasilPrediksi(
    val menu: String = "",
    val prediksi: Int? = null,
    val movingAverage7: Int? = null,
    val status: String = "",     
    val dataTersedia: Int = 0,
    val dataMinimum: Int = 14,
    val rataHarian: Float = 0f
)
