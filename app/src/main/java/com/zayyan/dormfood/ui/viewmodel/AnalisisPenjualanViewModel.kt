package com.zayyan.dormfood.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zayyan.dormfood.ui.model.MenuItem
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.model.RingkasanPenjualan
import com.zayyan.dormfood.ui.utils.DataStoreManager
import kotlinx.coroutines.launch

class AnalisisPenjualanViewModel : ViewModel() {

    enum class Periode {
        HARI_7,
        HARI_30,
        SEMUA
    }

    data class DataPointGrafik(
        val label: String,
        val pendapatan: Long,
        val jumlahPesanan: Int
    )


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
    ): RingkasanPenjualan {

        val stats = getMenuStats(orders)

        return RingkasanPenjualan(
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

    fun getDataGrafik(
        orders: List<OrderItem>,
        periode: Periode
    ): List<DataPointGrafik> {
        if (orders.isEmpty()) return emptyList()

        return when (periode) {
            Periode.HARI_7 -> {
                java.util.Calendar.getInstance()
                (6 downTo 0).map { daysAgo ->
                    val cal = java.util.Calendar.getInstance().apply {
                        add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
                    }
                    val dayLabel = android.text.format.DateFormat.format("EEE", cal.time).toString()
                    val startOfDay = cal.apply {
                        set(java.util.Calendar.HOUR_OF_DAY, 0)
                        set(java.util.Calendar.MINUTE, 0)
                        set(java.util.Calendar.SECOND, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val endOfDay = startOfDay + 86_400_000L

                    val dayOrders = orders.filter { o ->
                        val t = o.createdAt?.toDate()?.time ?: return@filter false
                        t in startOfDay until endOfDay
                    }
                    DataPointGrafik(
                        label = dayLabel,
                        pendapatan = dayOrders.sumOf { it.totalHarga },
                        jumlahPesanan = dayOrders.size
                    )
                }
            }
            Periode.HARI_30 -> {
                (3 downTo 0).map { weeksAgo ->
                    val startMs = System.currentTimeMillis() - (weeksAgo + 1) * 7L * 86_400_000
                    val endMs   = System.currentTimeMillis() - weeksAgo * 7L * 86_400_000
                    val weekOrders = orders.filter { o ->
                        val t = o.createdAt?.toDate()?.time ?: return@filter false
                        t in startMs until endMs
                    }
                    DataPointGrafik(
                        label = "Minggu ${4 - weeksAgo}",
                        pendapatan = weekOrders.sumOf { it.totalHarga },
                        jumlahPesanan = weekOrders.size
                    )
                }
            }
            Periode.SEMUA -> {
                val grouped = orders.groupBy { o ->
                    val cal = java.util.Calendar.getInstance().apply {
                        time = o.createdAt?.toDate() ?: java.util.Date()
                    }
                    "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH)}"
                }
                grouped.entries
                    .sortedBy { it.key }
                    .takeLast(6)
                    .map { (key, monthOrders) ->
                        val parts = key.split("-")
                        val cal = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.YEAR, parts[0].toInt())
                            set(java.util.Calendar.MONTH, parts[1].toInt())
                        }
                        DataPointGrafik(
                            label = android.text.format.DateFormat.format("MMM", cal.time).toString(),
                            pendapatan = monthOrders.sumOf { it.totalHarga },
                            jumlahPesanan = monthOrders.size
                        )
                    }
            }
        }
    }

    fun toggleShowGrafik(context: android.content.Context, currentValue: Boolean) {
        viewModelScope.launch {
            DataStoreManager.setShowGrafik(context, !currentValue)
        }
    }
}