package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Representation of network connectivity status.
 */
enum class NetworkStatus {
  ONLINE,
  OFFLINE;

  val isOnline: Boolean get() = this == ONLINE
  val isOffline: Boolean get() = this == OFFLINE
}

/**
 * Network connection medium category.
 */
enum class ConnectionType {
  WIFI,
  CELLULAR,
  ETHERNET,
  OTHER,
  NONE
}

/**
 * ConnectivityManager wrapper that monitors device network connectivity and exposes
 * real-time network status (Online/Offline) as an observable Kotlin Flow to the UI.
 */
class ConnectivityManagerWrapper(
  private val context: Context,
  private val connectivityManager: ConnectivityManager? =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
) {

  /**
   * Observable Flow emitting [NetworkStatus.ONLINE] or [NetworkStatus.OFFLINE].
   * Emits immediately upon collection with the current network state, and then
   * reacts instantaneously to network transitions.
   */
  val networkStatus: Flow<NetworkStatus> = callbackFlow {
    if (connectivityManager == null) {
      trySend(NetworkStatus.OFFLINE)
      close()
      return@callbackFlow
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)
        trySend(NetworkStatus.ONLINE)
      }

      override fun onLost(network: Network) {
        super.onLost(network)
        trySend(NetworkStatus.OFFLINE)
      }

      override fun onUnavailable() {
        super.onUnavailable()
        trySend(NetworkStatus.OFFLINE)
      }

      override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
      ) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        // If capabilities show Internet (and optionally validated when available), mark online
        if (hasInternet) {
          trySend(NetworkStatus.ONLINE)
        } else {
          trySend(NetworkStatus.OFFLINE)
        }
      }
    }

    // Emit initial status synchronously upon collection
    val currentStatus = if (isOnline()) NetworkStatus.ONLINE else NetworkStatus.OFFLINE
    trySend(currentStatus)

    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    try {
      connectivityManager.registerNetworkCallback(request, callback)
    } catch (e: Exception) {
      // Handle security or test environment constraints gracefully
    }

    awaitClose {
      try {
        connectivityManager.unregisterNetworkCallback(callback)
      } catch (e: Exception) {
        // Ignored
      }
    }
  }.distinctUntilChanged()

  /**
   * Synchronously checks if the device is currently online with an active Internet capability.
   */
  fun isOnline(): Boolean {
    if (connectivityManager == null) return false
    return try {
      val activeNetwork = connectivityManager.activeNetwork ?: return false
      val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) {
      true
    }
  }

  /**
   * Synchronously checks if the device is disconnected.
   */
  fun isOffline(): Boolean = !isOnline()

  /**
   * Returns the current connection medium type.
   */
  fun getConnectionType(): ConnectionType {
    if (connectivityManager == null) return ConnectionType.NONE
    return try {
      val activeNetwork = connectivityManager.activeNetwork ?: return ConnectionType.NONE
      val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return ConnectionType.NONE
      when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
        else -> ConnectionType.OTHER
      }
    } catch (e: Exception) {
      ConnectionType.NONE
    }
  }
}
