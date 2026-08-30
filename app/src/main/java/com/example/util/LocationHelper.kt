package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        val gpsEnabled = try { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (e: Exception) { false }
        val networkEnabled = try { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { false }
        return gpsEnabled || networkEnabled
    }

    /**
     * Fetches real-time GPS coordinates of the device using FusedLocationProviderClient
     * with standard LocationManager fallback.
     */
    fun fetchRealtimeGpsLocation(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (lat: Double, lng: Double, addressPreview: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError("Location permission not granted. Please allow location access.")
            return
        }

        if (!isLocationServiceEnabled(context)) {
            onError("GPS / Device Location is turned off. Please enable Location in your phone settings.")
            return
        }

        val fusedClient = try {
            LocationServices.getFusedLocationProviderClient(context)
        } catch (e: Exception) {
            null
        }

        val cts = CancellationTokenSource()

        try {
            if (fusedClient != null) {
                // Request highest accuracy real-time fix
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            processLocation(context, scope, location.latitude, location.longitude, onSuccess)
                        } else {
                            // Try last known location from fused provider
                            fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                                if (lastLoc != null) {
                                    processLocation(context, scope, lastLoc.latitude, lastLoc.longitude, onSuccess)
                                } else {
                                    // Fallback to android.location.LocationManager
                                    fallbackToLocationManager(context, scope, onSuccess, onError)
                                }
                            }.addOnFailureListener {
                                fallbackToLocationManager(context, scope, onSuccess, onError)
                            }
                        }
                    }
                    .addOnFailureListener {
                        fallbackToLocationManager(context, scope, onSuccess, onError)
                    }
            } else {
                fallbackToLocationManager(context, scope, onSuccess, onError)
            }
        } catch (e: SecurityException) {
            onError("Location permission required: ${e.message}")
        } catch (e: Exception) {
            fallbackToLocationManager(context, scope, onSuccess, onError)
        }
    }

    private fun fallbackToLocationManager(
        context: Context,
        scope: CoroutineScope,
        onSuccess: (lat: Double, lng: Double, addressPreview: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                onError("Location service unavailable on this device.")
                return
            }

            var bestLocation: Location? = null

            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )

            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null && (bestLocation == null || loc.accuracy < bestLocation.accuracy)) {
                            bestLocation = loc
                        }
                    }
                } catch (e: SecurityException) {
                    // Ignore and continue
                }
            }

            if (bestLocation != null) {
                processLocation(context, scope, bestLocation.latitude, bestLocation.longitude, onSuccess)
            } else {
                // Register one-time update listener
                val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    LocationManager.GPS_PROVIDER
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    LocationManager.NETWORK_PROVIDER
                } else {
                    null
                }

                if (provider != null) {
                    val singleListener = object : LocationListener {
                        override fun onLocationChanged(loc: Location) {
                            try {
                                locationManager.removeUpdates(this)
                            } catch (e: Exception) {}
                            processLocation(context, scope, loc.latitude, loc.longitude, onSuccess)
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(p: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(p: String) {}
                        override fun onProviderDisabled(p: String) {}
                    }

                    locationManager.requestSingleUpdate(provider, singleListener, Looper.getMainLooper())
                } else {
                    onError("Could not obtain GPS fix. Please ensure location is enabled and you are in an open area.")
                }
            }
        } catch (e: SecurityException) {
            onError("Location permission denied: ${e.message}")
        } catch (e: Exception) {
            onError("GPS acquisition error: ${e.localizedMessage ?: "Unknown location error"}")
        }
    }

    private fun processLocation(
        context: Context,
        scope: CoroutineScope,
        lat: Double,
        lng: Double,
        onSuccess: (lat: Double, lng: Double, addressPreview: String?) -> Unit
    ) {
        scope.launch {
            var addressText: String? = null
            try {
                withContext(Dispatchers.IO) {
                    if (Geocoder.isPresent()) {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(lat, lng, 1) { addresses ->
                                if (addresses.isNotEmpty()) {
                                    val addr = addresses[0]
                                    addressText = formatAddress(addr)
                                }
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(lat, lng, 1)
                            if (!addresses.isNullOrEmpty()) {
                                addressText = formatAddress(addresses[0])
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Geocoding failure is non-fatal; coordinates are still valid
            }

            withContext(Dispatchers.Main) {
                onSuccess(lat, lng, addressText)
            }
        }
    }

    private fun formatAddress(addr: Address): String {
        val parts = mutableListOf<String>()
        addr.featureName?.let { if (it.isNotBlank() && it != addr.subLocality) parts.add(it) }
        addr.subLocality?.let { if (it.isNotBlank()) parts.add(it) }
        addr.locality?.let { if (it.isNotBlank() && it != addr.subLocality) parts.add(it) }
        addr.subAdminArea?.let { if (it.isNotBlank() && !parts.contains(it)) parts.add(it) }
        return if (parts.isNotEmpty()) parts.joinToString(", ") else (addr.getAddressLine(0) ?: "")
    }
}
