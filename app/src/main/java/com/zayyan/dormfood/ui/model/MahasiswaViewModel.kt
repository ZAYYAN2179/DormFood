package com.zayyan.dormfood.ui.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MahasiswaData(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val building: String = "",
    val roomNumber: String = "",
    val role: String = "mahasiswa"
)

class MahasiswaViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _mahasiswaData = MutableStateFlow<MahasiswaData?>(null)
    val mahasiswaData: StateFlow<MahasiswaData?> = _mahasiswaData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // State untuk pesan snackbar — null berarti tidak ada pesan
    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage

    fun setUpdateMessage(message: String) {
        _updateMessage.value = message
    }

    fun clearUpdateMessage() {
        _updateMessage.value = null
    }

    fun fetchMahasiswaData() {
        val uid = auth.currentUser?.uid ?: return

        // Cek apakah data sudah ada dan milik user yang sama
        if (_mahasiswaData.value != null && _mahasiswaData.value?.uid == uid) return

        _isLoading.value = true
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _mahasiswaData.value = MahasiswaData(
                        uid = doc.getString("uid") ?: uid,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        building = doc.getString("building") ?: "",
                        roomNumber = doc.getString("roomNumber") ?: "",
                        role = doc.getString("role") ?: "mahasiswa"
                    )
                }
                _isLoading.value = false
            }
            .addOnFailureListener { _isLoading.value = false }
    }

    fun clearData() {
        _mahasiswaData.value = null
    }

    fun updateName(
        newName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update("name", newName)
            .addOnSuccessListener {
                _mahasiswaData.value = _mahasiswaData.value?.copy(name = newName)
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Gagal update nama") }
    }

    fun updateBuilding(
        newBuilding: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update("building", newBuilding)
            .addOnSuccessListener {
                _mahasiswaData.value = _mahasiswaData.value?.copy(building = newBuilding)
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Gagal update gedung") }
    }

    fun updateRoomNumber(
        newRoomNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid)
            .update("roomNumber", newRoomNumber)
            .addOnSuccessListener {
                _mahasiswaData.value = _mahasiswaData.value?.copy(roomNumber = newRoomNumber)
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Gagal update nomor kamar") }
    }
}