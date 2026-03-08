package com.zayyan.dormfood.ui.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ── Model ──
data class ItemPesanan(
    val namaMenu: String,
    val harga: Int,
    val quantity: Int
)

enum class StatusPesanan(val label: String) {
    BELUM_BAYAR("Belum Bayar"),
    DIBUAT("Dibuat"),
    DIANTAR("Diantar"),
    SELESAI("Selesai")
}

// ── ViewModel ──
class PesananViewModel : ViewModel() {
    private val _itemPesanan = MutableStateFlow<List<ItemPesanan>>(emptyList())
    val itemPesanan: StateFlow<List<ItemPesanan>> = _itemPesanan

    private val _totalHarga = MutableStateFlow(0L)
    val totalHarga: StateFlow<Long> = _totalHarga

    private val _status = MutableStateFlow(StatusPesanan.BELUM_BAYAR)
    val status: StateFlow<StatusPesanan> = _status

    private val _fotoQr = MutableStateFlow("")
    val fotoQr: StateFlow<String> = _fotoQr

    private val _namaWarung = MutableStateFlow("")
    val namaWarung: StateFlow<String> = _namaWarung

    fun buatPesanan(
        items: List<ItemPesanan>,
        total: Long,
        fotoQr: String,
        namaWarung: String
    ) {
        _itemPesanan.value = items
        _totalHarga.value = total
        _fotoQr.value = fotoQr
        _namaWarung.value = namaWarung
        _status.value = StatusPesanan.BELUM_BAYAR
    }

    fun updateStatus(status: StatusPesanan) {
        _status.value = status
    }
}