package com.zayyan.dormfood.ui.model

import com.google.firebase.Timestamp

data class OrderItem(
    val orderId: String = "",
    val userId: String = "",
    val warungId: String = "",
    val namaWarung: String = "",
    val namaUser: String = "",
    val building: String = "",
    val roomNumber: String = "",
    val items: List<ItemPesanan> = emptyList(),
    val totalHarga: Long = 0,
    val statusPesanan: String = "",
    val buktiPembayaranUrl: String = "",
    val createdAt: Timestamp? = null
)