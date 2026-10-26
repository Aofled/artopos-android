package ru.createsmart.artopos.core.common.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks if the network is validated (has actual Internet access),
 * not just if WiFi/Data is enabled.
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // Check for real internet access, not just active Wi-Fi or mobile network.
    fun isOnline(): Boolean = context.isNetworkAvailable()
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val activeNetwork = connectivityManager?.activeNetwork
    val capabilities = activeNetwork?.let { connectivityManager.getNetworkCapabilities(it) }
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
}
