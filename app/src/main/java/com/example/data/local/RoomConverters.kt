package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.OrderItem
import com.example.data.model.ProductCategory
import org.json.JSONArray
import org.json.JSONObject

class RoomConverters {
    @TypeConverter
    fun fromCategory(category: ProductCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): ProductCategory {
        return try {
            ProductCategory.valueOf(value)
        } catch (e: Exception) {
            ProductCategory.ALL
        }
    }

    @TypeConverter
    fun fromOrderItems(items: List<OrderItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("productId", item.productId)
                put("productTitle", item.productTitle)
                put("unitPrice", item.unitPrice)
                put("quantity", item.quantity)
                put("unit", item.unit)
                put("imageUrl", item.imageUrl)
            }
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toOrderItems(json: String): List<OrderItem> {
        val list = mutableListOf<OrderItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    OrderItem(
                        productId = obj.optString("productId"),
                        productTitle = obj.optString("productTitle"),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        quantity = obj.optInt("quantity", 1),
                        unit = obj.optString("unit", "1 kg"),
                        imageUrl = obj.optString("imageUrl", "")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback
        }
        return list
    }
}
