package com.zayyan.dormfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.zayyan.dormfood.ui.model.MenuItem
import com.zayyan.dormfood.ui.model.MenuWarungDetail

class MenuViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val warungDetailCache = mutableMapOf<String, MenuWarungDetail>()
    private val menuByWarungCache = mutableMapOf<String, List<MenuItem>>()

    private val _menuList = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuList: StateFlow<List<MenuItem>> = _menuList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _warungDetail = MutableStateFlow<MenuWarungDetail?>(null)
    val warungDetail: StateFlow<MenuWarungDetail?> = _warungDetail

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail

    private val _menuListByWarung = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuListByWarung: StateFlow<List<MenuItem>> = _menuListByWarung

    init {
        loadMenuList()
    }

    fun loadMenuList() {
        val currentUser = auth.currentUser ?: return
        _isLoading.value = true

        firestore.collection("menus")
            .whereEqualTo("warungId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = error.message
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val menus = snapshot.documents.mapNotNull { doc ->
                        try {
                            MenuItem(
                                menuId = doc.id,
                                warungId = doc.getString("warungId") ?: "",
                                namaMenu = doc.getString("namaMenu") ?: "",
                                harga = doc.getLong("harga")?.toInt() ?: 0,
                                ketersediaan = doc.getBoolean("ketersediaan") ?: true,
                                tipe = doc.getString("tipe") ?: "Makanan"
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }.sortedBy { it.namaMenu.lowercase() }
                    _menuList.value = menus
                }
                _isLoading.value = false
            }
    }

    fun loadWarungDetail(userId: String) {
        val cachedDetail = warungDetailCache[userId]
        val cachedMenu = menuByWarungCache[userId]

        if (cachedDetail != null && cachedMenu != null) {
            _warungDetail.value = cachedDetail
            _menuListByWarung.value = cachedMenu
            return
        }

        _isLoadingDetail.value = true

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { userDoc ->
                val nama = userDoc.getString("name") ?: ""
                val phone = userDoc.getString("phone") ?: ""

                firestore.collection("warungs").document(userId).get()
                    .addOnSuccessListener { warungDoc ->
                        val detail = MenuWarungDetail(
                            namaWarung = nama,
                            alamatWarung = warungDoc.getString("alamatWarung") ?: "",
                            phone = phone,
                            fotoMenu = warungDoc.getString("fotoMenu") ?: "",
                            fotoQr = warungDoc.getString("fotoQr") ?: ""
                        )
                        _warungDetail.value = detail
                        warungDetailCache[userId] = detail

                        firestore.collection("menus")
                            .whereEqualTo("warungId", userId)
                            .get()
                            .addOnSuccessListener { menuSnapshot ->
                                val menus = menuSnapshot.documents.mapNotNull { doc ->
                                    try {
                                        MenuItem(
                                            menuId = doc.id,
                                            warungId = doc.getString("warungId") ?: "",
                                            namaMenu = doc.getString("namaMenu") ?: "",
                                            harga = doc.getLong("harga")?.toInt() ?: 0,
                                            ketersediaan = doc.getBoolean("ketersediaan") ?: true,
                                            tipe = doc.getString("tipe") ?: "Makanan"
                                        )
                                    } catch (_: Exception) {
                                        null
                                    }
                                }.sortedBy { it.namaMenu.lowercase() }
                                _menuListByWarung.value = menus
                                menuByWarungCache[userId] = menus
                                _isLoadingDetail.value = false
                            }
                            .addOnFailureListener { _isLoadingDetail.value = false }
                    }
                    .addOnFailureListener { _isLoadingDetail.value = false }
            }
            .addOnFailureListener { _isLoadingDetail.value = false }
    }

    override fun onCleared() {
        super.onCleared()
        warungDetailCache.clear()
        menuByWarungCache.clear()
    }

    fun addMenu(
        namaMenu: String,
        harga: Int,
        tipe: String,
        ketersediaan: Boolean = true,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("User tidak terautentikasi"); return
        }
        if (namaMenu.isBlank()) {
            onError("Nama menu tidak boleh kosong"); return
        }
        if (harga <= 0) {
            onError("Harga harus lebih dari 0"); return
        }

        _isLoading.value = true
        val menuData = hashMapOf(
            "warungId" to currentUser.uid,
            "namaMenu" to namaMenu.trim(),
            "harga" to harga,
            "ketersediaan" to ketersediaan,
            "tipe" to tipe,
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        firestore.collection("menus").add(menuData)
            .addOnSuccessListener { _isLoading.value = false; onSuccess() }
            .addOnFailureListener {
                _isLoading.value = false; onError(
                it.message ?: "Gagal menambahkan menu"
            )
            }
    }

    fun updateMenu(
        menuId: String,
        namaMenu: String,
        harga: Int,
        tipe: String,
        ketersediaan: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (menuId.isBlank()) {
            onError("ID menu tidak valid"); return
        }
        if (namaMenu.isBlank()) {
            onError("Nama menu tidak boleh kosong"); return
        }
        if (harga <= 0) {
            onError("Harga harus lebih dari 0"); return
        }

        _isLoading.value = true
        val updates = hashMapOf<String, Any>(
            "namaMenu" to namaMenu.trim(),
            "harga" to harga,
            "tipe" to tipe,
            "ketersediaan" to ketersediaan,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        firestore.collection("menus").document(menuId).update(updates)
            .addOnSuccessListener { _isLoading.value = false; onSuccess() }
            .addOnFailureListener {
                _isLoading.value = false; onError(
                it.message ?: "Gagal mengupdate menu"
            )
            }
    }

    fun toggleKetersediaan(menuId: String, currentStatus: Boolean, onError: (String) -> Unit) {
        firestore.collection("menus").document(menuId)
            .update("ketersediaan", !currentStatus)
            .addOnFailureListener { onError(it.message ?: "Gagal mengubah ketersediaan") }
    }

    fun deleteMenu(menuId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (menuId.isBlank()) {
            onError("ID menu tidak valid"); return
        }
        _isLoading.value = true
        firestore.collection("menus").document(menuId).delete()
            .addOnSuccessListener { _isLoading.value = false; onSuccess() }
            .addOnFailureListener {
                _isLoading.value = false; onError(
                it.message ?: "Gagal menghapus menu"
            )
            }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}