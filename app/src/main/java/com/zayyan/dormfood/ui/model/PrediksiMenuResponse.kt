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