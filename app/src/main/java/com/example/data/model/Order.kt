package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class OrderStatus(val label: String, val stepIndex: Int, val description: String) {
    PLACED("Placed", 0, "Order received at LocalMart Depot"),
    PACKED("Packed", 1, "Package sorted & sealed at Depot"),
    OUT_FOR_DELIVERY("Out for Delivery", 2, "Driver dispatched on route"),
    DELIVERED("Delivered", 3, "Handed over to customer"),
    CANCELLED("Cancelled", -1, "Order was cancelled")
}

data class OrderItem(
    val productId: String,
    val productTitle: String,
    val unitPrice: Double,
    val quantity: Int,
    val unit: String = "1 kg",
    val imageUrl: String = ""
) {
    val totalItemPrice: Double get() = unitPrice * quantity
}

data class Order(
    val id: String = UUID.randomUUID().toString(),
    val orderNumber: String = "LM-${(1000..9999).random()}",
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryLat: Double,
    val deliveryLng: Double,
    val landmarkName: String = "",
    val items: List<OrderItem>,
    val subtotal: Double,
    val deliveryFee: Double = 25.0,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.PLACED,
    val orderTimestamp: Long = System.currentTimeMillis(),
    val deliveryPromise: String = "1-Day Express Local Delivery",
    val deliveryNotes: String = "",
    val paymentMethod: String = "Cash on Delivery",
    val paymentStatus: String = "Pending on Delivery",
    val distanceKm: Double = 0.0,
    val estimatedMinutes: Int = 30,
    val deliveryPartnerName: String = "Rahul Das (Local Rider #7)",
    val deliveryPartnerPhone: String = "+91 94350 88219",
    val otpCode: String = "${(1000..9999).random()}"
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            return sdf.format(Date(orderTimestamp))
        }

    val totalItemsCount: Int
        get() = items.sumOf { it.quantity }
}
