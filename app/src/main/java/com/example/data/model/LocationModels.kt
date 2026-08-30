package com.example.data.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Fixed origin hub specified in prompt:
 * Lat: 26.838432, Lng: 92.910880 (Local Depot / Assam Zone)
 */
object LocationConstants {
    const val STORE_ORIGIN_LAT = 26.838432
    const val STORE_ORIGIN_LNG = 92.910880
    const val STORE_HUB_NAME = "LocalMart Central Depot (Hub #1)"
    const val STORE_HUB_ADDRESS = "Station Road Logistics Point, 26.8384, 92.9108"
    const val STORE_PHONE = "+91 98640 12345"
    const val MAX_LOCAL_DELIVERY_RADIUS_KM = 35.0
}

data class LocalLandmark(
    val id: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val areaTag: String
)

val PRESET_LOCAL_DESTINATIONS = listOf(
    LocalLandmark(
        id = "dest_1",
        name = "Mission Chariali Market",
        description = "Commercial Square, Near Bus Terminus",
        latitude = 26.848920,
        longitude = 92.924150,
        areaTag = "North Sector"
    ),
    LocalLandmark(
        id = "dest_2",
        name = "Lake View Colony, Block B",
        description = "Opposite Padum Pukhuri Park",
        latitude = 26.829140,
        longitude = 92.899450,
        areaTag = "South Lakeside"
    ),
    LocalLandmark(
        id = "dest_3",
        name = "University Road Campus Gate",
        description = "Napaam Campus Residential Zone",
        latitude = 26.852100,
        longitude = 92.841200,
        areaTag = "University Area"
    ),
    LocalLandmark(
        id = "dest_4",
        name = "Tribeni Commercial Complex",
        description = "Main MG Road, Shop #14",
        latitude = 26.835400,
        longitude = 92.915600,
        areaTag = "City Center"
    ),
    LocalLandmark(
        id = "dest_5",
        name = "Chowk Bazaar Corner",
        description = "Old Market Gate, Main Street",
        latitude = 26.840100,
        longitude = 92.918900,
        areaTag = "Central Market"
    ),
    LocalLandmark(
        id = "dest_6",
        name = "Green Heights Apartments",
        description = "Near Civil Hospital Road",
        latitude = 26.844500,
        longitude = 92.905200,
        areaTag = "East Sector"
    )
)

object HaversineCalculator {
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates distance between store origin (26.838432, 92.910880) and target coordinates
     * using the accurate Haversine formula.
     */
    fun calculateDistanceFromStore(targetLat: Double, targetLng: Double): Double {
        return calculateDistanceKm(
            LocationConstants.STORE_ORIGIN_LAT,
            LocationConstants.STORE_ORIGIN_LNG,
            targetLat,
            targetLng
        )
    }

    /**
     * Haversine formula implementation
     */
    fun calculateDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS_KM * c

        // Round to 2 decimal places
        return Math.round(distance * 100.0) / 100.0
    }

    fun estimateDeliveryMinutes(distanceKm: Double): Int {
        // Base dispatch prep 15 mins + ~3.5 min/km in local terrain
        return (15 + (distanceKm * 3.5)).toInt().coerceIn(20, 180)
    }

    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            "${(distanceKm * 1000).toInt()} m"
        } else {
            String.format("%.1f km", distanceKm)
        }
    }
}
