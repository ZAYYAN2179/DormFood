package com.zayyan.dormfood.ui.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

class PesananViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

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

    private val _warungId = MutableStateFlow("")
    val warungId: StateFlow<String> = _warungId

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    fun buatPesanan(
        items: List<ItemPesanan>,
        total: Long,
        fotoQr: String,
        namaWarung: String,
        warungId: String
    ) {
        _itemPesanan.value = items
        _totalHarga.value = total
        _fotoQr.value = fotoQr
        _namaWarung.value = namaWarung
        _warungId.value = warungId
        _status.value = StatusPesanan.BELUM_BAYAR
    }

    fun updateStatus(status: StatusPesanan) {
        _status.value = status
    }

    fun uploadBuktiDanKirimPesanan(
        buktiUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("User tidak ditemukan")
            return
        }

        _isUploading.value = true

        MediaManager.get().upload(buktiUri)
            .unsigned("dormfood_unsigned")
            .option("folder", "dormfood/bukti_pembayaran/${currentUser.uid}")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val buktiUrl = resultData["secure_url"].toString()
                    simpanOrderKeFirestore(
                        userId = currentUser.uid,
                        buktiPembayaranUrl = buktiUrl,
                        onSuccess = {
                            _isUploading.value = false
                            onSuccess()
                        },
                        onError = { err ->
                            _isUploading.value = false
                            onError(err)
                        }
                    )
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _isUploading.value = false
                    onError(error.description ?: "Gagal upload gambar")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    private fun simpanOrderKeFirestore(
        userId: String,
        buktiPembayaranUrl: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Konversi ItemPesanan ke Map agar bisa disimpan di Firestore
        val itemsMap = _itemPesanan.value.map { item ->
            mapOf(
                "namaMenu" to item.namaMenu,
                "harga" to item.harga,
                "quantity" to item.quantity
            )
        }

        val orderData = hashMapOf(
            "userId" to userId,
            "warungId" to _warungId.value,
            "items" to itemsMap,
            "totalHarga" to _totalHarga.value,
            "statusPesanan" to StatusPesanan.BELUM_BAYAR.label,
            "buktiPembayaranUrl" to buktiPembayaranUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )

        // Firestore auto-generate orderId
        firestore.collection("orders")
            .add(orderData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Gagal menyimpan pesanan")
            }
    }
}