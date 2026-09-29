package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.NetworkMode


/**
 * Persistent Connectivity Status Bar Component.
 * Automatically detects real device network state and supports simulated field conditions.
 * Displays "Syncing" when online/syncing and "Offline Mode" when disconnected.
 */
@Composable
fun PersistentConnectivityStatusBar(
  isOnline: Boolean,
  networkMode: NetworkMode,
  isSyncing: Boolean,
  pendingCount: Int,
  conflictCount: Int,
  syncMessage: String?,
  onToggleMode: (NetworkMode) -> Unit,
  onTriggerSync: () -> Unit,
  onNavigateToConflicts: () -> Unit,
  onNavigateToSync: () -> Unit,
  modifier: Modifier = Modifier
) {
  // Determine primary display state based on connectivity detection
  val isDisconnected = !isOnline || networkMode == NetworkMode.OFFLINE

  // Primary status label: explicitly "Syncing" when online, "Offline Mode" when disconnected
  val statusLabel = if (isDisconnected) {
    "Offline Mode"
  } else if (isSyncing) {
    "Syncing..."
  } else {
    "Syncing"
  }

  val statusSubtext = when {
    isDisconnected -> "Room DB Local Persistence"
    isSyncing -> "Replicating Outbox to Cloud"
    networkMode == NetworkMode.SLOW_2G -> "Slow 2G • Cloud Sync Active"
    pendingCount > 0 -> "$pendingCount mutations in outbox"
    else -> "Cloud Gateway Connected"
  }

  // Color theme dynamically adapting to network state
  val targetBgColor = when {
    isDisconnected -> Color(0xFF1E293B) // Slate Dark for Offline Mode
    networkMode == NetworkMode.SLOW_2G -> Color(0xFF9A3412) // Amber Dark for Slow 2G
    isSyncing -> Color(0xFF0369A1) // Sky/Ocean Blue for active syncing
    else -> Color(0xFF14532D) // Forest Emerald Green for nominal online syncing
  }

  val animatedBgColor by animateColorAsState(
    targetValue = targetBgColor,
    animationSpec = tween(durationMillis = 400),
    label = "statusBarBackground"
  )

  // Pulsing animation for active sync / live connection dot
  val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  // Continuous rotation for sync icon when actively syncing
  val syncRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "syncRotation"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("persistent_connectivity_status_bar"),
    color = animatedBgColor,
    contentColor = Color.White,
    shadowElevation = 4.dp
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left side: Brand Logo + Status badge & indicator (Shows "Syncing" or "Offline Mode")
        Row(verticalAlignment = Alignment.CenterVertically) {
          Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = "FieldSync AI Logo",
            modifier = Modifier
              .size(26.dp)
              .clip(RoundedCornerShape(6.dp))
              .testTag("status_bar_brand_logo"),
            contentScale = ContentScale.Crop
          )
          Spacer(modifier = Modifier.width(8.dp))

          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color.White.copy(alpha = 0.16f))
              .clickable {
                // Quick-toggle simulation cycle for technician demonstration
                val nextMode = when (networkMode) {
                  NetworkMode.ONLINE -> NetworkMode.OFFLINE
                  NetworkMode.OFFLINE -> NetworkMode.SLOW_2G
                  NetworkMode.SLOW_2G -> NetworkMode.ONLINE
                }
                onToggleMode(nextMode)
              }
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("connectivity_mode_pill")
          ) {
            // Status icon
            if (isDisconnected) {
              Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = "Offline Mode Icon",
                tint = Color(0xFFFCA5A5),
                modifier = Modifier
                  .size(16.dp)
                  .testTag("connectivity_offline_badge")
              )
            } else {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Syncing Icon",
                tint = Color(0xFF86EFAC),
                modifier = Modifier
                  .size(16.dp)
                  .rotate(if (isSyncing) syncRotation else 0f)
                  .testTag("connectivity_syncing_indicator")
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Live pulse dot
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(
                  if (isDisconnected) {
                    Color(0xFFEF4444).copy(alpha = pulseAlpha)
                  } else {
                    Color(0xFF22C55E).copy(alpha = pulseAlpha)
                  }
                )
                .testTag("connectivity_status_indicator")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Core Status Label (Mandatory: "Syncing" or "Offline Mode")
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


        // Right side: Outbox mutation counter, conflict alert, and manual force sync
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Unresolved Conflict Warning Pill
          if (conflictCount > 0) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDC2626))
                .clickable { onNavigateToConflicts() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("conflict_badge")
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Conflicts Alert",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$conflictCount Conflicts",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          // Outbox Mutations Badge
          if (pendingCount > 0) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                  if (isDisconnected) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.22f)
                )
                .clickable { onNavigateToSync() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("outbox_count_badge")
            ) {
              Text(
                text = "Outbox: $pendingCount",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDisconnected) Color.Black else Color.White
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          // Sync Action Button or Circular Progress
          if (isSyncing) {
            CircularProgressIndicator(
              modifier = Modifier
                .size(20.dp)
                .padding(2.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            IconButton(
              onClick = onTriggerSync,
              enabled = !isDisconnected,
              modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (isDisconnected) 0.08f else 0.2f))
                .testTag("sync_trigger_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Trigger Sync",
                tint = if (isDisconnected) Color.White.copy(alpha = 0.4f) else Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
      }

      // Ephemeral Sync Notification / Feedback Banner
      AnimatedVisibility(
        visible = syncMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F172A))
            .padding(horizontal = 14.dp, vertical = 5.dp)
        ) {
          Text(
            text = syncMessage ?: "",
            color = Color(0xFF38BDF8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
