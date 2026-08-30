package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val deliveryLat: Double,
    val deliveryLng: Double,
    val landmarkName: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val status: String,
    val orderTimestamp: Long,
    val deliveryPromise: String,
    val deliveryNotes: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val deliveryPartnerName: String,
    val deliveryPartnerPhone: String,
    val otpCode: String
) {
    fun toDomainModel(): Order = Order(
        id = id,
        orderNumber = orderNumber,
        customerName = customerName,
        customerPhone = customerPhone,
        deliveryAddress = deliveryAddress,
        deliveryLat = deliveryLat,
        deliveryLng = deliveryLng,
        landmarkName = landmarkName,
        items = items,
        subtotal = subtotal,
        deliveryFee = deliveryFee,
        totalAmount = totalAmount,
        status = try { OrderStatus.valueOf(status) } catch (e: Exception) { OrderStatus.PLACED },
        orderTimestamp = orderTimestamp,
        deliveryPromise = deliveryPromise,
        deliveryNotes = deliveryNotes,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus,
        distanceKm = distanceKm,
        estimatedMinutes = estimatedMinutes,
        deliveryPartnerName = deliveryPartnerName,
        deliveryPartnerPhone = deliveryPartnerPhone,
        otpCode = otpCode
    )

    companion object {
        fun fromDomainModel(order: Order): OrderEntity = OrderEntity(
            id = order.id,
            orderNumber = order.orderNumber,
            customerName = order.customerName,
            customerPhone = order.customerPhone,
            deliveryAddress = order.deliveryAddress,
            deliveryLat = order.deliveryLat,
            deliveryLng = order.deliveryLng,
            landmarkName = order.landmarkName,
            items = order.items,
            subtotal = order.subtotal,
            deliveryFee = order.deliveryFee,
            totalAmount = order.totalAmount,
            status = order.status.name,
            orderTimestamp = order.orderTimestamp,
            deliveryPromise = order.deliveryPromise,
            deliveryNotes = order.deliveryNotes,
            paymentMethod = order.paymentMethod,
            paymentStatus = order.paymentStatus,
            distanceKm = order.distanceKm,
            estimatedMinutes = order.estimatedMinutes,
            deliveryPartnerName = order.deliveryPartnerName,
            deliveryPartnerPhone = order.deliveryPartnerPhone,
            otpCode = order.otpCode
        )
    }
}
