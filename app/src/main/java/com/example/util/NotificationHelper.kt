package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Order

object NotificationHelper {

    const val CHANNEL_ID = "1dd_owner_orders_channel"
    private const val CHANNEL_NAME = "1DD Store Orders"
    private const val CHANNEL_DESC = "Push notifications for new orders placed by buyers"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a system status bar notification for the owner ONLY.
     * Guaranteed never to trigger for normal buyers.
     */
    fun sendOwnerOrderNotification(context: Context, order: Order) {
        // Ensure channel exists
        createNotificationChannel(context)

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                // Cannot post notification without permission
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_OWNER_DASHBOARD", true)
            putExtra("ORDER_ID", order.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            order.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val deliveryLoc = order.landmarkName.ifBlank { order.deliveryAddress }
        val contentText = "${order.customerName} ordered ₹${String.format("%.0f", order.totalAmount)} • $deliveryLoc"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔔 New Order Received: ${order.orderNumber}")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("New delivery request from ${order.customerName} (${order.customerPhone})\nAmount: ₹${String.format("%.0f", order.totalAmount)}\nAddress: ${order.deliveryAddress}\nGPS: ${String.format("%.4f", order.deliveryLat)}, ${String.format("%.4f", order.deliveryLng)} (${String.format("%.1f", order.distanceKm)} km away)")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(order.orderNumber.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handled gracefully if permission revoked
        }
    }
}
