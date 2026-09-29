package com.example.network

import android.content.Context
import android.net.ConnectivityManager as AndroidConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ConnectivityManager class that leverages Android's ConnectivityManager.NetworkCallback
 * to observe network changes and exposes a StateFlow<Boolean> for the application to consume.
 */
class ConnectivityManager(
  context: Context,
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

  private val androidConnectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as? AndroidConnectivityManager

  private val _isConnected = MutableStateFlow(isCurrentlyConnected())

  /**
   * Observable StateFlow indicating whether the device has an active Internet connection.
   */
  val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

  /**
   * Observable StateFlow alias for isConnected, for consumers preferring isOnline naming.
   */
  val isOnline: StateFlow<Boolean> get() = isConnected

  private val networkCallback = object : AndroidConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      super.onAvailable(network)
      _isConnected.value = true
    }

    override fun onLost(network: Network) {
      super.onLost(network)
      _isConnected.value = isCurrentlyConnected()
    }

    override fun onUnavailable() {
      super.onUnavailable()
      _isConnected.value = false
    }

    override fun onCapabilitiesChanged(
      network: Network,
      networkCapabilities: NetworkCapabilities
    ) {
      super.onCapabilitiesChanged(network, networkCapabilities)
      val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      _isConnected.value = hasInternet
    }
  }

  init {
    registerCallback()
  }

  private fun registerCallback() {
    if (androidConnectivityManager == null) {
      _isConnected.value = false
      return
    }

    val request = NetworkRequest.Builder()
      .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
      .build()

    try {
      androidConnectivityManager.registerNetworkCallback(request, networkCallback)
    } catch (e: Exception) {
      // Graceful fallback for test or mock environments
    }
  }

  /**
   * Synchronously checks if the device currently has an active network with Internet capability.
   */
  fun isCurrentlyConnected(): Boolean {
    if (androidConnectivityManager == null) return false
    return try {
      val activeNetwork = androidConnectivityManager.activeNetwork ?: return false
      val capabilities = androidConnectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
      capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    } catch (e: Exception) {
      true
    }
  }

  /**
   * Synchronously checks if the device is currently offline.
   */
  fun isCurrentlyOffline(): Boolean = !isCurrentlyConnected()

  /**
   * Unregisters the network callback when done to prevent memory leaks.
   */
  fun unregister() {
    try {
      androidConnectivityManager?.unregisterNetworkCallback(networkCallback)
    } catch (e: Exception) {
      // Ignored if already unregistered
    }
  }
}
