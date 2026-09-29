package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.entity.MediaUploadEntity
import com.example.data.entity.SyncQueueItemEntity
import com.example.ui.components.ResumableMediaUploadCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.viewmodel.FieldSyncViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncCenterScreen(
  viewModel: FieldSyncViewModel,
  onNavigateToConflicts: () -> Unit,
  modifier: Modifier = Modifier
) {
  val networkMode by viewModel.networkMode.collectAsState()
  val isSyncing by viewModel.isSyncing.collectAsState()
  val queueItems by viewModel.syncQueue.collectAsState()
  val pendingCount by viewModel.pendingQueueCount.collectAsState()
  val conflictCount by viewModel.unresolvedConflictCount.collectAsState()
  val mediaUploads by viewModel.allMediaUploads.collectAsState()
  val activeUploadCount by viewModel.activeMediaUploadCount.collectAsState()


  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Network Simulation Control Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.NetworkCheck,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Network Topology Simulator",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            StatusBadge(
              status = when (networkMode) {
                NetworkMode.ONLINE -> "ONLINE"
                NetworkMode.OFFLINE -> "OFFLINE"
                NetworkMode.SLOW_2G -> "SLOW_2G"
              }
            )
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "Switch connectivity mode to evaluate offline storage persistence, degraded connection backoff, and instant cloud sync.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))

          // 3 Mode Selector Pills
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // ONLINE
            NetworkModePill(
              label = "Online",
              sub = "150ms",
              isSelected = networkMode == NetworkMode.ONLINE,
              activeColor = Color(0xFF16A34A),
              onClick = { viewModel.setNetworkMode(NetworkMode.ONLINE) },
              modifier = Modifier.weight(1f).testTag("select_online_mode")
            )
            // OFFLINE
            NetworkModePill(
              label = "Offline",
              sub = "Room DB",
              isSelected = networkMode == NetworkMode.OFFLINE,
              activeColor = Color(0xFF475569),
              onClick = { viewModel.setNetworkMode(NetworkMode.OFFLINE) },
              modifier = Modifier.weight(1f).testTag("select_offline_mode")
            )
            // SLOW 2G
            NetworkModePill(
              label = "Slow 2G",
              sub = "3000ms",
              isSelected = networkMode == NetworkMode.SLOW_2G,
              activeColor = Color(0xFFD97706),
              onClick = { viewModel.setNetworkMode(NetworkMode.SLOW_2G) },
              modifier = Modifier.weight(1f).testTag("select_slow_2g_mode")
            )
          }
        }
      }
    }

    // Force Sync & Actions Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Sync Dispatcher",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "$pendingCount pending mutations waiting in outbox",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Button(
              onClick = { viewModel.triggerSync() },
              enabled = !isSyncing && networkMode != NetworkMode.OFFLINE,
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("force_sync_button")
            ) {
              if (isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Syncing...", fontSize = 12.sp)
              } else {
                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Force Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Secondary test actions
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { viewModel.simulateRemoteConflict() },
              modifier = Modifier.weight(1f).testTag("simulate_conflict_in_sync"),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Simulate Conflict", fontSize = 11.sp)
            }

            if (conflictCount > 0) {
              Button(
                onClick = onNavigateToConflicts,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Text("Resolve ($conflictCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // Retry & Backoff Specification Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "Exponential Backoff & Replay Policy",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF38BDF8)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Base retry: 2.0s • Max jitter backoff: 32.0s • Idempotent UUID deduplication guarantees at-least-once transactional delivery without redundant server writes.",
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 15.sp
          )
        }
      }
    }

    // Outbox Mutation Queue Items
    item {
      SectionHeader(
        title = "Outbox Mutation Queue (${queueItems.size})",
        subtitle = "Local events dispatched sequentially to cloud gateway"
      )
    }

    if (queueItems.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp)
        ) {
          Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Sync outbox is empty. All local events delivered.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(queueItems) { item ->
        SyncQueueItemRow(item = item)
      }
    }

    // Resumable Media Upload Streams Section
    item {
      Spacer(modifier = Modifier.height(10.dp))
      SectionHeader(
        title = "Resumable Media Transfers (${mediaUploads.size})",
        subtitle = "TUS chunk ranges with byte-level progress tracking"
      )
    }

    // Global Batch Media Controls
    item {
      val totalBytesAll = mediaUploads.sumOf { it.totalBytes }
      val totalUploadedAll = mediaUploads.sumOf { it.bytesUploaded }
      val formattedTotal = MediaUploadEntity.formatBytes(totalBytesAll)
      val formattedUploaded = MediaUploadEntity.formatBytes(totalUploadedAll)

      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Media Stream Bandwidth",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "$formattedUploaded / $formattedTotal transferred • $activeUploadCount active",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = { viewModel.pauseAllMediaUploads() },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("pause_all_media_button")
              ) {
                Icon(imageVector = Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pause All", fontSize = 11.sp)
              }

              Button(
                onClick = { viewModel.resumeAllMediaUploads() },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("resume_all_media_button")
              ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Resume All", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    if (mediaUploads.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp)
        ) {
          Box(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No media streams in queue. Initiate large media streams from Inspection Details.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(mediaUploads) { upload ->
        ResumableMediaUploadCard(
          upload = upload,
          onPause = { viewModel.pauseMediaUpload(upload.id) },
          onResume = { viewModel.resumeMediaUpload(upload.id) },
          onCancel = { viewModel.cancelMediaUpload(upload.id) },
          onRetry = { viewModel.retryMediaUpload(upload.id) },
          onDelete = { viewModel.deleteMediaUpload(upload.id) }
        )
      }
    }
  }
}


@Composable
fun NetworkModePill(
  label: String,
  sub: String,
  isSelected: Boolean,
  activeColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .clickable { onClick() }
      .padding(vertical = 8.dp, horizontal = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = sub,
        fontSize = 10.sp,
        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
      )
    }
  }
}

@Composable
fun SyncQueueItemRow(item: SyncQueueItemEntity) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(10.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(
            when (item.status) {
              "COMPLETED" -> Color(0xFFDCFCE7)
              "SYNCING" -> Color(0xFFDBEAFE)
              "FAILED" -> Color(0xFFFEE2E2)
              else -> Color(0xFFFEF3C7)
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when (item.status) {
            "COMPLETED" -> Icons.Default.CloudDone
            "SYNCING" -> Icons.Default.Sync
            "FAILED" -> Icons.Default.Warning
            else -> Icons.Default.Schedule
          },
          contentDescription = null,
          tint = when (item.status) {
            "COMPLETED" -> Color(0xFF16A34A)
            "SYNCING" -> Color(0xFF2563EB)
            "FAILED" -> Color(0xFFDC2626)
            else -> Color(0xFFD97706)
          },
          modifier = Modifier.size(16.dp)
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "${item.entityType} [${item.action}]",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = item.status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = when (item.status) {
              "COMPLETED" -> Color(0xFF16A34A)
              "SYNCING" -> Color(0xFF2563EB)
              "FAILED" -> Color(0xFFDC2626)
              else -> Color(0xFFD97706)
            }
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = item.summary,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "${formatTime(item.timestamp)} • Retry: ${item.retryCount}",
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    }
  }
}

private fun formatTime(timestamp: Long): String {
  val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
