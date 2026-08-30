package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.HaversineCalculator
import com.example.data.model.LocationConstants
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.PRESET_LOCAL_DESTINATIONS
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import com.example.data.repository.MarketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppRole(val title: String, val subtitle: String) {
    CUSTOMER("Storefront", "Browse & 1-Day Order"),
    SELLER_ADMIN("Seller Admin", "Inventory & Orders"),
    DELIVERY_PARTNER("Delivery Partner", "Haversine Route & Maps"),
    CLOUD_HOSTING("Free Cloud Hosting", "Vercel / Netlify & Firebase")
}

data class OwnerProfile(
    val email: String = "angsudas62@gmail.com",
    val displayName: String = "Store House Owner",
    val photoUrl: String = "",
    val isVerified: Boolean = true
)

data class CartItemState(
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}

class MarketViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MarketRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = MarketRepository(db.marketDao())
        viewModelScope.launch {
            repository.initializeSeedDataIfEmpty()
        }
    }

    // Owner Google Authentication state
    private val _isOwnerLoggedIn = MutableStateFlow(false)
    val isOwnerLoggedIn: StateFlow<Boolean> = _isOwnerLoggedIn.asStateFlow()

    private val _ownerProfile = MutableStateFlow<OwnerProfile?>(null)
    val ownerProfile: StateFlow<OwnerProfile?> = _ownerProfile.asStateFlow()

    private val _isOwnerViewActive = MutableStateFlow(false)
    val isOwnerViewActive: StateFlow<Boolean> = _isOwnerViewActive.asStateFlow()

    // Immediate real-time alert for Owner when any new order arrives
    private val _newOrderNotification = MutableStateFlow<Order?>(null)
    val newOrderNotification: StateFlow<Order?> = _newOrderNotification.asStateFlow()

    fun loginAsOwnerWithGoogle(
        email: String = "angsudas62@gmail.com",
        name: String = "Store House Owner"
    ) {
        _ownerProfile.value = OwnerProfile(
            email = email.ifBlank { "angsudas62@gmail.com" },
            displayName = name.ifBlank { "Store House Owner" }
        )
        _isOwnerLoggedIn.value = true
        _isOwnerViewActive.value = true
    }

    fun logoutOwner() {
        _ownerProfile.value = null
        _isOwnerLoggedIn.value = false
        _isOwnerViewActive.value = false
    }

    fun setOwnerViewActive(active: Boolean) {
        _isOwnerViewActive.value = active
    }

    fun dismissNewOrderNotification() {
        _newOrderNotification.value = null
    }

    fun seedSampleOwnerItems() {
        viewModelScope.launch {
            repository.seedSampleOwnerItems()
        }
    }

    // Role state
    private val _selectedRole = MutableStateFlow(AppRole.CUSTOMER)
    val selectedRole: StateFlow<AppRole> = _selectedRole.asStateFlow()

    fun selectRole(role: AppRole) {
        _selectedRole.value = role
    }

    // Search and Category Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
    val selectedCategory: StateFlow<ProductCategory> = _selectedCategory.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: ProductCategory) {
        _selectedCategory.value = category
    }

    // Raw products and orders flows
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered products for customer store
    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesCategory = (category == ProductCategory.ALL || product.category == category)
            val matchesQuery = query.isBlank() ||
                    product.title.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.sellerName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shopping Cart State
    private val _cartItems = MutableStateFlow<Map<String, CartItemState>>(emptyMap())
    val cartItems: StateFlow<Map<String, CartItemState>> = _cartItems.asStateFlow()

    val cartTotalCount: StateFlow<Int> = _cartItems.map { itemsMap ->
        itemsMap.values.sumOf { it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartSubtotal: StateFlow<Double> = _cartItems.map { itemsMap ->
        itemsMap.values.sumOf { it.subtotal }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val deliveryFee: StateFlow<Double> = cartSubtotal.map { subtotal ->
        if (subtotal >= 499.0 || subtotal == 0.0) 0.0 else 25.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 25.0)

    val cartGrandTotal: StateFlow<Double> = combine(cartSubtotal, deliveryFee) { sub, fee ->
        if (sub > 0) sub + fee else 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addToCart(product: Product) {
        val current = _cartItems.value.toMutableMap()
        val existing = current[product.id]
        if (existing != null) {
            if (existing.quantity < product.stockQuantity) {
                current[product.id] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            current[product.id] = CartItemState(product, 1)
        }
        _cartItems.value = current
    }

    fun removeFromCart(productId: String) {
        val current = _cartItems.value.toMutableMap()
        val existing = current[productId] ?: return
        if (existing.quantity > 1) {
            current[productId] = existing.copy(quantity = existing.quantity - 1)
        } else {
            current.remove(productId)
        }
        _cartItems.value = current
    }

    fun deleteFromCart(productId: String) {
        val current = _cartItems.value.toMutableMap()
        current.remove(productId)
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    // Active order placed by user during this session for quick tracking
    private val _lastPlacedOrder = MutableStateFlow<Order?>(null)
    val lastPlacedOrder: StateFlow<Order?> = _lastPlacedOrder.asStateFlow()

    fun placeOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        latitude: Double,
        longitude: Double,
        landmarkName: String,
        deliveryNotes: String,
        paymentMethod: String
    ): Order? {
        val items = _cartItems.value.values.map {
            OrderItem(
                productId = it.product.id,
                productTitle = it.product.title,
                unitPrice = it.product.price,
                quantity = it.quantity,
                unit = it.product.unit,
                imageUrl = it.product.imageUrl
            )
        }
        if (items.isEmpty()) return null

        val subtotal = cartSubtotal.value
        val fee = deliveryFee.value
        val total = subtotal + fee
        val distance = HaversineCalculator.calculateDistanceFromStore(latitude, longitude)

        val newOrder = Order(
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            deliveryLat = latitude,
            deliveryLng = longitude,
            landmarkName = landmarkName,
            items = items,
            subtotal = subtotal,
            deliveryFee = fee,
            totalAmount = total,
            status = OrderStatus.PLACED,
            deliveryNotes = deliveryNotes,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod.contains("UPI", ignoreCase = true)) "Paid via UPI" else "Collect on Delivery",
            distanceKm = distance,
            estimatedMinutes = HaversineCalculator.estimateDeliveryMinutes(distance)
        )

        viewModelScope.launch {
            val created = repository.createOrder(newOrder)
            _lastPlacedOrder.value = created
            _newOrderNotification.value = created
            clearCart()
        }

        return newOrder
    }

    // Admin product creation & editing
    fun saveProduct(
        id: String?,
        title: String,
        description: String,
        price: Double,
        unit: String,
        category: ProductCategory,
        stockQuantity: Int,
        imageUrl: String,
        isLocalSpecialty: Boolean
    ) {
        viewModelScope.launch {
            val product = Product(
                id = id ?: java.util.UUID.randomUUID().toString(),
                title = title.trim(),
                description = description.trim(),
                price = price,
                unit = unit.trim(),
                category = category,
                stockQuantity = stockQuantity,
                imageUrl = imageUrl.trim(),
                isLocalSpecialty = isLocalSpecialty,
                isOneDayDelivery = true,
                isAvailable = stockQuantity > 0
            )
            repository.saveProduct(product)
        }
    }

    fun updateStock(productId: String, newStock: Int) {
        viewModelScope.launch {
            repository.updateStock(productId, newStock)
        }
    }

    fun toggleAvailability(productId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateAvailability(productId, isAvailable)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    // Order status update for Seller & Delivery Partner
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            if (_lastPlacedOrder.value?.id == orderId) {
                _lastPlacedOrder.value = _lastPlacedOrder.value?.copy(status = newStatus)
            }
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
            if (_lastPlacedOrder.value?.id == orderId) {
                _lastPlacedOrder.value = null
            }
        }
    }

    // Cloud export string
    private val _cloudExportJson = MutableStateFlow("")
    val cloudExportJson: StateFlow<String> = _cloudExportJson.asStateFlow()

    fun generateCloudExportJson() {
        viewModelScope.launch {
            _cloudExportJson.value = repository.exportJsonDataForCloud()
        }
    }
}
