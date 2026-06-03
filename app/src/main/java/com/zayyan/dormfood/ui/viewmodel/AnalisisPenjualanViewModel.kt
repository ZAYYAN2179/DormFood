package com.zayyan.dormfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zayyan.dormfood.ui.model.MenuItem
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.model.RingkasanPenjulan

class AnalisisPenjualanViewModel : ViewModel() {

    enum class Periode {
        HARI_7,
        HARI_30,
        SEMUA
    }

    fun filterOrders(
        orders: List<OrderItem>,
        periode: Periode
    ): List<OrderItem> {
        val now = System.currentTimeMillis()
        return orders.filter { order ->
            val createdAt = order.createdAt?.toDate()?.time ?: return@filter false
            when (periode) {
                Periode.HARI_7 -> now - createdAt <= 7L * 86_400_000
                Periode.HARI_30 -> now - createdAt <= 30L * 86_400_000
                Periode.SEMUA -> true
            }
        }
    }

    private fun getMenuStats(orders: List<OrderItem>): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        orders.forEach { order ->
            order.items.forEach { item ->
                map[item.namaMenu] = (map[item.namaMenu] ?: 0) + item.quantity
            }
        }
        return map
    }



    fun getRingkasanPrediksi(
        orders: List<OrderItem>,
        allMenus: List<MenuItem>
    ): RingkasanPenjulan {

        val stats = getMenuStats(orders)

        return RingkasanPenjulan(
            totalPesanan = orders.size,
            totalMenu = allMenus.size,
            menuTerjual = stats.size,
            menuBelumTerjual = allMenus.count {
                (stats[it.namaMenu] ?: 0) == 0
            }
        )
    }

    fun getInsight(
        orders: List<OrderItem>,
        allMenus: List<MenuItem>
    ): List<String> {

        val result = mutableListOf<String>()

        val terlaris =
            getMenuTerlaris(orders)
                .firstOrNull()

        if (terlaris != null) {
            result.add(
                "${terlaris.first} adalah menu terlaris dengan ${terlaris.second} penjualan"
            )
        }

        val belumLaku =
            getMenuKurangLaris(
                orders,
                allMenus
            )

        if (belumLaku.isNotEmpty()) {
            result.add(
                "${belumLaku.size} menu belum pernah dipesan"
            )
        }
        return result
    }

    fun getMenuTerlaris(orders: List<OrderItem>): List<Pair<String, Int>> =
        getMenuStats(orders)
            .toList()
            .sortedByDescending { it.second }
            .take(5)

    fun getMenuKurangLaris(
        orders: List<OrderItem>,
        allMenus: List<MenuItem>
    ): List<Pair<String, Int>> {

        val stats = getMenuStats(orders)

        return allMenus
            .map { menu ->
                menu.namaMenu to (stats[menu.namaMenu] ?: 0)
            }
            .filter { it.second == 0 }
            .sortedBy { it.first }
    }
}