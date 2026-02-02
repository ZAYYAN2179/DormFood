package com.zayyan.dormfood.ui.model

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(
        name: String,
        building: String,
        roomNumber: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (name.isBlank() || building.isBlank() || roomNumber.isBlank() ||
            email.isBlank() || password.isBlank()
        ) {
            _authState.value = AuthState.Error("Semua field harus diisi")
            return
        }

        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Password tidak sama")
            return
        }

        _authState.value = AuthState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val userData = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "building" to building,
                    "roomNumber" to roomNumber,
                    "email" to email,
                    "role" to "mahasiswa"
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        _authState.value = AuthState.Success("Registrasi berhasil")
                    }
                    .addOnFailureListener {
                        _authState.value = AuthState.Error(it.message ?: "Gagal menyimpan data")
                    }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Registrasi gagal")
            }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email dan password harus diisi")
            return
        }

        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                // Ambil data user dari Firestore untuk cek role
                firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role = document.getString("role") ?: "mahasiswa"
                            val name = document.getString("name") ?: ""

                            // Kirim role ke state untuk navigasi
                            _authState.value = AuthState.LoginSuccess(
                                message = "Login berhasil",
                                role = role,
                                userName = name
                            )
                        } else {
                            _authState.value = AuthState.Error("Data user tidak ditemukan")
                        }
                    }
                    .addOnFailureListener {
                        _authState.value = AuthState.Error(it.message ?: "Gagal mengambil data user")
                    }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Login gagal")
            }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}