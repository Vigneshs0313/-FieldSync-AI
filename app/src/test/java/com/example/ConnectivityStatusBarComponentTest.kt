package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.network.ConnectivityManager
import com.example.ui.components.ConnectivityStatusBar
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConnectivityStatusBarComponentTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun displays_syncing_when_online() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ConnectivityStatusBar(
          isConnected = true
        )
      }
    }

    composeTestRule.onNodeWithTag("connectivity_status_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Syncing").assertIsDisplayed()
    composeTestRule.onNodeWithTag("status_icon", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("ONLINE").assertIsDisplayed()
  }

  @Test
  fun displays_offline_mode_when_disconnected() {
    composeTestRule.setContent {
      MyApplicationTheme {
        ConnectivityStatusBar(
          isConnected = false
        )
      }
    }

    composeTestRule.onNodeWithTag("connectivity_status_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Offline Mode").assertIsDisplayed()
    composeTestRule.onNodeWithTag("status_icon", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("LOCAL").assertIsDisplayed()
  }

  @Test
  fun consumes_state_flow_directly() {
    val stateFlow = MutableStateFlow(true)

    composeTestRule.setContent {
      MyApplicationTheme {
        ConnectivityStatusBar(
          isConnectedFlow = stateFlow
        )
      }
    }

    composeTestRule.onNodeWithText("Syncing").assertIsDisplayed()

    // Transition state
    stateFlow.value = false
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Offline Mode").assertIsDisplayed()
  }

  @Test
  fun consumes_connectivity_manager_state() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val cm = ConnectivityManager(context)

    composeTestRule.setContent {
      MyApplicationTheme {
        ConnectivityStatusBar(
          connectivityManager = cm
        )
      }
    }

    composeTestRule.onNodeWithTag("connectivity_status_bar").assertIsDisplayed()
    // Initial state verified based on current manager state
    val isConnected = cm.isConnected.value
    if (isConnected) {
      composeTestRule.onNodeWithText("Syncing").assertIsDisplayed()
    } else {
      composeTestRule.onNodeWithText("Offline Mode").assertIsDisplayed()
    }

    cm.unregister()
  }
}
