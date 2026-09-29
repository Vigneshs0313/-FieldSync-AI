package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.FieldSyncLogo
import com.example.ui.components.FieldSyncTopNavHeader
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LogoAndNavigationTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testVectorLogo_rendersProperly() {
    composeTestRule.setContent {
      MyApplicationTheme {
        FieldSyncLogo()
      }
    }

    composeTestRule.onNodeWithTag("fieldsync_vector_logo_container").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fieldsync_vector_logo_image", useUnmergedTree = true).assertExists()
  }

  @Test
  fun testTopNavHeader_displaysLogoAndBranding() {
    composeTestRule.setContent {
      MyApplicationTheme {
        FieldSyncTopNavHeader()
      }
    }

    composeTestRule.onNodeWithTag("fieldsync_top_nav_header").assertIsDisplayed()
    composeTestRule.onNodeWithTag("top_nav_app_name").assertIsDisplayed()
    composeTestRule.onNodeWithText("FieldSync").assertIsDisplayed()
    composeTestRule.onNodeWithText("AI").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fieldsync_vector_logo_image", useUnmergedTree = true).assertExists()
  }

  @Test
  fun testSplashScreen_displaysLogoAndNavigates() {
    var navigated = false

    composeTestRule.setContent {
      MyApplicationTheme {
        SplashScreen(
          onNavigateToDashboard = { navigated = true },
          autoNavigateDelayMs = 10000L
        )
      }
    }

    composeTestRule.onNodeWithTag("splash_screen").assertExists()
    composeTestRule.onNodeWithTag("splash_vector_logo", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("splash_start_button", useUnmergedTree = true).assertExists()

    // Test clicking the launch button invokes navigation
    composeTestRule.onNodeWithTag("splash_start_button", useUnmergedTree = true).performClick()
    assertTrue(navigated)
  }

}
