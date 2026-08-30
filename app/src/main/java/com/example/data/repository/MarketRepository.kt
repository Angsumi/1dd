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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class MarketRepository(private val dao: MarketDao) {

    val allProducts: Flow<List<Product>> = dao.getAllProducts().map { entities ->
        entities.map { it.toDomainModel() }
    }

    val allOrders: Flow<List<Order>> = dao.getAllOrders().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun initializeSeedDataIfEmpty() {
        val existingProducts = dao.getAllProducts().firstOrNull()
        if (existingProducts.isNullOrEmpty()) {
            val seedProducts = listOf(
                Product(
                    id = "prod_1",
                    title = "Fresh Organic Spinach (Palak)",
                    description = "Freshly harvested chemical-free crisp green spinach leaves from local organic farms.",
                    price = 40.0,
                    unit = "500g bunch",
                    category = ProductCategory.VEGETABLES,
                    stockQuantity = 25,
                    imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = true,
                    isOneDayDelivery = true,
                    rating = 4.9,
                    reviewCount = 52,
                    sellerName = "Green Valley Local Farm"
                ),
                Product(
                    id = "prod_2",
                    title = "Assam Joha Aromatic Rice",
                    description = "Heritage scented small-grain Joha rice, harvested locally. Unmatched aroma and tender texture.",
                    price = 190.0,
                    unit = "2 kg bag",
                    category = ProductCategory.LOCAL_SPECIALS,
                    stockQuantity = 40,
                    imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = true,
                    isOneDayDelivery = true,
                    rating = 5.0,
                    reviewCount = 89,
                    sellerName = "Brahmaputra Agro Co-op"
                ),
                Product(
                    id = "prod_3",
                    title = "Tezpur King Chilli (Bhut Jolokia)",
                    description = "World famous authentic sun-ripened hot Naga King chili with distinct aroma and fiery punch.",
                    price = 120.0,
                    unit = "250g pack",
                    category = ProductCategory.LOCAL_SPECIALS,
                    stockQuantity = 18,
                    imageUrl = "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = true,
                    isOneDayDelivery = true,
                    rating = 4.8,
                    reviewCount = 64,
                    sellerName = "Hilltop Spice Growers"
                ),
                Product(
                    id = "prod_4",
                    title = "Farm Fresh Country Eggs",
                    description = "Free-range country brown eggs collected every morning from cruelty-free local poultry.",
                    price = 110.0,
                    unit = "Tray of 12",
                    category = ProductCategory.DAIRY_BAKERY,
                    stockQuantity = 30,
                    imageUrl = "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = false,
                    isOneDayDelivery = true,
                    rating = 4.7,
                    reviewCount = 41,
                    sellerName = "Sunshine Country Poultry"
                ),
                Product(
                    id = "prod_5",
                    title = "Fresh Morning Pure Cow Milk",
                    description = "100% pure pasteurized unadulterated whole cow milk in eco-friendly glass bottles.",
                    price = 65.0,
                    unit = "1 Litre bottle",
                    category = ProductCategory.DAIRY_BAKERY,
                    stockQuantity = 50,
                    imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = true,
                    isOneDayDelivery = true,
                    rating = 4.9,
                    reviewCount = 110,
                    sellerName = "Dairy Bliss Farms"
                ),
                Product(
                    id = "prod_6",
                    title = "Farm Vine-Ripened Tomatoes",
                    description = "Juicy, firm and sweet locally grown red tomatoes. Perfect for curries and fresh salads.",
                    price = 35.0,
                    unit = "1 kg",
                    category = ProductCategory.VEGETABLES,
                    stockQuantity = 45,
                    imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = false,
                    isOneDayDelivery = true,
                    rating = 4.6,
                    reviewCount = 38,
                    sellerName = "Green Valley Local Farm"
                ),
                Product(
                    id = "prod_7",
                    title = "Handcrafted Assam CTC Gold Tea",
                    description = "First flush golden tip CTC tea with strong malt flavor and bright liquor.",
                    price = 240.0,
                    unit = "250g tin",
                    category = ProductCategory.SNACKS_BEVERAGES,
                    stockQuantity = 35,
                    imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = true,
                    isOneDayDelivery = true,
                    rating = 5.0,
                    reviewCount = 145,
                    sellerName = "Tezpur Heritage Estate"
                ),
                Product(
                    id = "prod_8",
                    title = "Raw Unprocessed Wild Forest Honey",
                    description = "Filtered raw honey collected from local natural flora. Rich in pollen and natural enzymes.",
                    price = 320.0,
                    unit = "500g jar",
                    category = ProductCategory.LOCAL_SPECIALS,
                    stockQuantity = 15,
                    imageUrl = "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = true,
                    isOneDayDelivery = true,
                    rating = 4.9,
                    reviewCount = 76,
                    sellerName = "Wild Flora Apiaries"
                ),
                Product(
                    id = "prod_9",
                    title = "Organic Malbhog Bananas",
                    description = "Sweet, fragrant locally famous Malbhog table bananas ripened naturally without carbides.",
                    price = 60.0,
                    unit = "1 Dozen",
                    category = ProductCategory.FRUITS,
                    stockQuantity = 22,
                    imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = true,
                    isOneDayDelivery = true,
                    rating = 4.8,
                    reviewCount = 30,
                    sellerName = "Brahmaputra Agro Co-op"
                ),
                Product(
                    id = "prod_10",
                    title = "Cold Pressed Mustard Oil (Kachi Ghani)",
                    description = "Traditional stone-pressed pure pungent mustard oil for authentic homestyle cooking.",
                    price = 195.0,
                    unit = "1 Litre bottle",
                    category = ProductCategory.GRAINS_SPICES,
                    stockQuantity = 28,
                    imageUrl = "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=600&auto=format&fit=crop&q=80",
                    isLocalSpecialty = true,
                    isOneDayDelivery = true,
                    rating = 4.9,
                    reviewCount = 63,
                    sellerName = "Brahmaputra Agro Co-op"
                )
            )
            dao.insertProducts(seedProducts.map { ProductEntity.fromDomainModel(it) })
        }

        val existingOrders = dao.getAllOrders().firstOrNull()
        if (existingOrders.isNullOrEmpty()) {
            val sampleDestination1 = PRESET_LOCAL_DESTINATIONS[0]
            val distance1 = HaversineCalculator.calculateDistanceFromStore(
                sampleDestination1.latitude,
                sampleDestination1.longitude
            )
            val sampleOrder1 = Order(
                id = "order_101",
                orderNumber = "LM-8421",
                customerName = "Ananya Sharma",
                customerPhone = "+91 98540 11223",
                deliveryAddress = "Flat 4A, Green Meadows, Near Mission Chariali",
                deliveryLat = sampleDestination1.latitude,
                deliveryLng = sampleDestination1.longitude,
                landmarkName = sampleDestination1.name,
                items = listOf(
                    OrderItem(
                        productId = "prod_1",
                        productTitle = "Fresh Organic Spinach (Palak)",
                        unitPrice = 40.0,
                        quantity = 2,
                        unit = "500g bunch",
                        imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600&auto=format&fit=crop&q=80"
                    ),
                    OrderItem(
                        productId = "prod_5",
                        productTitle = "Fresh Morning Pure Cow Milk",
                        unitPrice = 65.0,
                        quantity = 2,
                        unit = "1 Litre bottle",
                        imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600&auto=format&fit=crop&q=80"
                    )
                ),
                subtotal = 210.0,
                deliveryFee = 25.0,
                totalAmount = 235.0,
                status = OrderStatus.OUT_FOR_DELIVERY,
                orderTimestamp = System.currentTimeMillis() - 45 * 60 * 1000,
                deliveryPromise = "1-Day Express Local Delivery",
                deliveryNotes = "Please ring the bell twice and leave near doorstep.",
                paymentMethod = "Cash on Delivery",
                paymentStatus = "Collect on Delivery",
                distanceKm = distance1,
                estimatedMinutes = HaversineCalculator.estimateDeliveryMinutes(distance1),
                deliveryPartnerName = "Bikram Borah (Rider #3)",
                deliveryPartnerPhone = "+91 94350 99881",
                otpCode = "5824"
            )

            val sampleDestination2 = PRESET_LOCAL_DESTINATIONS[3]
            val distance2 = HaversineCalculator.calculateDistanceFromStore(
                sampleDestination2.latitude,
                sampleDestination2.longitude
            )
            val sampleOrder2 = Order(
                id = "order_102",
                orderNumber = "LM-8422",
                customerName = "Debojit Kalita",
                customerPhone = "+91 97060 44556",
                deliveryAddress = "House #23, Tribeni MG Road Commercial Zone",
                deliveryLat = sampleDestination2.latitude,
                deliveryLng = sampleDestination2.longitude,
                landmarkName = sampleDestination2.name,
                items = listOf(
                    OrderItem(
                        productId = "prod_2",
                        productTitle = "Assam Joha Aromatic Rice",
                        unitPrice = 190.0,
                        quantity = 1,
                        unit = "2 kg bag",
                        imageUrl = "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop&q=80"
                    ),
                    OrderItem(
                        productId = "prod_3",
                        productTitle = "Tezpur King Chilli (Bhut Jolokia)",
                        unitPrice = 120.0,
                        quantity = 1,
                        unit = "250g pack",
                        imageUrl = "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=600&auto=format&fit=crop&q=80"
                    ),
                    OrderItem(
                        productId = "prod_7",
                        productTitle = "Handcrafted Assam CTC Gold Tea",
                        unitPrice = 240.0,
                        quantity = 1,
                        unit = "250g tin",
                        imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?w=600&auto=format&fit=crop&q=80"
                    )
                ),
                subtotal = 550.0,
                deliveryFee = 0.0, // Free local delivery above 500
                totalAmount = 550.0,
                status = OrderStatus.PACKED,
                orderTimestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
                deliveryPromise = "1-Day Express Local Delivery",
                deliveryNotes = "Call before arriving.",
                paymentMethod = "UPI Online",
                paymentStatus = "Paid Online",
                distanceKm = distance2,
                estimatedMinutes = HaversineCalculator.estimateDeliveryMinutes(distance2),
                deliveryPartnerName = "Bikram Borah (Rider #3)",
                deliveryPartnerPhone = "+91 94350 99881",
                otpCode = "7193"
            )

            dao.insertOrders(listOf(sampleOrder1, sampleOrder2).map { OrderEntity.fromDomainModel(it) })
        }
    }

    suspend fun saveProduct(product: Product) {
        dao.insertProduct(ProductEntity.fromDomainModel(product))
    }

    suspend fun updateStock(productId: String, stock: Int) {
        dao.updateStock(productId, stock)
    }

    suspend fun updateAvailability(productId: String, isAvailable: Boolean) {
        dao.updateAvailability(productId, isAvailable)
    }

    suspend fun deleteProduct(productId: String) {
        dao.deleteProductById(productId)
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

        // Decrement product inventory
        for (item in finalOrder.items) {
            val entity = dao.getProductById(item.productId)
            if (entity != null) {
                val updatedStock = (entity.stockQuantity - item.quantity).coerceAtLeast(0)
                dao.updateStock(item.productId, updatedStock)
            }
        }

        return finalOrder
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        dao.updateOrderStatus(orderId, newStatus.name)
    }

    suspend fun deleteOrder(orderId: String) {
        dao.deleteOrder(orderId)
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
