package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Product
import com.example.data.model.ProductCategory

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val unit: String,
    val category: ProductCategory,
    val stockQuantity: Int,
    val imageUrl: String,
    val isLocalSpecialty: Boolean,
    val isOneDayDelivery: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val sellerName: String,
    val isAvailable: Boolean
) {
    fun toDomainModel(): Product = Product(
        id = id,
        title = title,
        description = description,
        price = price,
        unit = unit,
        category = category,
        stockQuantity = stockQuantity,
        imageUrl = imageUrl,
        isLocalSpecialty = isLocalSpecialty,
        isOneDayDelivery = isOneDayDelivery,
        rating = rating,
        reviewCount = reviewCount,
        sellerName = sellerName,
        isAvailable = isAvailable
    )

    companion object {
        fun fromDomainModel(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            title = product.title,
            description = product.description,
            price = product.price,
            unit = product.unit,
            category = product.category,
            stockQuantity = product.stockQuantity,
            imageUrl = product.imageUrl,
            isLocalSpecialty = product.isLocalSpecialty,
            isOneDayDelivery = product.isOneDayDelivery,
            rating = product.rating,
            reviewCount = product.reviewCount,
            sellerName = product.sellerName,
            isAvailable = product.isAvailable
        )
    }
}
