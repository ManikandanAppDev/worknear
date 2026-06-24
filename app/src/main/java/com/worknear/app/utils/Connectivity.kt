package com.worknear.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Returns true when there is an active network with validated internet access. */
fun Context.isCurrentlyOnline(): Boolean {
    val cm = getSystemService(ConnectivityManager::class.java) ?: return true
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

/**
 * Observes connectivity and emits the current online state. Registers a [ConnectivityManager]
 * callback for the lifetime of the composition and cleans it up on dispose.
 */
@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    val isOnline = remember { mutableStateOf(context.isCurrentlyOnline()) }

    DisposableEffect(context) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isOnline.value = context.isCurrentlyOnline()
            }

            override fun onLost(network: Network) {
                isOnline.value = context.isCurrentlyOnline()
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                isOnline.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm?.registerNetworkCallback(request, callback)
        // Re-check immediately in case state changed before the callback registered.
        isOnline.value = context.isCurrentlyOnline()

        onDispose { cm?.unregisterNetworkCallback(callback) }
    }

    return isOnline
}
