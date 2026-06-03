package com.zayyan.dormfood.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.zayyan.dormfood.ui.model.WarungData
import com.zayyan.dormfood.ui.model.WarungItem
import com.zayyan.dormfood.ui.model.WarungProfile

class WarungViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _warungData = MutableStateFlow<WarungData?>(null)
    val warungData: StateFlow<WarungData?> = _warungData
    private val _warungProfile = MutableStateFlow<WarungProfile?>(null)
    val warungProfile: StateFlow<WarungProfile?> = _warungProfile
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _warungList = MutableStateFlow<List<WarungItem>>(emptyList())
    val warungList: StateFlow<List<WarungItem>> = _warungList
    private val _isLoadingList = MutableStateFlow(false)
    val isLoadingList: StateFlow<Boolean> = _isLoadingList
    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage
    private var warungListeners = mutableListOf<() -> Unit>()

    var savedScrollIndex by mutableIntStateOf(0)
        private set
    var savedScrollOffset by mutableIntStateOf(0)
        private set

    fun setUpdateMessage(message: String) {
        _updateMessage.value = message
    }

    fun clearUpdateMessage() {
        _updateMessage.value = null
    }

    fun clearData() {
        _warungData.value = null
        _warungProfile.value = null
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }

    fun loadWarungData() {
        val currentUser = auth.currentUser ?: return

        if (_warungData.value != null && _warungData.value?.uid == currentUser.uid) return

        _isLoading.value = true

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role") ?: ""
                    val data = WarungData(
                        uid = document.getString("uid") ?: "",
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        phone = document.getString("phone") ?: "",
                        role = role
                    )
                    _warungData.value = data

                    if (role == "warung") {
                        loadWarungProfile()
                    }
                }
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun loadWarungProfile() {
        val currentUser = auth.currentUser ?: return

        firestore.collection("warungs")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val data = WarungProfile(
                        userId = doc.getString("userId") ?: "",
                        layananAntar = doc.getBoolean("layananAntar") ?: false,
                        ongkirAktif = doc.getBoolean("ongkirAktif") ?: false,
                        ongkir = (doc.getLong("ongkir") ?: 0).toInt(),
                        alamatWarung = doc.getString("alamatWarung") ?: "",
                        fotoMenu = doc.getString("fotoMenu") ?: "",
                        fotoQr = doc.getString("fotoQr") ?: "",
                        latitude = doc.getDouble("latitude") ?: 0.0,
                        longitude = doc.getDouble("longitude") ?: 0.0
                    )
                    _warungProfile.value = data
                } else {
                    val role = _warungData.value?.role ?: ""
                    if (role == "warung") {
                        createWarungProfile()
                    }
                }
            }
    }

    fun loadAllWarungs() {
        warungListeners.forEach { it() }
        warungListeners.clear()

        if (_warungList.value.isEmpty()) {
            _isLoadingList.value = true
        }

        _isLoadingList.value = true

        val usersListener = firestore.collection("users")
            .whereEqualTo("role", "warung")
            .addSnapshotListener { users, error ->
                if (error != null || users == null) {
                    _isLoadingList.value = false
                    return@addSnapshotListener
                }

                if (users.isEmpty) {
                    _warungList.value = emptyList()
                    _isLoadingList.value = false
                    return@addSnapshotListener
                }

                for (userDoc in users) {
                    val userId = userDoc.getString("uid") ?: userDoc.id
                    val nama = userDoc.getString("name") ?: "Warung"
                    val phone = userDoc.getString("phone") ?: ""

                    val warungListener = firestore.collection("warungs")
                        .document(userId)
                        .addSnapshotListener { warungDoc, warungError ->
                            if (warungError != null || warungDoc == null) return@addSnapshotListener

                            val item = WarungItem(
                                userId = userId,
                                namaWarung = nama,
                                alamatWarung = warungDoc.getString("alamatWarung") ?: "",
                                phone = phone,
                                fotoMenu = warungDoc.getString("fotoMenu") ?: "",
                                layananAntar = warungDoc.getBoolean("layananAntar") ?: false,
                                latitude = warungDoc.getDouble("latitude") ?: 0.0,
                                longitude = warungDoc.getDouble("longitude") ?: 0.0
                            )

                            val current = _warungList.value.toMutableList()
                            val index = current.indexOfFirst { it.userId == userId }
                            if (index >= 0) current[index] = item else current.add(item)
                            _warungList.value = current.toList()

                            _isLoadingList.value = false
                        }

                    warungListeners.add { warungListener.remove() }
                }
            }

        warungListeners.add { usersListener.remove() }
    }

    override fun onCleared() {
        super.onCleared()
        warungListeners.forEach { it() }
        warungListeners.clear()
    }

    private fun createWarungProfile() {
        val currentUser = auth.currentUser ?: return
        val namaWarung = _warungData.value?.name ?: "Warung"

        val data = hashMapOf(
            "userId" to currentUser.uid,
            "layananAntar" to false,
            "ongkirAktif" to false,
            "ongkir" to 0,
            "alamatWarung" to namaWarung,
            "fotoMenu" to "",
            "fotoQr" to ""
        )

        firestore.collection("warungs")
            .document(currentUser.uid)
            .set(data)
            .addOnSuccessListener {
                loadWarungProfile()
            }
    }

    fun updateName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users")
            .document(currentUser.uid)
            .update("name", newName)
            .addOnSuccessListener {
                _warungData.value = _warungData.value?.copy(name = newName)
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal update nama")
            }
    }

    fun updatePhone(newPhone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users")
            .document(currentUser.uid)
            .update("phone", newPhone)
            .addOnSuccessListener {
                _warungData.value = _warungData.value?.copy(phone = newPhone)
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal update phone")
            }
    }

    fun updateLayananAntar(isActive: Boolean, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("warungs")
            .document(currentUser.uid)
            .update("layananAntar", isActive)
            .addOnSuccessListener {
                _warungProfile.value = _warungProfile.value?.copy(layananAntar = isActive)
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal update status delivery")
            }
    }

    fun updateOngkir(
        ongkirAktif: Boolean,
        ongkir: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("warungs")
            .document(currentUser.uid)
            .update(
                mapOf(
                    "ongkirAktif" to ongkirAktif,
                    "ongkir" to ongkir
                )
            )
            .addOnSuccessListener {

                _warungProfile.value =
                    _warungProfile.value?.copy(
                        ongkirAktif = ongkirAktif,
                        ongkir = ongkir
                    )

                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal update ongkir")
            }
    }

    fun uploadMenuImage(imageUri: Uri) {
        val currentUser = auth.currentUser ?: return
        _isLoading.value = true

        MediaManager.get().upload(imageUri)
            .unsigned("dormfood_unsigned")
            .option("folder", "dormfood/menu/${currentUser.uid}")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"].toString()
                    firestore.collection("warungs")
                        .document(currentUser.uid)
                        .update("fotoMenu", imageUrl)
                        .addOnSuccessListener {
                            _warungProfile.value = _warungProfile.value?.copy(fotoMenu = imageUrl)
                            _isLoading.value = false
                            setUpdateMessage("Foto menu berhasil diupload")
                        }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _isLoading.value = false
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    fun uploadQrImage(imageUri: Uri) {
        val currentUser = auth.currentUser ?: return
        _isLoading.value = true

        MediaManager.get().upload(imageUri)
            .unsigned("dormfood_unsigned")
            .option("folder", "dormfood/qr/${currentUser.uid}")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"].toString()
                    firestore.collection("warungs")
                        .document(currentUser.uid)
                        .update("fotoQr", imageUrl)
                        .addOnSuccessListener {
                            _warungProfile.value = _warungProfile.value?.copy(fotoQr = imageUrl)
                            _isLoading.value = false
                            setUpdateMessage("QR Pembayaran berhasil diupload")
                        }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    _isLoading.value = false
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    fun updateAlamatWarung(newAlamat: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("warungs")
            .document(currentUser.uid)
            .update("alamatWarung", newAlamat)
            .addOnSuccessListener {
                _warungProfile.value = _warungProfile.value?.copy(alamatWarung = newAlamat)
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal update alamat")
            }
    }

    fun updateWarungLocation(
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("warungs")
            .document(currentUser.uid)
            .update(
                mapOf(
                    "latitude" to latitude,
                    "longitude" to longitude
                )
            )
            .addOnSuccessListener {
                _warungProfile.value = _warungProfile.value?.copy(
                    latitude = latitude,
                    longitude = longitude
                )
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal menyimpan lokasi")
            }
    }
}