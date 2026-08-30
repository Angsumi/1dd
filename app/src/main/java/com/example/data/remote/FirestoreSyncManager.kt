package com.example.data.remote

import android.util.Log
import com.example.data.local.MarketDao
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.local.RoomConverters
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class CloudSyncState {
    IDLE,
    SYNCING,
    SYNCED_ONLINE,
    OFFLINE_LOCAL
}

/**
 * Manages bidirectional real-time synchronization between Local Room Database
 * and Cloud Firebase Firestore for Store House products and orders.
 */
class FirestoreSyncManager(
    private val dao: MarketDao,
    private val scope: CoroutineScope
) {
    private val TAG = "FirestoreSyncManager"
    private val converters = RoomConverters()
    private var firestore: FirebaseFirestore? = null

    private var productsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null

    private val _syncState = MutableStateFlow(CloudSyncState.IDLE)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    private val _lastSyncedTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncedTime: StateFlow<Long> = _lastSyncedTime.asStateFlow()

    init {
        initFirestore()
    }

    private fun initFirestore() {
        try {
            firestore = FirebaseFirestore.getInstance()
            _syncState.value = CloudSyncState.SYNCED_ONLINE
            Log.d(TAG, "Firebase Firestore initialized successfully")
            startRealtimeListeners()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization notice: ${e.message}. Using offline-first Room cache.")
            _syncState.value = CloudSyncState.OFFLINE_LOCAL
        }
    }

    fun startRealtimeListeners() {
        val db = firestore ?: return

        // 1. Real-time Products Sync
        try {
            productsListener?.remove()
            productsListener = db.collection("store_products")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(TAG, "Product listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            val products = mutableListOf<ProductEntity>()
                            for (doc in snapshots.documents) {
                                try {
                                    val catStr = doc.getString("category") ?: ProductCategory.VEGETABLES.name
                                    val category = try {
                                        ProductCategory.valueOf(catStr)
                                    } catch (e: Exception) {
                                        ProductCategory.VEGETABLES
                                    }

                                    val entity = ProductEntity(
                                        id = doc.id,
                                        title = doc.getString("title") ?: "Product",
                                        description = doc.getString("description") ?: "",
                                        price = doc.getDouble("price") ?: 0.0,
                                        unit = doc.getString("unit") ?: "1 unit",
                                        category = category,
                                        stockQuantity = (doc.getLong("stockQuantity") ?: 0).toInt(),
                                        imageUrl = doc.getString("imageUrl") ?: "",
                                        isLocalSpecialty = doc.getBoolean("isLocalSpecialty") ?: true,
                                        isOneDayDelivery = doc.getBoolean("isOneDayDelivery") ?: true,
                                        rating = doc.getDouble("rating") ?: 5.0,
                                        reviewCount = (doc.getLong("reviewCount") ?: 0).toInt(),
                                        sellerName = doc.getString("sellerName") ?: "Store House Owner",
                                        isAvailable = doc.getBoolean("isAvailable") ?: true
                                    )
                                    products.add(entity)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing product document: ${e.message}")
                                }
                            }
                            if (products.isNotEmpty()) {
                                dao.insertProducts(products)
                                _lastSyncedTime.value = System.currentTimeMillis()
                                _syncState.value = CloudSyncState.SYNCED_ONLINE
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Could not start products listener: ${e.message}")
        }

        // 2. Real-time Orders Sync
        try {
            ordersListener?.remove()
            ordersListener = db.collection("store_orders")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(TAG, "Orders listener error: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshots != null && !snapshots.isEmpty) {
                        scope.launch(Dispatchers.IO) {
                            val orders = mutableListOf<OrderEntity>()
                            for (doc in snapshots.documents) {
                                try {
                                    val itemsJsonStr = doc.getString("itemsJson") ?: "[]"
                                    val parsedItems = converters.toOrderItems(itemsJsonStr)
                                    val statusStr = doc.getString("status") ?: OrderStatus.PLACED.name

                                    val entity = OrderEntity(
                                        id = doc.id,
                                        orderNumber = doc.getString("orderNumber") ?: "LM-${(1000..9999).random()}",
                                        customerName = doc.getString("customerName") ?: "Customer",
                                        customerPhone = doc.getString("customerPhone") ?: "",
                                        deliveryAddress = doc.getString("deliveryAddress") ?: "",
                                        deliveryLat = doc.getDouble("deliveryLat") ?: 0.0,
                                        deliveryLng = doc.getDouble("deliveryLng") ?: 0.0,
                                        landmarkName = doc.getString("landmarkName") ?: "",
                                        items = parsedItems,
                                        subtotal = doc.getDouble("subtotal") ?: 0.0,
                                        deliveryFee = doc.getDouble("deliveryFee") ?: 25.0,
                                        totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                        status = statusStr,
                                        orderTimestamp = doc.getLong("orderTimestamp") ?: System.currentTimeMillis(),
                                        deliveryPromise = doc.getString("deliveryPromise") ?: "1-Day Express Local Delivery",
                                        deliveryNotes = doc.getString("deliveryNotes") ?: "",
                                        paymentMethod = doc.getString("paymentMethod") ?: "Cash on Delivery",
                                        paymentStatus = doc.getString("paymentStatus") ?: "Pending on Delivery",
                                        distanceKm = doc.getDouble("distanceKm") ?: 0.0,
                                        estimatedMinutes = (doc.getLong("estimatedMinutes") ?: 30).toInt(),
                                        deliveryPartnerName = doc.getString("deliveryPartnerName") ?: "Store House Partner",
                                        deliveryPartnerPhone = doc.getString("deliveryPartnerPhone") ?: "+91 98640 12345",
                                        otpCode = doc.getString("otpCode") ?: "1234"
                                    )
                                    orders.add(entity)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing order document: ${e.message}")
                                }
                            }
                            if (orders.isNotEmpty()) {
                                dao.insertOrders(orders)
                                _lastSyncedTime.value = System.currentTimeMillis()
                                _syncState.value = CloudSyncState.SYNCED_ONLINE
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Could not start orders listener: ${e.message}")
        }
    }

    suspend fun syncProductToCloud(product: Product) {
        val db = firestore ?: return
        try {
            _syncState.value = CloudSyncState.SYNCING
            val productMap = hashMapOf(
                "title" to product.title,
                "description" to product.description,
                "price" to product.price,
                "unit" to product.unit,
                "category" to product.category.name,
                "stockQuantity" to product.stockQuantity,
                "imageUrl" to product.imageUrl,
                "isLocalSpecialty" to product.isLocalSpecialty,
                "isOneDayDelivery" to product.isOneDayDelivery,
                "rating" to product.rating,
                "reviewCount" to product.reviewCount,
                "sellerName" to product.sellerName,
                "isAvailable" to product.isAvailable,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("store_products").document(product.id)
                .set(productMap, SetOptions.merge())
            _syncState.value = CloudSyncState.SYNCED_ONLINE
            _lastSyncedTime.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing product to cloud: ${e.message}")
            _syncState.value = CloudSyncState.OFFLINE_LOCAL
        }
    }

    suspend fun deleteProductFromCloud(productId: String) {
        val db = firestore ?: return
        try {
            db.collection("store_products").document(productId).delete()
        } catch (e: Exception) {
            Log.w(TAG, "Error deleting product from cloud: ${e.message}")
        }
    }

    suspend fun syncOrderToCloud(order: Order) {
        val db = firestore ?: return
        try {
            _syncState.value = CloudSyncState.SYNCING
            val itemsJsonArray = JSONArray()
            order.items.forEach { item ->
                val obj = JSONObject().apply {
                    put("productId", item.productId)
                    put("productTitle", item.productTitle)
                    put("unitPrice", item.unitPrice)
                    put("quantity", item.quantity)
                    put("unit", item.unit)
                    put("imageUrl", item.imageUrl)
                }
                itemsJsonArray.put(obj)
            }

            val orderMap = hashMapOf(
                "orderNumber" to order.orderNumber,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "deliveryAddress" to order.deliveryAddress,
                "deliveryLat" to order.deliveryLat,
                "deliveryLng" to order.deliveryLng,
                "landmarkName" to order.landmarkName,
                "itemsJson" to itemsJsonArray.toString(),
                "subtotal" to order.subtotal,
                "deliveryFee" to order.deliveryFee,
                "totalAmount" to order.totalAmount,
                "status" to order.status.name,
                "orderTimestamp" to order.orderTimestamp,
                "deliveryPromise" to order.deliveryPromise,
                "deliveryNotes" to order.deliveryNotes,
                "paymentMethod" to order.paymentMethod,
                "paymentStatus" to order.paymentStatus,
                "distanceKm" to order.distanceKm,
                "estimatedMinutes" to order.estimatedMinutes,
                "deliveryPartnerName" to order.deliveryPartnerName,
                "deliveryPartnerPhone" to order.deliveryPartnerPhone,
                "otpCode" to order.otpCode,
                "syncedAt" to System.currentTimeMillis()
            )

            db.collection("store_orders").document(order.id)
                .set(orderMap, SetOptions.merge())
            _syncState.value = CloudSyncState.SYNCED_ONLINE
            _lastSyncedTime.value = System.currentTimeMillis()
        } catch (e: Exception) {
            Log.w(TAG, "Error syncing order to cloud: ${e.message}")
            _syncState.value = CloudSyncState.OFFLINE_LOCAL
        }
    }

    suspend fun updateOrderStatusInCloud(orderId: String, newStatus: OrderStatus) {
        val db = firestore ?: return
        try {
            db.collection("store_orders").document(orderId)
                .update(
                    mapOf(
                        "status" to newStatus.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
        } catch (e: Exception) {
            Log.w(TAG, "Error updating order status in cloud: ${e.message}")
        }
    }

    fun cleanup() {
        productsListener?.remove()
        ordersListener?.remove()
    }
}
