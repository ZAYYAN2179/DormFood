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
    // Dipakai oleh RatingUlasanWarungScreen — baik dari sisi warung sendiri
    // maupun dari sisi mahasiswa yang ingin lihat ulasan warung tertentu.
    // Jika warungId berbeda dari yang sedang di-listen, listener lama dihapus
    // dan diganti dengan listener baru untuk warung yang diminta.
    // ─────────────────────────────────────────────────────────────────────────
    private var currentListenedWarungId: String = ""

    fun loadWarungReviews(warungId: String) {
        // Jika warungId sama dan listener sudah aktif, tidak perlu reload
        if (warungId == currentListenedWarungId && reviewsListener != null) return

        // Hapus listener lama (pindah ke warung berbeda)
        reviewsListener?.remove()
        reviewsListener = null
        currentListenedWarungId = warungId
        _warungReviews.value = emptyList()
        _averageRating.value = 0.0
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
    // FETCH RINGKASAN RATING SATU WARUNG (one-time, untuk MenuMahasiswa)
    // Hasilnya disimpan di map warungId -> Pair(average, totalUlasan)
    // sehingga bisa dipakai di berbagai card tanpa realtime listener.
    // ─────────────────────────────────────────────────────────────────────────
    private val _ratingSummaryMap = MutableStateFlow<Map<String, Pair<Double, Int>>>(emptyMap())
    val ratingSummaryMap: StateFlow<Map<String, Pair<Double, Int>>> = _ratingSummaryMap

    fun fetchRatingSummary(warungId: String) {
        // Skip jika warungId ini sudah pernah di-fetch
        if (_ratingSummaryMap.value.containsKey(warungId)) return

        firestore.collection("reviews")
            .whereEqualTo("warungId", warungId)
            .get()
            .addOnSuccessListener { snapshot ->
                val ratings = snapshot.documents.mapNotNull { doc ->
                    doc.getLong("rating")?.toInt()
                }
                val avg   = if (ratings.isEmpty()) 0.0
                else ratings.sum() / ratings.size.toDouble()
                val total = ratings.size
                _ratingSummaryMap.value = _ratingSummaryMap.value + (warungId to Pair(avg, total))
            }
            .addOnFailureListener {
                // Gagal fetch -> simpan 0 agar tidak retry terus
                _ratingSummaryMap.value = _ratingSummaryMap.value + (warungId to Pair(0.0, 0))
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