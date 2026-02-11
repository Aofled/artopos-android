package ru.createsmart.artopos.core.common.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val capabilities = connectivityManager.activeNetwork?.let { network ->
        connectivityManager.getNetworkCapabilities(network)
    }

    return capabilities?.run {
        // Checks only connectivity type, not actual Internet access (Ping)
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    } ?: false
}
