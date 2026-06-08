package com.zayyan.dormfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zayyan.dormfood.ui.model.PrediksiMenuResponse
import com.zayyan.dormfood.ui.utils.PrediksiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// PrediksiStokViewModel.kt
class PrediksiStokViewModel : ViewModel() {

    private val _prediksiState = MutableStateFlow<PrediksiUiState>(PrediksiUiState.Idle)
    val prediksiState: StateFlow<PrediksiUiState> = _prediksiState

    // Logika utama: cukup data → real, kurang → dummy + warning
    fun loadPrediksi(warungId: String) {
        viewModelScope.launch {
            _prediksiState.value = PrediksiUiState.Loading
            android.util.Log.d("PrediksiVM", "Loading prediksi for warungId: $warungId")

            val result = PrediksiRepository.getPrediksi(warungId)
            result.fold(
                onSuccess = { response ->
                    android.util.Log.d("PrediksiVM", "Success: status=${response.status}, total=${response.totalMenu}")
                    when {
                        response.status == "NO_DATA" -> {
                            val dummy = PrediksiRepository.getPrediksiDummy(warungId)
                            _prediksiState.value = PrediksiUiState.Success(
                                data = dummy.getOrNull() ?: response,
                                warningLevel = WarningLevel.NO_DATA
                            )
                        }
                        response.menuSiapPrediksi == 0 -> {
                            val dummy = PrediksiRepository.getPrediksiDummy(warungId)
                            _prediksiState.value = PrediksiUiState.Success(
                                data = dummy.getOrNull() ?: response,
                                warningLevel = WarningLevel.SEMUA_KURANG
                            )
                        }
                        response.menuDataKurang > 0 -> {
                            _prediksiState.value = PrediksiUiState.Success(
                                data = response,
                                warningLevel = WarningLevel.SEBAGIAN_KURANG
                            )
                        }
                        else -> {
                            _prediksiState.value = PrediksiUiState.Success(
                                data = response,
                                warningLevel = WarningLevel.NONE
                            )
                        }
                    }
                },
                onFailure = { e ->
                    android.util.Log.e("PrediksiVM", "FAILED: ${e.javaClass.simpleName}: ${e.message}")
                    _prediksiState.value = PrediksiUiState.Error(
                        "${e.javaClass.simpleName}: ${e.message}"
                    )
                }
            )
        }
    }
}

sealed class PrediksiUiState {
    object Idle : PrediksiUiState()
    object Loading : PrediksiUiState()
    data class Success(
        val data: PrediksiMenuResponse,
        val warningLevel: WarningLevel
    ) : PrediksiUiState()
    data class Error(val message: String) : PrediksiUiState()
}

enum class WarningLevel {
    NONE,           // data cukup semua
    SEBAGIAN_KURANG,// sebagian menu belum cukup data
    SEMUA_KURANG,   // semua menu pakai dummy
    NO_DATA         // belum ada transaksi sama sekali
}