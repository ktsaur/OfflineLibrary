package ru.itis.offlinecache

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Утилита для проверки состояния интернета.
 * Использует встроенный Android инструмент — ConnectivityManager.
 */
class NetworkChecker(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Возвращает true если интернет есть и работает
     */
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Возвращает тип соединения — удобно для логов и дашборда
     */
    fun getConnectionType(): ConnectionType {
        val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return ConnectionType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.UNKNOWN
        }
    }

    enum class ConnectionType {
        WIFI,       // Wi-Fi
        MOBILE,     // мобильный интернет
        ETHERNET,   // проводной
        UNKNOWN,    // есть, но непонятно какой
        NONE        // нет интернета
    }
}