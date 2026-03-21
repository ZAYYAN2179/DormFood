package com.zayyan.dormfood.ui.model

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// ─── Data class untuk satu review ────────────────────────────────────────────
data class ReviewItem(
    val reviewId: String = "",
    val orderId: String = "",
    val userId: String = "",
    val namaUser: String = "",
    val warungId: String = "",
    val namaWarung: String = "",
    val rating: Int = 0,
    val ulasan: String = "",
    val createdAt: Timestamp? = null
)

// ─── Sealed class untuk state submit ─────────────────────────────────────────
sealed class SubmitReviewState {
    object Idle : SubmitReviewState()
    object Loading : SubmitReviewState()
    object Success : SubmitReviewState()
    data class Error(val message: String) : SubmitReviewState()
    object AlreadyReviewed : SubmitReviewState()
}

class RatingViewModel : ViewModel() {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // ── State submit ──────────────────────────────────────────────────────────
    private val _submitState = MutableStateFlow<SubmitReviewState>(SubmitReviewState.Idle)
    val submitState: StateFlow<SubmitReviewState> = _submitState

    // ── Daftar review milik warung tertentu (untuk halaman warung) ────────────
    private val _warungReviews = MutableStateFlow<List<ReviewItem>>(emptyList())
    val warungReviews: StateFlow<List<ReviewItem>> = _warungReviews

    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews

    // ── Rata-rata rating warung ───────────────────────────────────────────────
    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating

    private var reviewsListener: ListenerRegistration? = null

    // ─────────────────────────────────────────────────────────────────────────
    // KIRIM ULASAN
    // Validasi duplikat berdasarkan orderId sebelum menyimpan.
    // ─────────────────────────────────────────────────────────────────────────
    fun kirimUlasan(
        orderId: String,
        warungId: String,
        namaWarung: String,
        rating: Int,
        ulasan: String
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _submitState.value = SubmitReviewState.Error("User tidak ditemukan, silakan login ulang.")
            return
        }
        if (rating == 0) {
            _submitState.value = SubmitReviewState.Error("Pilih rating bintang terlebih dahulu.")
            return
        }
        if (orderId.isBlank() || warungId.isBlank()) {
            _submitState.value = SubmitReviewState.Error("Data pesanan tidak valid.")
            return
        }

        _submitState.value = SubmitReviewState.Loading

        // 1. Cek duplikat: apakah orderId ini sudah pernah diulas
        firestore.collection("reviews")
            .whereEqualTo("orderId", orderId)
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    // Sudah pernah mengulas order ini
                    _submitState.value = SubmitReviewState.AlreadyReviewed
                    return@addOnSuccessListener
                }

                // 2. Ambil nama user terbaru dari Firestore
                firestore.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { userDoc ->
                        val namaUser = userDoc.getString("name") ?: "Pengguna"

                        val reviewData = hashMapOf(
                            "orderId"    to orderId,
                            "userId"     to currentUser.uid,
                            "namaUser"   to namaUser,
                            "warungId"   to warungId,
                            "namaWarung" to namaWarung,
                            "rating"     to rating,
                            "ulasan"     to ulasan.trim(),
                            "createdAt"  to FieldValue.serverTimestamp()
                        )

                        // 3. Simpan ke collection "reviews"
                        firestore.collection("reviews")
                            .add(reviewData)
                            .addOnSuccessListener {
                                _submitState.value = SubmitReviewState.Success
                            }
                            .addOnFailureListener { e ->
                                _submitState.value = SubmitReviewState.Error(
                                    e.message ?: "Gagal menyimpan ulasan."
                                )
                            }
                    }
                    .addOnFailureListener { e ->
                        _submitState.value = SubmitReviewState.Error(
                            e.message ?: "Gagal mengambil data user."
                        )
                    }
            }
            .addOnFailureListener { e ->
                _submitState.value = SubmitReviewState.Error(
                    e.message ?: "Gagal memeriksa ulasan sebelumnya."
                )
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD REVIEWS WARUNG (realtime)
    // Gunakan di halaman profil/dashboard warung.
    // ─────────────────────────────────────────────────────────────────────────
    fun loadWarungReviews(warungId: String) {
        if (reviewsListener != null) return
        _isLoadingReviews.value = true

        reviewsListener = firestore.collection("reviews")
            .whereEqualTo("warungId", warungId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _isLoadingReviews.value = false
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ReviewItem(
                            reviewId   = doc.id,
                            orderId    = doc.getString("orderId")    ?: "",
                            userId     = doc.getString("userId")     ?: "",
                            namaUser   = doc.getString("namaUser")   ?: "",
                            warungId   = doc.getString("warungId")   ?: "",
                            namaWarung = doc.getString("namaWarung") ?: "",
                            rating     = (doc.getLong("rating")?.toInt()) ?: 0,
                            ulasan     = doc.getString("ulasan")     ?: "",
                            createdAt  = doc.getTimestamp("createdAt")
                        )
                    } catch (_: Exception) { null }
                } ?: emptyList()

                _warungReviews.value    = list
                _averageRating.value    = if (list.isEmpty()) 0.0
                else list.sumOf { it.rating } / list.size.toDouble()
                _isLoadingReviews.value = false
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CEGAH TOMBOL "Berikan Ulasan" muncul lagi jika sudah diulas
    // Panggil dari OrderCard atau PesananScreen.
    // ─────────────────────────────────────────────────────────────────────────
    fun cekSudahDiulas(
        orderId: String,
        onResult: (sudahDiulas: Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return onResult(false)
        firestore.collection("reviews")
            .whereEqualTo("orderId", orderId)
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot -> onResult(!snapshot.isEmpty) }
            .addOnFailureListener { onResult(false) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reset state (panggil saat keluar dari screen rating)
    // ─────────────────────────────────────────────────────────────────────────
    fun resetState() {
        _submitState.value = SubmitReviewState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        reviewsListener?.remove()
        reviewsListener = null
    }
}