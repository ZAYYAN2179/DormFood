package com.zayyan.dormfood.ui.model

@Suppress("unused")
enum class StatusPesanan(val label: String) {
    MENUNGGU_KONFIRMASI("Menunggu Konfirmasi"),
    DIBUAT("Dibuat"),
    DIANTAR("Diantar"),
    SELESAI("Selesai"),
    DIBATALKAN("Dibatalkan")
}