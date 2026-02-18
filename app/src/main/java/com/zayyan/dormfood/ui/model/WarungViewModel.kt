package com.zayyan.dormfood.ui.model

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

    init {
        loadWarungData()
        loadWarungProfile()
    }

    fun loadWarungData() {
        val currentUser = auth.currentUser ?: return

        _isLoading.value = true

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val data = WarungData(
                        uid = document.getString("uid") ?: "",
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        phone = document.getString("phone") ?: "",
                        role = document.getString("role") ?: ""
                    )
                    _warungData.value = data
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
                        alamatWarung = doc.getString("alamatWarung") ?: "",
                        fotoMenu = doc.getString("fotoMenu") ?: "",
                        fotoQr = doc.getString("fotoQr") ?: ""
                    )
                    _warungProfile.value = data
                } else {
                    createWarungProfile()
                }
            }
    }

    fun loadAllWarungs() {
        _isLoadingList.value = true

        firestore.collection("users")
            .whereEqualTo("role", "warung")
            .get()
            .addOnSuccessListener { users ->
                val result = mutableListOf<WarungItem>()
                val total = users.size()

                if (total == 0) {
                    _warungList.value = emptyList()
                    _isLoadingList.value = false
                    return@addOnSuccessListener
                }

                var loaded = 0
                for (userDoc in users) {
                    val userId = userDoc.getString("uid") ?: userDoc.id
                    val nama = userDoc.getString("name") ?: "Warung"
                    val phone = userDoc.getString("phone") ?: ""

                    firestore.collection("warungs")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { warungDoc ->
                            val alamat = warungDoc.getString("alamatWarung") ?: ""
                            val fotoMenu = warungDoc.getString("fotoMenu") ?: ""

                            result.add(
                                WarungItem(
                                    userId = userId,
                                    namaWarung = nama,
                                    alamatWarung = alamat,
                                    phone = phone,
                                    fotoMenu = fotoMenu
                                )
                            )
                            loaded++
                            if (loaded == total) {
                                _warungList.value = result.toList()
                                _isLoadingList.value = false
                            }
                        }
                        .addOnFailureListener {
                            loaded++
                            if (loaded == total) {
                                _warungList.value = result.toList()
                                _isLoadingList.value = false
                            }
                        }
                }
            }
            .addOnFailureListener {
                _isLoadingList.value = false
            }
    }

    private fun createWarungProfile() {
        val currentUser = auth.currentUser ?: return
        val namaWarung = _warungData.value?.name ?: "Warung"

        val data = hashMapOf(
            "userId" to currentUser.uid,
            "layananAntar" to false,
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

    fun updatePhone(newPhone: String, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users")
            .document(currentUser.uid)
            .update("phone", newPhone)
            .addOnSuccessListener {
                _warungData.value = _warungData.value?.copy(phone = newPhone)
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal update phone")
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
}

data class WarungData(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = ""
)

data class WarungProfile(
    val userId: String = "",
    val layananAntar: Boolean = false,
    val alamatWarung: String = "",
    val fotoMenu: String = "",
    val fotoQr: String = ""
)

data class WarungItem(
    val userId: String = "",
    val namaWarung: String = "",
    val alamatWarung: String = "",
    val phone: String = "",
    val fotoMenu: String = ""
)