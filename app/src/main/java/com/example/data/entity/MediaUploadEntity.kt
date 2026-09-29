package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a resumable media upload session for field inspections.
 * Tracks byte-level offset and chunk progression to allow uninterrupted uploads
 * over unstable industrial connectivity (loss of Wi-Fi / cellular).
 */
@Entity(tableName = "media_uploads")
data class MediaUploadEntity(
  @PrimaryKey val id: String,
  val inspectionId: String,
  val checklistItemId: String? = null,
  val fileName: String,
  val fileType: String = "IMAGE", // IMAGE, VIDEO, THERMAL, AUDIO, DOCUMENT
  val mimeType: String = "image/jpeg",
  val localUri: String = "",
  val totalBytes: Long,
  val bytesUploaded: Long = 0L,
  val chunkSizeBytes: Int = 262144, // 256 KB chunk
  val currentChunkIndex: Int = 0,
  val totalChunks: Int = 1,
  val status: String = "QUEUED", // QUEUED, UPLOADING, PAUSED_OFFLINE, PAUSED_USER, COMPLETED, FAILED, CANCELLED
  val uploadSpeedKbps: Double = 0.0,
  val remoteBlobUrl: String? = null,
  val sha256Checksum: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val completedAt: Long? = null,
  val errorMessage: String? = null
) {
  val progress: Float
    get() = if (totalBytes > 0) (bytesUploaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f) else 0f

  val progressPercent: Int
    get() = (progress * 100).toInt()

  val isFinished: Boolean
    get() = status == "COMPLETED" || status == "FAILED" || status == "CANCELLED"

  val isPaused: Boolean
    get() = status == "PAUSED_OFFLINE" || status == "PAUSED_USER"

  val isUploading: Boolean
    get() = status == "UPLOADING"

  val formattedTotalSize: String
    get() = formatBytes(totalBytes)

  val formattedUploadedSize: String
    get() = formatBytes(bytesUploaded)

  companion object {
    fun formatBytes(bytes: Long): String {
      return when {
        bytes >= 1024 * 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f GB", bytes.toDouble() / (1024 * 1024 * 1024))
        bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
        bytes >= 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes.toDouble() / 1024)
        else -> "$bytes B"
      }
    }

    fun calculateTotalChunks(totalBytes: Long, chunkSize: Int): Int {
      if (totalBytes <= 0 || chunkSize <= 0) return 1
      return Math.ceil(totalBytes.toDouble() / chunkSize.toDouble()).toInt().coerceAtLeast(1)
    }
  }
}
