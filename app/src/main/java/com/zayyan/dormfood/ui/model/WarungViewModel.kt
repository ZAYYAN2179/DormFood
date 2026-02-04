package com.zayyan.dormfood.ui.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class WarungViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _warungData = MutableStateFlow<WarungData?>(null)
    val warungData: StateFlow<WarungData?> = _warungData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadWarungData()
    }

    fun loadWarungData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return
        }

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

    fun updatePhone(newPhone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users")
            .document(currentUser.uid)
            .update("phone", newPhone)
            .addOnSuccessListener {
                // Update local state
                _warungData.value = _warungData.value?.copy(phone = newPhone)
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal update nomor telepon")
            }
    }

    fun updateName(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser ?: return

        firestore.collection("users")
            .document(currentUser.uid)
            .update("name", newName)
            .addOnSuccessListener {
                // Update local state
                _warungData.value = _warungData.value?.copy(name = newName)
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Gagal update nama warung")
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