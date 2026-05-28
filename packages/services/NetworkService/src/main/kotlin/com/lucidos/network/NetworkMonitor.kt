package com.lucidos.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log

/**
 * Network connectivity status monitor
 */
class NetworkMonitor(private val context: Context) {
    private const val TAG = "NetworkMonitor"
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    data class NetworkStatus(
        val isConnected: Boolean,
        val type: String,
        val isMetered: Boolean
    )

    fun getNetworkStatus(): NetworkStatus {
        return try {
            val network = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(network)

            val isConnected = network != null && caps != null
            val type = when {
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
                caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
                else -> "None"
            }
            val isMetered = connectivityManager?.isActiveNetworkMetered ?: false

            NetworkStatus(isConnected, type, isMetered)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network status", e)
            NetworkStatus(false, "Unknown", false)
        }
    }

    fun isConnected(): Boolean = getNetworkStatus().isConnected

    fun logNetworkInfo() {
        val status = getNetworkStatus()
        Log.d(TAG, """
            Network Status:
            Connected: ${status.isConnected}
            Type: ${status.type}
            Metered: ${status.isMetered}
        """.trimIndent())
    }
}
