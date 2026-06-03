package com.zayyan.dormfood.ui.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.zayyan.dormfood.ui.model.OrderItem
import com.zayyan.dormfood.ui.model.StatusPesanan
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object RekapPdfHelper {

    fun cetakRekap(
        context: Context,
        orders: List<OrderItem>,
        periodeLabel: String = "Semua Waktu"
    ) {
        val completedOrders = orders.filter {
            it.statusPesanan == StatusPesanan.SELESAI.label
        }

        val html = buildHtml(completedOrders, periodeLabel)

        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val jobName = "Rekap_Penjualan_${System.currentTimeMillis()}"
                val printAdapter = view.createPrintDocumentAdapter(jobName)
                printManager.print(
                    jobName,
                    printAdapter,
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                )
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun buildHtml(orders: List<OrderItem>, periodeLabel: String): String {
        val rupiahFmt = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val dateFmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

        val totalPendapatan = orders.sumOf { it.totalHarga }
        val totalMenu = orders.sumOf { order -> order.items.sumOf { it.quantity } }

        // Ringkasan per menu
        val menuMap = mutableMapOf<String, Pair<Int, Long>>() // nama -> (qty, revenue)
        orders.forEach { order ->
            order.items.forEach { item ->
                val current = menuMap[item.namaMenu] ?: Pair(0, 0L)
                menuMap[item.namaMenu] = Pair(
                    current.first + item.quantity,
                    current.second + (item.harga.toLong() * item.quantity)
                )
            }
        }
        val sortedMenu = menuMap.entries.sortedByDescending { it.value.first }

        val rowsTransaksi = orders.joinToString("") { order ->
            val tanggal = order.createdAt?.toDate()?.let { dateFmt.format(it) } ?: "-"
            val namaItems = order.items.joinToString(", ") { "${it.namaMenu} x${it.quantity}" }
            """
            <tr>
                <td>${order.namaUser}</td>
                <td>${order.building} - ${order.roomNumber}</td>
                <td>$namaItems</td>
                <td style="text-align:right">${rupiahFmt.format(order.totalHarga)}</td>
                <td>$tanggal</td>
            </tr>
            """
        }

        val rowsMenu = sortedMenu.joinToString("") { (nama, data) ->
            """
            <tr>
                <td>$nama</td>
                <td style="text-align:center">${data.first}</td>
                <td style="text-align:right">${rupiahFmt.format(data.second)}</td>
            </tr>
            """
        }

        val now = dateFmt.format(Date())

        return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: Arial, sans-serif; font-size: 12px; color: #222; margin: 24px; }
            h1 { font-size: 18px; color: #C62828; margin-bottom: 4px; }
            .subtitle { color: #666; font-size: 12px; margin-bottom: 20px; }
            .summary-box { display: flex; gap: 16px; margin-bottom: 20px; }
            .card { background: #FFF3F3; border-left: 4px solid #C62828; padding: 10px 16px; border-radius: 6px; flex: 1; }
            .card .label { font-size: 11px; color: #888; }
            .card .value { font-size: 16px; font-weight: bold; color: #C62828; margin-top: 2px; }
            h2 { font-size: 14px; border-bottom: 1px solid #eee; padding-bottom: 6px; margin-top: 24px; }
            table { width: 100%; border-collapse: collapse; margin-top: 8px; }
            th { background: #C62828; color: white; padding: 8px; text-align: left; font-size: 11px; }
            td { padding: 7px 8px; border-bottom: 1px solid #eee; font-size: 11px; }
            tr:nth-child(even) { background: #FAFAFA; }
            .footer { margin-top: 32px; font-size: 10px; color: #aaa; text-align: right; }
        </style>
        </head>
        <body>
            <h1>📋 Rekap Penjualan</h1>
            <div class="subtitle">Periode: $periodeLabel &nbsp;|&nbsp; Dicetak: $now</div>

            <div class="summary-box">
                <div class="card">
                    <div class="label">Total Transaksi Selesai</div>
                    <div class="value">${orders.size} pesanan</div>
                </div>
                <div class="card">
                    <div class="label">Total Menu Terjual</div>
                    <div class="value">$totalMenu item</div>
                </div>
                <div class="card">
                    <div class="label">Total Pendapatan</div>
                    <div class="value">${rupiahFmt.format(totalPendapatan)}</div>
                </div>
            </div>

            <h2>📊 Rekap Per Menu</h2>
            <table>
                <thead><tr><th>Nama Menu</th><th style="text-align:center">Qty Terjual</th><th style="text-align:right">Total Pendapatan</th></tr></thead>
                <tbody>$rowsMenu</tbody>
            </table>

            <h2>🧾 Detail Transaksi</h2>
            <table>
                <thead><tr><th>Nama Pemesan</th><th>Kamar</th><th>Pesanan</th><th style="text-align:right">Total</th><th>Waktu</th></tr></thead>
                <tbody>$rowsTransaksi</tbody>
            </table>

            <div class="footer">DormFood &mdash; Dokumen ini digenerate otomatis</div>
        </body>
        </html>
        """.trimIndent()
    }
}