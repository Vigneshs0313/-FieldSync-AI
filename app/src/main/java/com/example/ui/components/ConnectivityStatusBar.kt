package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.network.ConnectivityManager

import kotlinx.coroutines.flow.StateFlow

/**
 * ConnectivityStatusBar composable that consumes the [ConnectivityManager] state
 * and displays a persistent UI element showing 'Syncing' when online and 'Offline Mode'
 * when disconnected.
 */
@Composable
fun ConnectivityStatusBar(
  connectivityManager: ConnectivityManager,
  modifier: Modifier = Modifier
) {
  val isConnected by connectivityManager.isConnected.collectAsState()
  ConnectivityStatusBar(
    isConnected = isConnected,
    modifier = modifier
  )
}

/**
 * Overload that consumes a [StateFlow] of Boolean directly.
 */
@Composable
fun ConnectivityStatusBar(
  isConnectedFlow: StateFlow<Boolean>,
  modifier: Modifier = Modifier
) {
  val isConnected by isConnectedFlow.collectAsState()
  ConnectivityStatusBar(
    isConnected = isConnected,
    modifier = modifier
  )
}

/**
 * Direct Composable that renders the persistent connectivity status UI element.
 */
@Composable
fun ConnectivityStatusBar(
  isConnected: Boolean,
  modifier: Modifier = Modifier,
  onlineText: String = "Syncing",
  offlineText: String = "Offline Mode",
  onlineSubText: String = "Cloud Gateway Connected",
  offlineSubText: String = "Room DB Local Persistence"
) {
  val statusLabel = if (isConnected) onlineText else offlineText
  val statusSubtext = if (isConnected) onlineSubText else offlineSubText

  // Theme transitions: Forest green for online syncing, slate dark for offline mode
  val targetBgColor = if (isConnected) Color(0xFF14532D) else Color(0xFF1E293B)
  val animatedBgColor by animateColorAsState(
    targetValue = targetBgColor,
    animationSpec = tween(durationMillis = 350),
    label = "connectivityStatusBarBg"
  )

  // Subtle pulsing animation for status indicator
  val infiniteTransition = rememberInfiniteTransition(label = "connectivityPulseTransition")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.45f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "connectivityPulseAlpha"
  )

  // Smooth rotation for active sync icon when online
  val syncRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "connectivitySyncRotation"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("connectivity_status_bar"),
    color = animatedBgColor,
    contentColor = Color.White,
    shadowElevation = 3.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 7.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          painter = painterResource(id = R.drawable.img_app_logo),
          contentDescription = "FieldSync AI Logo",
          modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .testTag("connectivity_status_bar_logo"),
          contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("connectivity_status_pill")
        ) {
          if (isConnected) {
            Icon(
              imageVector = Icons.Default.Sync,
              contentDescription = "Syncing Icon",
              tint = Color(0xFF86EFAC),
              modifier = Modifier
                .size(16.dp)
                .rotate(syncRotation)
                .testTag("status_icon")
            )
          } else {
            Icon(
              imageVector = Icons.Default.CloudOff,
              contentDescription = "Offline Mode Icon",
              tint = Color(0xFFFCA5A5),
              modifier = Modifier
                .size(16.dp)
                .testTag("status_icon")
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Pulsing status dot
          Box(
            modifier = Modifier
              .size(7.dp)
              .clip(CircleShape)
              .background(
                if (isConnected) {
                  Color(0xFF22C55E).copy(alpha = pulseAlpha)
                } else {
                  Color(0xFFEF4444).copy(alpha = pulseAlpha)
                }
              )
              .testTag("connectivity_pulse_indicator")
          )

          Spacer(modifier = Modifier.width(6.dp))

          Column {
            Text(
              text = statusLabel,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.testTag("connectivity_status_text")
            )
            Text(
              text = statusSubtext,
              fontSize = 9.sp,
              color = Color.White.copy(alpha = 0.82f),
              lineHeight = 11.sp
            )
          }
        }
      }


      // Status badge pill on right
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isConnected) Color(0xFF22C55E).copy(alpha = 0.25f) else Color(0xFFF59E0B).copy(alpha = 0.3f)
          )
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = if (isConnected) "ONLINE" else "LOCAL",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
      }
    }
  }
}
