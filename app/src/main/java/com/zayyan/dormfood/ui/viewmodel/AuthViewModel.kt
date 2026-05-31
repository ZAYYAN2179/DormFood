package com.zayyan.dormfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

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
        if (
            name.isBlank() ||
            building.isBlank() ||
            roomNumber.isBlank() ||
            email.isBlank() ||
            password.isBlank() ||
            confirmPassword.isBlank()
        ) {
            _authState.value = AuthState.Error("Semua field harus diisi")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value =
                AuthState.Error("Format email tidak valid")
            return
        }

        if (password.length < 6) {
            _authState.value =
                AuthState.Error("Kata sandi minimal 6 karakter")
            return
        }

        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Konfirmasi kata sandi tidak sesuai")
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
            .addOnFailureListener { exception ->

                val message = when (exception) {

                    is FirebaseAuthUserCollisionException ->
                        "Email telah terdaftar"

                    is FirebaseAuthWeakPasswordException ->
                        "Kata sandi minimal 6 karakter"

                    is FirebaseAuthInvalidCredentialsException ->
                        "Format email tidak valid"

                    else ->
                        "Registrasi gagal"
                }

                _authState.value = AuthState.Error(message)
            }
    }

    fun login(email: String, password: String) {

        if (email.isBlank() || password.isBlank()) {
            _authState.value =
                AuthState.Error("Email dan kata sandi harus diisi")
            return
        }

        _authState.value = AuthState.Loading

        auth.signInWithEmailAndPassword(email, password)

            .addOnSuccessListener { result ->

                val uid = result.user?.uid
                    ?: return@addOnSuccessListener

                firestore.collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val role =
                                document.getString("role")
                                    ?: "mahasiswa"

                            val name =
                                document.getString("name")
                                    ?: ""

                            _authState.value =
                                AuthState.LoginSuccess(
                                    message = "Login berhasil",
                                    role = role,
                                    userName = name
                                )
                        } else {
                            _authState.value =
                                AuthState.Error(
                                    "Data user tidak ditemukan"
                                )
                        }
                    }
                    .addOnFailureListener {
                        _authState.value =
                            AuthState.Error(
                                "Gagal mengambil data user"
                            )
                    }
            }
            .addOnFailureListener { exception ->
                val message =
                    if (exception is FirebaseAuthException) {
                        when (exception.errorCode) {
                            "ERROR_USER_NOT_FOUND" ->
                                "Email tidak terdaftar"

                            "ERROR_WRONG_PASSWORD" ->
                                "Kata sandi salah"

                            "ERROR_INVALID_CREDENTIAL" ->
                                "Email atau kata sandi salah"

                            else ->
                                "Login gagal"
                        }
                    } else {

                        "Login gagal"
                    }
                _authState.value =
                    AuthState.Error(message)
            }
    }

    fun checkUserRole(onResult: (String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val role = doc.getString("role")
                    onResult(role)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun resetPassword(
        email: String,
        onResult: (String) -> Unit
    ) {

        if (email.isBlank()) {
            onResult("Email harus diisi")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult("Format email tidak valid")
            return
        }

        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()

            .addOnSuccessListener { documents ->

                if (documents.isEmpty) {

                    onResult("Email tidak terdaftar")
                    return@addOnSuccessListener
                }


                auth.sendPasswordResetEmail(email)

                    .addOnSuccessListener {

                        onResult(
                            "Link reset kata sandi telah dikirim ke email Anda"
                        )
                    }

                    .addOnFailureListener {

                        onResult(
                            "Gagal mengirim email reset kata sandi"
                        )
                    }
            }

            .addOnFailureListener {

                onResult(
                    "Gagal memeriksa email"
                )
            }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}