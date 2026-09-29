package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.data.NetworkMode
import com.example.ui.components.PersistentConnectivityStatusBar
import com.example.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConnectivityStatusBarTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun shows_syncing_when_online() {
    composeTestRule.setContent {
      MyApplicationTheme {
        PersistentConnectivityStatusBar(
          isOnline = true,
          networkMode = NetworkMode.ONLINE,
          isSyncing = false,
          pendingCount = 0,
          conflictCount = 0,
          syncMessage = null,
          onToggleMode = {},
          onTriggerSync = {},
          onNavigateToConflicts = {},
          onNavigateToSync = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("persistent_connectivity_status_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Syncing").assertIsDisplayed()
    composeTestRule.onNodeWithTag("connectivity_syncing_indicator", useUnmergedTree = true).assertExists()
  }

  @Test
  fun shows_offline_mode_when_disconnected() {
    composeTestRule.setContent {
      MyApplicationTheme {
        PersistentConnectivityStatusBar(
          isOnline = false,
          networkMode = NetworkMode.OFFLINE,
          isSyncing = false,
          pendingCount = 2,
          conflictCount = 0,
          syncMessage = null,
          onToggleMode = {},
          onTriggerSync = {},
          onNavigateToConflicts = {},
          onNavigateToSync = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("persistent_connectivity_status_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Offline Mode").assertIsDisplayed()
    composeTestRule.onNodeWithTag("connectivity_offline_badge", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithText("Outbox: 2").assertIsDisplayed()
  }
}
