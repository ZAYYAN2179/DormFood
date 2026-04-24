package com.zayyan.dormfood.ui.model

import com.google.firebase.Timestamp

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