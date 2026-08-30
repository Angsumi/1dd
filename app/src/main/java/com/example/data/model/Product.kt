package com.example.data.model

import java.util.UUID

enum class ProductCategory(val displayName: String, val iconName: String) {
    ALL("All Items", "category"),
    VEGETABLES("Fresh Veggies", "eco"),
    FRUITS("Farm Fruits", "nutrition"),
    DAIRY_BAKERY("Dairy & Eggs", "egg"),
    GRAINS_SPICES("Rice & Spices", "grain"),
    LOCAL_SPECIALS("Assam Specials", "local_activity"),
    SNACKS_BEVERAGES("Snacks & Tea", "coffee")
}

data class Product(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val price: Double,
    val unit: String = "1 kg",
    val category: ProductCategory = ProductCategory.VEGETABLES,
    val stockQuantity: Int = 20,
    val imageUrl: String = "",
    val isLocalSpecialty: Boolean = true,
    val isOneDayDelivery: Boolean = true,
    val rating: Double = 4.8,
    val reviewCount: Int = 34,
    val sellerName: String = "Local Green Farm & Organics",
    val isAvailable: Boolean = true
)
