package com.zayyan.dormfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.zayyan.dormfood.ui.model.OrderItem

class PrediksiViewModel: ViewModel() {

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

            val createdAt =
                order.createdAt?.toDate()?.time
                    ?: return@filter false

            when (periode) {

                Periode.HARI_7 ->
                    now - createdAt <= 7L * 24 * 60 * 60 * 1000

                Periode.HARI_30 ->
                    now - createdAt <= 30L * 24 * 60 * 60 * 1000

                Periode.SEMUA ->
                    true
            }
        }
    }

    fun getMenuTerlaris(
        orders: List<OrderItem>
    ): List<Pair<String, Int>> {

        return getMenuStats(orders)
            .toList()
            .sortedByDescending { it.second }
            .take(3)
    }

    fun getMenuKurangLaris(
        orders: List<OrderItem>
    ): List<Pair<String, Int>> {

        return getMenuStats(orders)
            .toList()
            .sortedBy { it.second }
            .take(3)
    }

    fun getRekomendasiStok(
        orders: List<OrderItem>,
        periode: Periode
    ): List<Pair<String, Int>> {

        val rataRataPenjualan =
            getRataRataPenjualanHarian(
                orders,
                periode
            )

        return rataRataPenjualan.map { (menu, rataRata) ->

            val rekomendasi =
                kotlin.math.ceil(
                    rataRata * 1.2
                ).toInt()

            menu to rekomendasi
        }
            .sortedByDescending { it.second }
    }

    fun getRataRataPenjualanHarian(
        orders: List<OrderItem>,
        periode: Periode
    ): List<Pair<String, Double>> {

        val menuMap = mutableMapOf<String, Int>()

        orders.forEach { order ->
            order.items.forEach { item ->

                menuMap[item.namaMenu] =
                    (menuMap[item.namaMenu] ?: 0) + item.quantity
            }
        }

        val jumlahHari = when (periode) {
            Periode.HARI_7 -> 7
            Periode.HARI_30 -> 30
            Periode.SEMUA -> {
                val minDate = orders.minOfOrNull {
                    it.createdAt?.toDate()?.time ?: Long.MAX_VALUE
                } ?: Long.MAX_VALUE

                val maxDate = orders.maxOfOrNull {
                    it.createdAt?.toDate()?.time ?: Long.MIN_VALUE
                } ?: Long.MIN_VALUE

                (((maxDate - minDate) / (1000 * 60 * 60 * 24)) + 1)
                    .coerceAtLeast(1)
                    .toInt()
            }
        }

        return menuMap.map { (menu, totalTerjual) ->
            menu to (totalTerjual.toDouble() / jumlahHari)
        }
            .sortedByDescending { it.second }
    }

    private fun getMenuStats(
        orders: List<OrderItem>
    ): Map<String, Int> {

        val menuMap = mutableMapOf<String, Int>()

        orders.forEach { order ->
            order.items.forEach { item ->
                menuMap[item.namaMenu] =
                    (menuMap[item.namaMenu] ?: 0) + item.quantity
            }
        }

        return menuMap
    }
}
