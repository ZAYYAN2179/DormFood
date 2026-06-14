package com.zayyan.dormfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zayyan.dormfood.ui.model.GrafikUiState
import com.zayyan.dormfood.ui.model.PrediksiMenuResponse
import com.zayyan.dormfood.ui.repository.IPrediksiRepository
import com.zayyan.dormfood.ui.repository.PrediksiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PrediksiStokViewModel(
    private val repository: IPrediksiRepository = PrediksiRepository
) : ViewModel() {

    private val _prediksiState = MutableStateFlow<PrediksiUiState>(PrediksiUiState.Idle)
    val prediksiState: StateFlow<PrediksiUiState> = _prediksiState

    private val _grafikState = MutableStateFlow<GrafikUiState>(GrafikUiState.Idle)
    val grafikState: StateFlow<GrafikUiState> = _grafikState

    private val _availableMenus = MutableStateFlow<List<String>>(emptyList())
    val availableMenus: StateFlow<List<String>> = _availableMenus

    fun loadGrafik(warungId: String, tahun: Int, bulan: Int, namaMenu: String) {
        viewModelScope.launch {
            _grafikState.value = GrafikUiState.Loading

            val result = repository.getGrafikMingguan(warungId, tahun, bulan, namaMenu)
            result.fold(
                onSuccess = { data ->
                    _grafikState.value = GrafikUiState.Success(data)
                },
                onFailure = { e ->
                    _grafikState.value = GrafikUiState.Error(e.message ?: "Gagal memuat grafik")
                }
            )
        }
    }

    fun loadPrediksi(warungId: String) {
        viewModelScope.launch {
            _prediksiState.value = PrediksiUiState.Loading

            val result = repository.getPrediksi(warungId)
            result.fold(
                onSuccess = { response ->
                    when {
                        response.status == "NO_DATA" -> {
                            val dummy = repository.getPrediksiDummy(warungId)
                            _prediksiState.value = PrediksiUiState.Success(
                                data = dummy.getOrNull() ?: response,
                                warningLevel = WarningLevel.NO_DATA
                            )
                        }

                        response.menuSiapPrediksi == 0 -> {
                            val dummy = repository.getPrediksiDummy(warungId)
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
                    loadAvailableMenus(warungId)
                },
                onFailure = { e ->
                    _prediksiState.value = PrediksiUiState.Error(
                        "${e.javaClass.simpleName}: ${e.message}"
                    )
                }
            )
        }
    }

    fun loadAvailableMenus(warungId: String) {
        viewModelScope.launch {
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val snapshot = firestore.collection("menus")
                    .whereEqualTo("warungId", warungId)
                    .get()
                    .await()

                val menus = snapshot.documents
                    .mapNotNull { it.getString("namaMenu") }
                    .filter { it.isNotBlank() }
                    .sorted()

                _availableMenus.value = menus
            } catch (_: Exception) {
            }
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
    NONE,
    SEBAGIAN_KURANG,
    SEMUA_KURANG,
    NO_DATA
}