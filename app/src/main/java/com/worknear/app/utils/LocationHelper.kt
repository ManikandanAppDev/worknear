package com.worknear.app.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class ResolvedLocation(
    val city: String,
    val area: String,
    val latitude: Double,
    val longitude: Double
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    fun isLocationEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun openLocationSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun openAppSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    suspend fun resolveCurrentLocation(context: Context): ResolvedLocation? =
        withContext(Dispatchers.IO) {
            if (!hasLocationPermission(context)) return@withContext null
            val location = fetchLocation(context) ?: return@withContext null
            reverseGeocode(context, location.latitude, location.longitude)
        }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLocation(context: Context): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)

        val cached = awaitLastLocation(client)
        if (cached != null) return cached

        return withTimeoutOrNull(20_000L) {
            awaitCurrentLocation(client)
        } ?: awaitLastLocation(client)
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitLastLocation(
        client: com.google.android.gms.location.FusedLocationProviderClient
    ): Location? = suspendCancellableCoroutine { cont ->
        client.lastLocation
            .addOnSuccessListener { location ->
                if (cont.isActive) cont.resume(location)
            }
            .addOnFailureListener {
                if (cont.isActive) cont.resume(null)
            }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitCurrentLocation(
        client: com.google.android.gms.location.FusedLocationProviderClient
    ): Location? = suspendCancellableCoroutine { cont ->
        val tokenSource = CancellationTokenSource()
        // Start the GMS request before wiring coroutine cancellation — otherwise a
        // LaunchedEffect restart can cancel the token before getCurrentLocation runs.
        val task = client.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            tokenSource.token
        )
        cont.invokeOnCancellation { tokenSource.cancel() }
        task
            .addOnSuccessListener { location ->
                if (!cont.isActive) return@addOnSuccessListener
                if (location != null) {
                    cont.resume(location)
                } else {
                    client.lastLocation
                        .addOnSuccessListener { last ->
                            if (cont.isActive) cont.resume(last)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
            }
            .addOnFailureListener {
                if (!cont.isActive) return@addOnFailureListener
                client.lastLocation
                    .addOnSuccessListener { last ->
                        if (cont.isActive) cont.resume(last)
                    }
                    .addOnFailureListener {
                        if (cont.isActive) cont.resume(null)
                    }
            }
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(context: Context, lat: Double, lng: Double): ResolvedLocation? {
        if (!Geocoder.isPresent()) return null
        return try {
            val address = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)?.firstOrNull()
                ?: return null
            val city = listOfNotNull(address.locality, address.subAdminArea, address.adminArea)
                .firstOrNull { it.isNotBlank() }
                .orEmpty()
            val area = listOfNotNull(address.subLocality, address.thoroughfare, address.featureName)
                .firstOrNull { it.isNotBlank() }
                .orEmpty()
            if (city.isBlank() && area.isBlank()) return null
            ResolvedLocation(
                city = city.ifBlank { area },
                area = area.ifBlank { city },
                latitude = lat,
                longitude = lng
            )
        } catch (_: Exception) {
            null
        }
    }
}
