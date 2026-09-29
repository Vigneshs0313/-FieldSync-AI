package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.MediaUploadEntity

@Composable
fun ResumableMediaUploadCard(
  upload: MediaUploadEntity,
  onPause: () -> Unit,
  onResume: () -> Unit,
  onCancel: () -> Unit,
  onRetry: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDetails by remember { mutableStateOf(false) }

  val typeIcon: ImageVector = when (upload.fileType.uppercase()) {
    "VIDEO" -> Icons.Default.Videocam
    "AUDIO" -> Icons.Default.Audiotrack
    "THERMAL" -> Icons.Default.Thermostat
    "DOCUMENT" -> Icons.Default.Description
    else -> Icons.Default.Image
  }

  val typeColor: Color = when (upload.fileType.uppercase()) {
    "VIDEO" -> Color(0xFF8B5CF6)
    "AUDIO" -> Color(0xFFEC4899)
    "THERMAL" -> Color(0xFFF97316)
    "DOCUMENT" -> Color(0xFF3B82F6)
    else -> Color(0xFF10B981)
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("resumable_upload_card_${upload.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header: File Type Icon, File Name, and Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(typeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = typeIcon,
              contentDescription = upload.fileType,
              tint = typeColor,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = upload.fileName,
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
            Text(
              text = "${upload.fileType} • ${upload.formattedTotalSize}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        UploadStatusPill(upload = upload)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress Bar
      LinearProgressIndicator(
        progress = { upload.progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = when (upload.status) {
          "COMPLETED" -> Color(0xFF10B981)
          "PAUSED_OFFLINE" -> Color(0xFFF59E0B)
          "PAUSED_USER" -> Color(0xFF6B7280)
          "FAILED" -> Color(0xFFEF4444)
          else -> MaterialTheme.colorScheme.primary
        },
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Chunk Blocks Visualization (simulates chunked transfer progress)
      ChunkBlocksVisualizer(
        totalChunks = upload.totalChunks,
        uploadedChunks = upload.currentChunkIndex,
        isUploading = upload.isUploading,
        status = upload.status
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Meta Stats & Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Chunk ${upload.currentChunkIndex}/${upload.totalChunks} • ${upload.formattedUploadedSize} (${upload.progressPercent}%)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
          )
          if (upload.isUploading && upload.uploadSpeedKbps > 0) {
            Text(
              text = "Transfer speed: ${String.format(java.util.Locale.US, "%.1f", upload.uploadSpeedKbps)} KB/s",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.primary
            )
          } else if (upload.status == "PAUSED_OFFLINE") {
            Text(
              text = "Auto-resumes when online",
              fontSize = 10.sp,
              color = Color(0xFFD97706)
            )
          } else if (upload.status == "COMPLETED") {
            Text(
              text = "Verified SHA-256 integrity",
              fontSize = 10.sp,
              color = Color(0xFF059669)
            )
          }
        }

        // Action Buttons Row
        Row(verticalAlignment = Alignment.CenterVertically) {
          when (upload.status) {
            "UPLOADING" -> {
              IconButton(
                onClick = onPause,
                modifier = Modifier
                  .size(32.dp)
                  .testTag("pause_upload_${upload.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.Pause,
                  contentDescription = "Pause Upload",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            "PAUSED_USER", "PAUSED_OFFLINE" -> {
              IconButton(
                onClick = onResume,
                modifier = Modifier
                  .size(32.dp)
                  .testTag("resume_upload_${upload.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Resume Upload",
                  tint = Color(0xFF10B981),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            "FAILED", "CANCELLED" -> {
              IconButton(
                onClick = onRetry,
                modifier = Modifier
                  .size(32.dp)
                  .testTag("retry_upload_${upload.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Retry Upload",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          if (!upload.isFinished) {
            IconButton(
              onClick = onCancel,
              modifier = Modifier
                .size(32.dp)
                .testTag("cancel_upload_${upload.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel Upload",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
              )
            }
          } else {
            IconButton(
              onClick = onDelete,
              modifier = Modifier
                .size(32.dp)
                .testTag("delete_upload_${upload.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Record",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(16.dp)
              )
            }
          }

          IconButton(
            onClick = { showDetails = !showDetails },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Toggle Protocol Details",
              tint = if (showDetails) MaterialTheme.colorScheme.primary else Color(0xFF9CA3AF),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Expandable Technical Specs: TUS / Byte Offset Details
      AnimatedVisibility(visible = showDetails) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(10.dp)
        ) {
          Text(
            text = "RESUMABLE PROTOCOL PROTOCOL (RFC-TUS)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Offset: ${upload.bytesUploaded} / ${upload.totalBytes} bytes",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFCBD5E1)
          )
          Text(
            text = "Chunk Size: ${upload.chunkSizeBytes / 1024} KB per slice",
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFCBD5E1)
          )
          if (upload.remoteBlobUrl != null) {
            Text(
              text = "Blob URL: ${upload.remoteBlobUrl}",
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              color = Color(0xFF4ADE80),
              maxLines = 1
            )
          }
          if (upload.sha256Checksum.isNotEmpty()) {
            Text(
              text = "SHA-256: ${upload.sha256Checksum}",
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace,
              color = Color(0xFFFCD34D),
              maxLines = 1
            )
          }
        }
      }
    }
  }
}

@Composable
fun UploadStatusPill(upload: MediaUploadEntity) {
  val (label, bgColor, textColor, icon) = when (upload.status) {
    "COMPLETED" -> Quad(
      "Completed",
      Color(0xFFDCFCE7),
      Color(0xFF166534),
      Icons.Default.CheckCircle
    )
    "UPLOADING" -> Quad(
      "Uploading",
      Color(0xFFDBEAFE),
      Color(0xFF1E40AF),
      Icons.Default.Refresh
    )
    "PAUSED_OFFLINE" -> Quad(
      "Paused (Offline)",
      Color(0xFFFEF3C7),
      Color(0xFF92400E),
      Icons.Default.WifiOff
    )
    "PAUSED_USER" -> Quad(
      "Paused",
      Color(0xFFF3F4F6),
      Color(0xFF4B5563),
      Icons.Default.Pause
    )
    "FAILED" -> Quad(
      "Failed",
      Color(0xFFFEE2E2),
      Color(0xFF991B1B),
      Icons.Default.Error
    )
    "CANCELLED" -> Quad(
      "Cancelled",
      Color(0xFFF3F4F6),
      Color(0xFF6B7280),
      Icons.Default.Close
    )
    else -> Quad(
      "Queued",
      Color(0xFFF1F5F9),
      Color(0xFF475569),
      Icons.Default.PlayArrow
    )
  }

  Surface(
    shape = CircleShape,
    color = bgColor
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = textColor,
        modifier = Modifier.size(11.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor
      )
    }
  }
}

@Composable
fun ChunkBlocksVisualizer(
  totalChunks: Int,
  uploadedChunks: Int,
  isUploading: Boolean,
  status: String,
  modifier: Modifier = Modifier
) {
  // Represent chunk progress visually with 14 segments
  val numSegments = 14
  val completedSegments = if (totalChunks > 0) {
    ((uploadedChunks.toFloat() / totalChunks.toFloat()) * numSegments).toInt().coerceIn(0, numSegments)
  } else 0

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(2.dp)
  ) {
    for (i in 0 until numSegments) {
      val isUploaded = i < completedSegments
      val isCurrent = isUploading && (i == completedSegments)

      val blockColor = when {
        isUploaded -> Color(0xFF10B981)
        isCurrent -> MaterialTheme.colorScheme.primary
        status == "PAUSED_OFFLINE" && i < completedSegments + 1 -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      }

      Box(
        modifier = Modifier
          .weight(1f)
          .height(3.dp)
          .clip(RoundedCornerShape(1.dp))
          .background(blockColor)
      )
    }
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
