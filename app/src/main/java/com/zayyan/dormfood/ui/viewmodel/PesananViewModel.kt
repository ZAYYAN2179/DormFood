package com.zayyan.dormfood.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.zayyan.dormfood.ui.model.ItemPesanan
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.model.StatusPesanan
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class PesananViewModel : ViewModel() {
    private var currentNamaUser: String = ""
    private var currentBuilding: String = ""
    private var currentRoomNumber: String = ""
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _itemPesanan = MutableStateFlow<List<ItemPesanan>>(emptyList())
    val itemPesanan: StateFlow<List<ItemPesanan>> = _itemPesanan
    private val _subtotal = MutableStateFlow(0L)

    private val _ongkir = MutableStateFlow(0L)
    val ongkir: StateFlow<Long> = _ongkir

    private val _totalHarga = MutableStateFlow(0L)
    val totalHarga: StateFlow<Long> = _totalHarga
    private val _fotoQr = MutableStateFlow("")
    val fotoQr: StateFlow<String> = _fotoQr
    private val _namaWarung = MutableStateFlow("")
    val namaWarung: StateFlow<String> = _namaWarung
    private var currentWarungId: String = ""
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading
    private val _orders = MutableStateFlow<List<OrderItem>>(emptyList())
    val orders: StateFlow<List<OrderItem>> = _orders
    private val _isLoadingOrders = MutableStateFlow(false)
    val isLoadingOrders: StateFlow<Boolean> = _isLoadingOrders
    private var ordersListener: ListenerRegistration? = null
    private val warungNamesCache = mutableMapOf<String, String>()
    private val _warungOrders = MutableStateFlow<List<OrderItem>>(emptyList())
    val warungOrders: StateFlow<List<OrderItem>> = _warungOrders
    private val _isLoadingWarungOrders = MutableStateFlow(false)
    val isLoadingWarungOrders: StateFlow<Boolean> = _isLoadingWarungOrders
    private val _updateStatusResult = MutableStateFlow<String?>(null)
    private var warungOrdersListener: ListenerRegistration? = null
    private val userDataCache = mutableMapOf<String, Triple<String, String, String>>()
    private val _statusChangeEvent =
        MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 5)
    val statusChangeEvent: SharedFlow<Pair<String, String>> = _statusChangeEvent
    private val previousWarungOrderIds = mutableSetOf<String>()
    private val _newOrderEvent = MutableSharedFlow<OrderItem>(extraBufferCapacity = 5)
    val newOrderEvent: SharedFlow<OrderItem> = _newOrderEvent
    private var isFirstWarungLoad = true
    private val _orderRejectedEvent = MutableSharedFlow<String>(extraBufferCapacity = 5)
    val orderRejectedEvent: SharedFlow<String> = _orderRejectedEvent
    private val previousOrderWarungNames = mutableMapOf<String, String>()

    private val previousOrderStatuses = mutableMapOf<String, String>()

    init {
        if (auth.currentUser != null) {
            loadOrders()
            loadWarungOrders()
        }
    }

    private fun normalizeStatus(raw: String): String {
        return when (raw.trim().uppercase()) {
            "MENUNGGU KONFIRMASI", "MENUNGGU_KONFIRMASI" -> "Menunggu Konfirmasi"
            "DIBUAT" -> "Dibuat"
            "DIANTAR" -> "Diantar"
            "SELESAI" -> "Selesai"
            else -> raw.trim()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseOrderDoc(doc: com.google.firebase.firestore.DocumentSnapshot): OrderItem? {
        return try {
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
                namaWarung = doc.getString("namaWarung") ?: "",
                namaUser = doc.getString("namaUser") ?: "",
                building = doc.getString("building") ?: "",
                roomNumber = doc.getString("roomNumber") ?: "",
                items = items,
                subtotal = doc.getLong("subtotal") ?: 0,
                ongkir = doc.getLong("ongkir") ?: 0,
                totalHarga = doc.getLong("totalHarga") ?: 0,
                statusPesanan = normalizeStatus(doc.getString("statusPesanan") ?: ""),
                buktiPembayaranUrl = doc.getString("buktiPembayaranUrl") ?: "",
                createdAt = doc.getTimestamp("createdAt")
            )
        } catch (_: Exception) {
            null
        }
    }

    fun buatPesanan(
        items: List<ItemPesanan>,
        subtotal: Long,
        ongkir: Long,
        fotoQr: String,
        namaWarung: String,
        warungId: String,
        namaUser: String = "",
        building: String = "",
        roomNumber: String = ""
    ) {
        _itemPesanan.value = items
        _subtotal.value = subtotal
        _ongkir.value = ongkir
        _totalHarga.value = subtotal + ongkir
        _fotoQr.value = fotoQr
        _namaWarung.value = namaWarung
        currentWarungId = warungId
        currentNamaUser = namaUser
        currentBuilding = building
        currentRoomNumber = roomNumber
    }

    fun uploadBuktiDanKirimPesanan(
        buktiUri: Uri,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser ?: run { onError("User tidak ditemukan"); return }
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
                        onSuccess = { _isUploading.value = false; onSuccess() },
                        onError = { err -> _isUploading.value = false; onError(err) }
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
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val namaUser = currentNamaUser.ifBlank { userDoc.getString("name") ?: "" }
                val building = currentBuilding.ifBlank { userDoc.getString("building") ?: "" }
                val roomNumber = currentRoomNumber.ifBlank { userDoc.getString("roomNumber") ?: "" }

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
                    "namaWarung" to _namaWarung.value,
                    "namaUser" to namaUser,
                    "building" to building,
                    "roomNumber" to roomNumber,
                    "items" to itemsMap,
                    "subtotal" to _subtotal.value,
                    "ongkir" to _ongkir.value,
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
            .addOnFailureListener { e -> onError(e.message ?: "Gagal mengambil data user") }
    }

    fun loadOrders() {
        val currentUser = auth.currentUser ?: return
        if (ordersListener != null) return

        _isLoadingOrders.value = true

        ordersListener = firestore.collection("orders")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoadingOrders.value = false
                    return@addSnapshotListener
                }

                val orderList = snapshot?.documents
                    ?.mapNotNull { parseOrderDoc(it) }
                    ?: emptyList()

                val needResolve = orderList.filter { it.namaWarung.isEmpty() }

                if (needResolve.isEmpty()) {
                    orderList.forEach { order ->
                        val prevStatus = previousOrderStatuses[order.orderId]
                        if (prevStatus != null && prevStatus != order.statusPesanan) {
                            _statusChangeEvent.tryEmit(Pair(order.namaWarung, order.statusPesanan))
                        }
                        previousOrderStatuses[order.orderId] = order.statusPesanan
                        previousOrderWarungNames[order.orderId] = order.namaWarung
                    }

                    val currentIds = orderList.map { it.orderId }.toSet()
                    previousOrderWarungNames.entries
                        .filter { it.key !in currentIds }
                        .forEach { (_, namaWarung) ->
                            _orderRejectedEvent.tryEmit(namaWarung)
                        }
                    previousOrderWarungNames.keys.retainAll(currentIds)
                    previousOrderStatuses.keys.retainAll(currentIds)

                    _orders.value = orderList
                    _isLoadingOrders.value = false
                    return@addSnapshotListener
                }

                val uniqueIds = needResolve
                    .map { it.warungId }
                    .distinct()
                    .filter { !warungNamesCache.containsKey(it) }

                if (uniqueIds.isEmpty()) {
                    val resolvedList = orderList.map { order ->
                        if (order.namaWarung.isEmpty())
                            order.copy(namaWarung = warungNamesCache[order.warungId] ?: "Warung")
                        else order
                    }

                    resolvedList.forEach { order ->
                        val prevStatus = previousOrderStatuses[order.orderId]
                        if (prevStatus != null && prevStatus != order.statusPesanan) {
                            _statusChangeEvent.tryEmit(Pair(order.namaWarung, order.statusPesanan))
                        }
                        previousOrderStatuses[order.orderId] = order.statusPesanan
                        previousOrderWarungNames[order.orderId] = order.namaWarung
                    }

                    val currentIds = resolvedList.map { it.orderId }.toSet()
                    previousOrderWarungNames.entries
                        .filter { it.key !in currentIds }
                        .forEach { (_, namaWarung) ->
                            _orderRejectedEvent.tryEmit(namaWarung)
                        }
                    previousOrderWarungNames.keys.retainAll(currentIds)
                    previousOrderStatuses.keys.retainAll(currentIds)

                    _orders.value = resolvedList
                    _isLoadingOrders.value = false
                    return@addSnapshotListener
                }

                var resolved = 0
                for (warungId in uniqueIds) {
                    firestore.collection("users").document(warungId).get()
                        .addOnCompleteListener { task ->
                            warungNamesCache[warungId] =
                                if (task.isSuccessful) task.result?.getString("name") ?: "Warung"
                                else "Warung"
                            resolved++
                            if (resolved == uniqueIds.size) {
                                val resolvedList = orderList.map { order ->
                                    if (order.namaWarung.isEmpty())
                                        order.copy(namaWarung = warungNamesCache[order.warungId] ?: "Warung")
                                    else order
                                }

                                resolvedList.forEach { order ->
                                    val prevStatus = previousOrderStatuses[order.orderId]
                                    if (prevStatus != null && prevStatus != order.statusPesanan) {
                                        _statusChangeEvent.tryEmit(Pair(order.namaWarung, order.statusPesanan))
                                    }
                                    previousOrderStatuses[order.orderId] = order.statusPesanan
                                    previousOrderWarungNames[order.orderId] = order.namaWarung
                                }

                                val currentIds = resolvedList.map { it.orderId }.toSet()
                                previousOrderWarungNames.entries
                                    .filter { it.key !in currentIds }
                                    .forEach { (_, namaWarung) ->
                                        _orderRejectedEvent.tryEmit(namaWarung)
                                    }
                                previousOrderWarungNames.keys.retainAll(currentIds)
                                previousOrderStatuses.keys.retainAll(currentIds)

                                _orders.value = resolvedList
                                _isLoadingOrders.value = false
                            }
                        }
                }
            }
    }

    fun loadWarungOrders() {
        val currentUser = auth.currentUser ?: return
        if (warungOrdersListener != null) return

        _isLoadingWarungOrders.value = true

        warungOrdersListener = firestore.collection("orders")
            .whereEqualTo("warungId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("PesananVM", "WarungOrders error: ${error.message}")
                    _isLoadingWarungOrders.value = false
                    return@addSnapshotListener
                }

                val orderList = snapshot?.documents
                    ?.mapNotNull { parseOrderDoc(it) }
                    ?: emptyList()

                if (orderList.isEmpty()) {
                    previousWarungOrderIds.clear()
                    _warungOrders.value = emptyList()
                    _isLoadingWarungOrders.value = false
                    return@addSnapshotListener
                }

                val uniqueUserIds = orderList.map { it.userId }.distinct()
                var resolved = 0

                for (uid in uniqueUserIds) {
                    firestore.collection("users").document(uid).get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val doc = task.result
                                userDataCache[uid] = Triple(
                                    doc?.getString("name") ?: "",
                                    doc?.getString("building") ?: "",
                                    doc?.getString("roomNumber") ?: ""
                                )
                            }
                            resolved++
                            if (resolved == uniqueUserIds.size) {
                                val resolvedList = orderList.map { order ->
                                    val userData = userDataCache[order.userId]
                                    if (userData != null) {
                                        order.copy(
                                            namaUser = userData.first,
                                            building = userData.second,
                                            roomNumber = userData.third
                                        )
                                    } else order
                                }

                                if (isFirstWarungLoad) {
                                    isFirstWarungLoad = false
                                } else {
                                    resolvedList.forEach { order ->
                                        if (!previousWarungOrderIds.contains(order.orderId)) {
                                            _newOrderEvent.tryEmit(order)
                                        }
                                    }
                                }

                                previousWarungOrderIds.clear()
                                previousWarungOrderIds.addAll(resolvedList.map { it.orderId })
                                _warungOrders.value = resolvedList
                                _isLoadingWarungOrders.value = false
                            }
                        }
                }
            }
    }

    fun refreshWarungOrders() {
        warungOrdersListener?.remove()
        warungOrdersListener = null
        userDataCache.clear()
        isFirstWarungLoad = true
        _warungOrders.value = emptyList()
        _isLoadingWarungOrders.value = false
        loadWarungOrders()
    }

    fun updateStatusPesanan(
        orderId: String,
        statusBaru: StatusPesanan,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (orderId.isBlank()) {
            _updateStatusResult.value = "Error: orderId kosong"
            return
        }
        firestore.collection("orders")
            .document(orderId)
            .update("statusPesanan", statusBaru.label)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                _updateStatusResult.value = "Gagal: ${e.message}"
                onError(e.message ?: "Terjadi kesalahan")
            }
    }

    fun resetPesananState() {
        _itemPesanan.value = emptyList()
        _subtotal.value = 0L
        _ongkir.value = 0L
        _totalHarga.value = 0L
        _fotoQr.value = ""
        _namaWarung.value = ""
        currentWarungId = ""
        currentNamaUser = ""
        currentBuilding = ""
        currentRoomNumber = ""
    }

    fun clearAndReload() {
        ordersListener?.remove()
        ordersListener = null
        warungNamesCache.clear()
        previousOrderStatuses.clear()
        previousOrderWarungNames.clear()
        _orders.value = emptyList()
        _isLoadingOrders.value = false
        loadOrders()
    }

    override fun onCleared() {
        super.onCleared()
        ordersListener?.remove()
        ordersListener = null
        warungOrdersListener?.remove()
        warungOrdersListener = null
        warungNamesCache.clear()
        userDataCache.clear()
    }

    fun batalkanDanHapusPesanan(
        orderId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (orderId.isBlank()) return

        firestore.collection("orders")
            .document(orderId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Gagal membatalkan pesanan") }
    }
}