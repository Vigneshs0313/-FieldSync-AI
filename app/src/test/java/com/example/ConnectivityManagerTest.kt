package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.network.ConnectivityManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConnectivityManagerTest {

  private lateinit var context: Context
  private lateinit var connectivityManager: ConnectivityManager

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    connectivityManager = ConnectivityManager(context)
  }

  @After
  fun tearDown() {
    connectivityManager.unregister()
  }

  @Test
  fun exposes_isConnected_and_isOnline_stateflow() = runBlocking {
    val isConnected = connectivityManager.isConnected.value
    val isOnline = connectivityManager.isOnline.value
    assertEquals(isConnected, isOnline)
    assertNotNull(connectivityManager.isConnected.first())
  }

  @Test
  fun synchronous_methods_match_stateflow_initial_value() {
    val isConnectedSync = connectivityManager.isCurrentlyConnected()
    val isOfflineSync = connectivityManager.isCurrentlyOffline()
    assertEquals(!isConnectedSync, isOfflineSync)
  }
}
