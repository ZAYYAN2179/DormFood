package com.zayyan.dormfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.zayyan.dormfood.ui.model.ReviewItem
import com.zayyan.dormfood.ui.model.SubmitReviewState

class RatingViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _submitState = MutableStateFlow<SubmitReviewState>(SubmitReviewState.Idle)
    val submitState: StateFlow<SubmitReviewState> = _submitState
    private val _warungReviews = MutableStateFlow<List<ReviewItem>>(emptyList())
    val warungReviews: StateFlow<List<ReviewItem>> = _warungReviews
    private val _isLoadingReviews = MutableStateFlow(false)
    val isLoadingReviews: StateFlow<Boolean> = _isLoadingReviews
    private val _averageRating = MutableStateFlow(0.0)
    val averageRating: StateFlow<Double> = _averageRating
    private var reviewsListener: ListenerRegistration? = null

    fun kirimUlasan(
        orderId: String,
        warungId: String,
        namaWarung: String,
        rating: Int,
        ulasan: String
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _submitState.value =
                SubmitReviewState.Error("User tidak ditemukan, silakan login ulang.")
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

        firestore.collection("reviews")
            .whereEqualTo("orderId", orderId)
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    _submitState.value = SubmitReviewState.AlreadyReviewed
                    return@addOnSuccessListener
                }

                firestore.collection("users").document(currentUser.uid).get()
                    .addOnSuccessListener { userDoc ->
                        val namaUser = userDoc.getString("name") ?: "Pengguna"

                        val reviewData = hashMapOf(
                            "orderId" to orderId,
                            "userId" to currentUser.uid,
                            "namaUser" to namaUser,
                            "warungId" to warungId,
                            "namaWarung" to namaWarung,
                            "rating" to rating,
                            "ulasan" to ulasan.trim(),
                            "createdAt" to FieldValue.serverTimestamp()
                        )

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

    private var currentListenedWarungId: String = ""

    fun loadWarungReviews(warungId: String) {
        if (warungId == currentListenedWarungId && reviewsListener != null) return

        reviewsListener?.remove()
        reviewsListener = null
        currentListenedWarungId = warungId
        _warungReviews.value = emptyList()
        _averageRating.value = 0.0
        _isLoadingReviews.value = true

        reviewsListener = firestore.collection("reviews")
            .whereEqualTo("warungId", warungId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoadingReviews.value = false

                if (error != null) {
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ReviewItem(
                            reviewId = doc.id,
                            orderId = doc.getString("orderId") ?: "",
                            userId = doc.getString("userId") ?: "",
                            namaUser = doc.getString("namaUser") ?: "",
                            warungId = doc.getString("warungId") ?: "",
                            namaWarung = doc.getString("namaWarung") ?: "",
                            rating = (doc.getLong("rating")?.toInt()) ?: 0,
                            ulasan = doc.getString("ulasan") ?: "",
                            createdAt = doc.getTimestamp("createdAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                } ?: emptyList()

                _warungReviews.value = list
                _averageRating.value = if (list.isEmpty()) 0.0
                else list.sumOf { it.rating } / list.size.toDouble()
            }
    }

    private val _ratingSummaryMap = MutableStateFlow<Map<String, Pair<Double, Int>>>(emptyMap())
    val ratingSummaryMap: StateFlow<Map<String, Pair<Double, Int>>> = _ratingSummaryMap

    fun fetchRatingSummary(warungId: String) {
        if (_ratingSummaryMap.value.containsKey(warungId)) return

        firestore.collection("reviews")
            .whereEqualTo("warungId", warungId)
            .get()
            .addOnSuccessListener { snapshot ->
                val ratings = snapshot.documents.mapNotNull { doc ->
                    doc.getLong("rating")?.toInt()
                }
                val avg = if (ratings.isEmpty()) 0.0
                else ratings.sum() / ratings.size.toDouble()
                val total = ratings.size
                _ratingSummaryMap.value = _ratingSummaryMap.value + (warungId to Pair(avg, total))
            }
            .addOnFailureListener {
                _ratingSummaryMap.value = _ratingSummaryMap.value + (warungId to Pair(0.0, 0))
            }
    }

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

    fun laporkanUlasan(
        warungId: String,
        reviewId: String,
        alasan: String,
        deskripsi: String,
        onResult: (berhasil: Boolean, pesan: String) -> Unit = { _, _ -> }
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(false, "User tidak ditemukan, silakan login ulang.")
            return
        }

        firestore.collection("laporan_ulasan")
            .whereEqualTo("reviewId", reviewId)
            .whereEqualTo("pelaporId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    onResult(false, "Kamu sudah pernah melaporkan ulasan ini.")
                    return@addOnSuccessListener
                }

                val laporanData = hashMapOf(
                    "warungId" to warungId,
                    "reviewId" to reviewId,
                    "alasan" to alasan,
                    "deskripsi" to deskripsi,
                    "pelaporId" to currentUser.uid,
                    "status" to "pending",
                    "createdAt" to FieldValue.serverTimestamp()
                )

                firestore.collection("laporan_ulasan")
                    .add(laporanData)
                    .addOnSuccessListener {
                        onResult(true, "Laporan terkirim, terima kasih.")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.message ?: "Gagal mengirim laporan.")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Gagal memeriksa laporan sebelumnya.")
            }
    }

    fun resetState() {
        _submitState.value = SubmitReviewState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        reviewsListener?.remove()
        reviewsListener = null
    }
}