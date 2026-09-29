package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NetworkMode

@Composable
fun NetworkStatusBar(
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
  val (bgColor, icon, label) = when (networkMode) {
    NetworkMode.ONLINE -> Triple(
      Color(0xFF1B5E20),
      Icons.Default.CloudDone,
      "ONLINE (Cloud Gateway)"
    )
    NetworkMode.OFFLINE -> Triple(
      Color(0xFF263238),
      Icons.Default.CloudOff,
      "OFFLINE (Local Room DB)"
    )
    NetworkMode.SLOW_2G -> Triple(
      Color(0xFFE65100),
      Icons.Default.NetworkWifi,
      "SLOW 2G (High Latency)"
    )
  }

  val animatedBg by animateColorAsState(targetValue = bgColor, label = "statusBarBg")

  Column(modifier = modifier.fillMaxWidth()) {
    Surface(
      color = animatedBg,
      contentColor = Color.White,
      shadowElevation = 4.dp
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Network state pill & toggle trigger
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .clickable {
              val nextMode = when (networkMode) {
                NetworkMode.ONLINE -> NetworkMode.OFFLINE
                NetworkMode.OFFLINE -> NetworkMode.SLOW_2G
                NetworkMode.SLOW_2G -> NetworkMode.ONLINE
              }
              onToggleMode(nextMode)
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("network_mode_toggle")
        ) {
          Icon(
            imageVector = icon,
            contentDescription = "Network Mode",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }

        // Action indicators: Pending outbox, conflicts, and sync button
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (conflictCount > 0) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD32F2F))
                .clickable { onNavigateToConflicts() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("conflict_badge")
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Conflicts",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$conflictCount Conflicts",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          if (pendingCount > 0) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.25f))
                .clickable { onNavigateToSync() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("outbox_badge")
            ) {
              Text(
                text = "Outbox: $pendingCount",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          if (isSyncing) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.dp
            )
          } else {
            IconButton(
              onClick = onTriggerSync,
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .testTag("sync_action_button")
            ) {
              Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sync Now",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    // Ephemeral Sync Toast / Notice
    AnimatedVisibility(
      visible = syncMessage != null,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF263238))
          .padding(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Text(
          text = syncMessage ?: "",
          color = Color(0xFF81D4FA),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
