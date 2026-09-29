package com.example.util

import android.content.Context
import kotlinx.coroutines.flow.Flow

typealias ConnectivityStatus = NetworkStatus

interface ConnectivityObserver {
  val status: Flow<NetworkStatus>
  fun isCurrentlyOnline(): Boolean
}


class NetworkConnectivityObserver(
  context: Context,
  private val wrapper: ConnectivityManagerWrapper = ConnectivityManagerWrapper(context)
) : ConnectivityObserver {

  override val status: Flow<NetworkStatus> = wrapper.networkStatus

  override fun isCurrentlyOnline(): Boolean = wrapper.isOnline()
}

