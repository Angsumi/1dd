package com.example.data.repository

import com.example.data.local.MarketDao
import com.example.data.local.OrderEntity
import com.example.data.local.ProductEntity
import com.example.data.model.HaversineCalculator
import com.example.data.model.LocationConstants
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.PRESET_LOCAL_DESTINATIONS
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.remote.CloudSyncState
import com.example.data.remote.FirestoreSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class MarketRepository(
    private val dao: MarketDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val syncManager = FirestoreSyncManager(dao, scope)
    val syncState: StateFlow<CloudSyncState> = syncManager.syncState
    val lastSyncedTime: StateFlow<Long> = syncManager.lastSyncedTime

    val allProducts: Flow<List<Product>> = dao.getAllProducts().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val allOrders: Flow<List<Order>> = dao.getAllOrders().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun initializeSeedDataIfEmpty() {
        // Database initializes empty for direct owner management.
    }

    suspend fun seedSampleOwnerItems() {
        val sampleProducts = listOf(
            Product(
                id = "item_1",
                title = "Fresh Organic Spinach (Palak)",
                description = "Crisp, fresh organic spinach leaves stored at Store House (26.838775, 92.910579).",
                price = 40.0,
                unit = "500g bunch",
                category = ProductCategory.VEGETABLES,
                stockQuantity = 20,
                imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&auto=format&fit=crop&q=80",
                isLocalSpecialty = true,
                isOneDayDelivery = true,
                rating = 5.0,
                reviewCount = 12,
                sellerName = "Store House Owner"
            ),
            Product(
                id = "item_2",
                title = "Assam Joha Aromatic Rice",
                description = "Premium heritage fragrant Joha rice stored at Store House.",
                price = 190.0,
                unit = "2 kg bag",
                category = ProductCategory.LOCAL_SPECIALS,
                stockQuantity = 30,
                imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop&q=80",
                isLocalSpecialty = true,
                isOneDayDelivery = true,
                rating = 5.0,
                reviewCount = 28,
                sellerName = "Store House Owner"
            ),
            Product(
                id = "item_3",
                title = "Fresh Pure Cow Milk",
                description = "100% pure pasteurized fresh milk bottled and stored cold.",
                price = 65.0,
                unit = "1 Litre bottle",
                category = ProductCategory.DAIRY_BAKERY,
                stockQuantity = 25,
                imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80",
                isLocalSpecialty = true,
                isOneDayDelivery = true,
                rating = 4.9,
                reviewCount = 45,
                sellerName = "Store House Owner"
            )
        )
        dao.insertProducts(sampleProducts.map { ProductEntity.fromDomainModel(it) })
        sampleProducts.forEach { syncManager.syncProductToCloud(it) }
    }

    suspend fun saveProduct(product: Product) {
        dao.insertProduct(ProductEntity.fromDomainModel(product))
        syncManager.syncProductToCloud(product)
    }

    suspend fun updateStock(productId: String, stock: Int) {
        dao.updateStock(productId, stock)
        val entity = dao.getProductById(productId)
        if (entity != null) {
            syncManager.syncProductToCloud(entity.toDomainModel())
        }
    }

    suspend fun updateAvailability(productId: String, isAvailable: Boolean) {
        dao.updateAvailability(productId, isAvailable)
        val entity = dao.getProductById(productId)
        if (entity != null) {
            syncManager.syncProductToCloud(entity.toDomainModel())
        }
    }

    suspend fun deleteProduct(productId: String) {
        dao.deleteProductById(productId)
        syncManager.deleteProductFromCloud(productId)
    }

    suspend fun createOrder(order: Order): Order {
        val distance = if (order.distanceKm <= 0.0 && order.deliveryLat != 0.0) {
            HaversineCalculator.calculateDistanceFromStore(order.deliveryLat, order.deliveryLng)
        } else {
            order.distanceKm
        }
        val estimatedMin = HaversineCalculator.estimateDeliveryMinutes(distance)
        val finalOrder = order.copy(
            distanceKm = distance,
            estimatedMinutes = estimatedMin
        )
        dao.insertOrder(OrderEntity.fromDomainModel(finalOrder))

        // Sync to cloud Firestore
        syncManager.syncOrderToCloud(finalOrder)

        // Decrement product inventory
        for (item in finalOrder.items) {
            val entity = dao.getProductById(item.productId)
            if (entity != null) {
                val updatedStock = (entity.stockQuantity - item.quantity).coerceAtLeast(0)
                dao.updateStock(item.productId, updatedStock)
                syncManager.syncProductToCloud(entity.toDomainModel().copy(stockQuantity = updatedStock))
            }
        }

        return finalOrder
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        dao.updateOrderStatus(orderId, newStatus.name)
        syncManager.updateOrderStatusInCloud(orderId, newStatus)
    }

    suspend fun deleteOrder(orderId: String) {
        dao.deleteOrder(orderId)
        syncManager.triggerManualSync()
    }

    fun triggerManualSync() {
        syncManager.triggerManualSync()
    }

    /**
     * Generates a JSON export string formatted for Firebase Firestore / Web Vercel/Netlify hosting
     */
    suspend fun exportJsonDataForCloud(): String {
        val products = dao.getAllProducts().firstOrNull() ?: emptyList()
        val orders = dao.getAllOrders().firstOrNull() ?: emptyList()

        val root = JSONObject()
        root.put("storeHub", JSONObject().apply {
            put("name", LocationConstants.STORE_HUB_NAME)
            put("latitude", LocationConstants.STORE_ORIGIN_LAT)
            put("longitude", LocationConstants.STORE_ORIGIN_LNG)
            put("address", LocationConstants.STORE_HUB_ADDRESS)
            put("phone", LocationConstants.STORE_PHONE)
            put("maxRadiusKm", LocationConstants.MAX_LOCAL_DELIVERY_RADIUS_KM)
            put("guarantee", "1-Day Local Delivery")
        })

        val productsArray = JSONArray()
        for (p in products) {
            productsArray.put(JSONObject().apply {
                put("id", p.id)
                put("title", p.title)
                put("description", p.description)
                put("price", p.price)
                put("unit", p.unit)
                put("category", p.category.name)
                put("stockQuantity", p.stockQuantity)
                put("imageUrl", p.imageUrl)
                put("isLocalSpecialty", p.isLocalSpecialty)
                put("isOneDayDelivery", p.isOneDayDelivery)
                put("sellerName", p.sellerName)
                put("isAvailable", p.isAvailable)
            })
        }
        root.put("products", productsArray)

        val ordersArray = JSONArray()
        for (o in orders) {
            ordersArray.put(JSONObject().apply {
                put("id", o.id)
                put("orderNumber", o.orderNumber)
                put("customerName", o.customerName)
                put("customerPhone", o.customerPhone)
                put("deliveryAddress", o.deliveryAddress)
                put("deliveryLat", o.deliveryLat)
                put("deliveryLng", o.deliveryLng)
                put("distanceKm", o.distanceKm)
                put("totalAmount", o.totalAmount)
                put("status", o.status)
                put("orderTimestamp", o.orderTimestamp)
            })
        }
        root.put("orders", ordersArray)

        return root.toString(2)
    }
}
