package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.ConnectivityManagerWrapper
import com.example.util.NetworkStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConnectivityManagerWrapperTest {

  private lateinit var context: Context
  private lateinit var wrapper: ConnectivityManagerWrapper

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    wrapper = ConnectivityManagerWrapper(context)
  }

  @Test
  fun wrapper_initializes_and_exposes_status() {
    assertNotNull(wrapper)
    val isOnline = wrapper.isOnline()
    val isOffline = wrapper.isOffline()
    assertEquals(!isOnline, isOffline)
  }

  @Test
  fun networkStatus_flow_emits_valid_initial_value() = runBlocking {
    val status = wrapper.networkStatus.first()
    assertTrue(status == NetworkStatus.ONLINE || status == NetworkStatus.OFFLINE)
  }

  @Test
  fun networkStatus_enum_properties() {
    assertTrue(NetworkStatus.ONLINE.isOnline)
    assertTrue(!NetworkStatus.ONLINE.isOffline)
    assertTrue(NetworkStatus.OFFLINE.isOffline)
    assertTrue(!NetworkStatus.OFFLINE.isOnline)
  }

  @Test
  fun connectionType_resolves_valid_medium() {
    val connectionType = wrapper.getConnectionType()
    assertNotNull(connectionType)
  }
}
