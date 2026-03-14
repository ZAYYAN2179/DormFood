package com.zayyan.dormfood.ui.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ── Models ──
data class ItemPesanan(
    val namaMenu: String,
    val harga: Int,
    val quantity: Int
)

data class OrderItem(
    val orderId: String = "",
    val userId: String = "",
    val warungId: String = "",
    val namaWarung: String = "",
    val items: List<ItemPesanan> = emptyList(),
    val totalHarga: Long = 0,
    val statusPesanan: String = "",
    val buktiPembayaranUrl: String = "",
    val createdAt: Timestamp? = null
)

@Suppress("unused")
enum class StatusPesanan(val label: String) {
    MENUNGGU_KONFIRMASI("Menunggu Konfirmasi"),
    DIBUAT("Dibuat"),
    DIANTAR("Diantar"),
    SELESAI("Selesai")
}

// ── ViewModel ──
class PesananViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ── State untuk membuat pesanan baru ──
    private val _itemPesanan = MutableStateFlow<List<ItemPesanan>>(emptyList())
    val itemPesanan: StateFlow<List<ItemPesanan>> = _itemPesanan

    private val _totalHarga = MutableStateFlow(0L)
    val totalHarga: StateFlow<Long> = _totalHarga

    private val _fotoQr = MutableStateFlow("")
    val fotoQr: StateFlow<String> = _fotoQr

    private val _namaWarung = MutableStateFlow("")
    val namaWarung: StateFlow<String> = _namaWarung

    private var currentWarungId: String = ""

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    // ── State untuk daftar order user ──
    private val _orders = MutableStateFlow<List<OrderItem>>(emptyList())
    val orders: StateFlow<List<OrderItem>> = _orders

    private val _isLoadingOrders = MutableStateFlow(false)
    val isLoadingOrders: StateFlow<Boolean> = _isLoadingOrders

    private var ordersListener: ListenerRegistration? = null

    // Cache nama warung agar tidak fetch ulang setiap snapshot
    private val warungNamesCache = mutableMapOf<String, String>()

    init {
        loadOrders()
    }

    private fun normalizeStatus(raw: String): String {
        return when (raw.trim().uppercase()) {
            "MENUNGGU KONFIRMASI", "MENUNGGU_KONFIRMASI" -> "Menunggu Konfirmasi"
            "DIBUAT"  -> "Dibuat"
            "DIANTAR" -> "Diantar"
            "SELESAI" -> "Selesai"
            else      -> raw
        }
    }

    // ──────────────────────────────────────
    // Membuat & mengirim pesanan baru
    // ──────────────────────────────────────

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
        currentWarungId = warungId
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
        val itemsMap = _itemPesanan.value.map { item ->
            mapOf(
                "namaMenu" to item.namaMenu,
                "harga" to item.harga,
                "quantity" to item.quantity
            )
        }

        val orderData = hashMapOf(
            "userId" to userId,
            "warungId" to currentWarungId,
            "namaWarung" to _namaWarung.value,  // ← simpan namaWarung langsung
            "items" to itemsMap,
            "totalHarga" to _totalHarga.value,
            "statusPesanan" to StatusPesanan.MENUNGGU_KONFIRMASI.label,
            "buktiPembayaranUrl" to buktiPembayaranUrl,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("orders")
            .add(orderData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal menyimpan pesanan") }
    }

    // ──────────────────────────────────────
    // Memuat daftar order milik user
    // ──────────────────────────────────────

    fun loadOrders() {
        val currentUser = auth.currentUser ?: return
        _isLoadingOrders.value = true

        ordersListener?.remove()

        ordersListener = firestore.collection("orders")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoadingOrders.value = false
                    return@addSnapshotListener
                }

                @Suppress("UNCHECKED_CAST")
                val orderList = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val rawItems = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                        val items = rawItems.map { map ->
                            ItemPesanan(
                                namaMenu = map["namaMenu"] as? String ?: "",
                                harga = (map["harga"] as? Long)?.toInt() ?: 0,
                                quantity = (map["quantity"] as? Long)?.toInt() ?: 0
                            )
                        }
                        OrderItem(
                            orderId = doc.id,
                            userId = doc.getString("userId") ?: "",
                            warungId = doc.getString("warungId") ?: "",
                            // Baca namaWarung dari dokumen order langsung
                            // Fallback ke cache jika belum ada field namaWarung (order lama)
                            namaWarung = doc.getString("namaWarung")
                                ?: warungNamesCache[doc.getString("warungId") ?: ""]
                                ?: "",
                            items = items,
                            totalHarga = doc.getLong("totalHarga") ?: 0,
                            statusPesanan = normalizeStatus(
                                doc.getString("statusPesanan") ?: ""
                            ),
                            buktiPembayaranUrl = doc.getString("buktiPembayaranUrl") ?: "",
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    } catch (_: Exception) { null }
                } ?: emptyList()

                // Untuk order lama yang belum punya namaWarung, resolve dari users
                val needResolve = orderList.filter { it.namaWarung.isEmpty() }

                if (needResolve.isEmpty()) {
                    // Semua sudah punya nama → langsung update
                    _orders.value = orderList
                    _isLoadingOrders.value = false
                } else {
                    // Resolve hanya order lama yang belum punya namaWarung
                    val uniqueIds = needResolve.map { it.warungId }.distinct()
                        .filter { !warungNamesCache.containsKey(it) }

                    if (uniqueIds.isEmpty()) {
                        _orders.value = orderList.map { order ->
                            if (order.namaWarung.isEmpty())
                                order.copy(namaWarung = warungNamesCache[order.warungId] ?: "Warung")
                            else order
                        }
                        _isLoadingOrders.value = false
                        return@addSnapshotListener
                    }

                    var resolved = 0
                    for (warungId in uniqueIds) {
                        firestore.collection("users").document(warungId).get()
                            .addOnSuccessListener { doc ->
                                warungNamesCache[warungId] = doc.getString("name") ?: "Warung"
                                resolved++
                                if (resolved == uniqueIds.size) {
                                    _orders.value = orderList.map { order ->
                                        if (order.namaWarung.isEmpty())
                                            order.copy(namaWarung = warungNamesCache[order.warungId] ?: "Warung")
                                        else order
                                    }
                                    _isLoadingOrders.value = false
                                }
                            }
                            .addOnFailureListener {
                                warungNamesCache[warungId] = "Warung"
                                resolved++
                                if (resolved == uniqueIds.size) {
                                    _orders.value = orderList.map { order ->
                                        if (order.namaWarung.isEmpty())
                                            order.copy(namaWarung = warungNamesCache[order.warungId] ?: "Warung")
                                        else order
                                    }
                                    _isLoadingOrders.value = false
                                }
                            }
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
        warungNamesCache.clear()
    }
}