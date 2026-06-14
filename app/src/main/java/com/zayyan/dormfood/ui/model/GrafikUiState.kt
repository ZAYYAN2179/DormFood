package com.zayyan.dormfood.ui.model

sealed class GrafikUiState {
    object Idle : GrafikUiState()
    object Loading : GrafikUiState()
    data class Success(val data: List<DataGrafikMingguan>) : GrafikUiState()
    data class Error(val message: String) : GrafikUiState()
}