package com.example

import com.example.data.model.HaversineCalculator
import com.example.data.model.LocationConstants
import com.example.data.model.PRESET_LOCAL_DESTINATIONS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testHaversineDistanceFromDepotToSelfIsZero() {
        val distance = HaversineCalculator.calculateDistanceKm(
            LocationConstants.STORE_ORIGIN_LAT,
            LocationConstants.STORE_ORIGIN_LNG,
            LocationConstants.STORE_ORIGIN_LAT,
            LocationConstants.STORE_ORIGIN_LNG
        )
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun testHaversineDistanceCalculationsForLocalLandmarks() {
        PRESET_LOCAL_DESTINATIONS.forEach { landmark ->
            val dist = HaversineCalculator.calculateDistanceFromStore(landmark.latitude, landmark.longitude)
            // Ensure all preset local landmarks are within realistic delivery radius
            assertTrue("Distance for ${landmark.name} should be > 0", dist > 0.0)
            assertTrue("Distance for ${landmark.name} should be <= 25.0 km", dist <= 25.0)

            val eta = HaversineCalculator.estimateDeliveryMinutes(dist)
            assertTrue("ETA should be at least 15 minutes", eta >= 15)
        }
    }

    @Test
    fun testSpecificHaversineCoordinates() {
        // Destination: 26.848920, 92.924150
        val dist = HaversineCalculator.calculateDistanceKm(
            26.838432,
            92.910880,
            26.848920,
            92.924150
        )
        // Approx 1.74 km
        assertTrue("Distance $dist should be in range 1.5 to 2.0 km", dist in 1.5..2.0)
    }
}
