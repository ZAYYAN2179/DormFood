package com.zayyan.dormfood.ui.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.zayyan.dormfood.MainActivity
import com.zayyan.dormfood.R

object NotificationHelper {

    const val CHANNEL_ID = "dormfood_orders"
    private const val CHANNEL_NAME = "Status Pesanan"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi update status pesanan makanan"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showOrderStatusNotification(
        context: Context,
        namaWarung: String,
        statusBaru: String
    ) {
        val bodyMap = mapOf(
            "Dibuat" to "Pesananmu di $namaWarung sedang diproses! 🍽️",
            "Diantar" to "Pesananmu dari $namaWarung sedang dalam perjalanan! 🛵",
            "Selesai" to "Pesananmu dari $namaWarung telah tiba. Selamat makan! ✅",
            "Menunggu Konfirmasi" to "Pesananmu di $namaWarung menunggu konfirmasi."
        )

        val body = bodyMap[statusBaru]
            ?: "Status pesananmu di $namaWarung diperbarui: $statusBaru"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Update Pesanan DormFood")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showNewOrderNotification(
        context: Context,
        namaUser: String,
        totalHarga: Long
    ) {
        val body = "Pesanan baru dari $namaUser masuk! Total: Rp${totalHarga}. Segera konfirmasi. 🔔"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Pesanan Baru Masuk!")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showOrderRejectedNotification(
        context: Context,
        namaWarung: String,
        alasan: String = ""
    ) {
        val body = if (alasan.isNotBlank()) {
            "Pesananmu di $namaWarung ditolak. Alasan: \"$alasan\" ❌"
        } else {
            "Maaf, pesananmu di $namaWarung ditolak oleh warung. Silakan coba lagi. ❌"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Pesanan Ditolak")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}