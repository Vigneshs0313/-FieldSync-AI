package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.entity.MediaUploadEntity
import com.example.ui.components.ResumableMediaUploadCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResumableMediaUploadTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun testChunkCalculationAndProgressFormatting() {
    val totalBytes = 13107200L // 12.5 MB
    val chunkSize = 262144 // 256 KB
    val totalChunks = MediaUploadEntity.calculateTotalChunks(totalBytes, chunkSize)
    assertEquals(50, totalChunks)

    val upload = MediaUploadEntity(
      id = "test-upl-001",
      inspectionId = "insp-001",
      fileName = "Turbine_Scan.mp4",
      fileType = "VIDEO",
      mimeType = "video/mp4",
      totalBytes = totalBytes,
      bytesUploaded = 6553600L, // 50%
      chunkSizeBytes = chunkSize,
      currentChunkIndex = 25,
      totalChunks = totalChunks,
      status = "UPLOADING"
    )

    assertEquals(0.5f, upload.progress, 0.01f)
    assertEquals(50, upload.progressPercent)
    assertTrue(upload.isUploading)
    assertFalse(upload.isPaused)
    assertFalse(upload.isFinished)
    assertEquals("12.5 MB", upload.formattedTotalSize)
    assertEquals("6.3 MB", upload.formattedUploadedSize)
  }

  @Test
  fun testPausedStateFlags() {
    val uploadOffline = MediaUploadEntity(
      id = "test-upl-002",
      inspectionId = "insp-001",
      fileName = "Audio_Waveform.wav",
      fileType = "AUDIO",
      mimeType = "audio/wav",
      totalBytes = 4194304L,
      bytesUploaded = 1048576L,
      status = "PAUSED_OFFLINE"
    )

    assertTrue(uploadOffline.isPaused)
    assertFalse(uploadOffline.isUploading)
    assertFalse(uploadOffline.isFinished)

    val uploadCompleted = uploadOffline.copy(
      bytesUploaded = 4194304L,
      status = "COMPLETED",
      remoteBlobUrl = "https://storage.fieldsync.ai/blobs/Audio_Waveform.wav",
      sha256Checksum = "abc123hash"
    )

    assertTrue(uploadCompleted.isFinished)
    assertFalse(uploadCompleted.isPaused)
    assertFalse(uploadCompleted.isUploading)
    assertEquals(100, uploadCompleted.progressPercent)
  }

  @Test
  fun testResumableMediaUploadCardRendersWithControls() {
    val upload = MediaUploadEntity(
      id = "upl-card-test",
      inspectionId = "insp-001",
      fileName = "Stator_Thermogram.raw",
      fileType = "THERMAL",
      mimeType = "image/x-raw",
      totalBytes = 8388608L, // 8.0 MB
      bytesUploaded = 4194304L, // 4.0 MB
      chunkSizeBytes = 262144,
      currentChunkIndex = 16,
      totalChunks = 32,
      status = "UPLOADING"
    )

    var pauseClicked = false

    composeTestRule.setContent {
      ResumableMediaUploadCard(
        upload = upload,
        onPause = { pauseClicked = true },
        onResume = {},
        onCancel = {},
        onRetry = {},
        onDelete = {}
      )
    }

    composeTestRule.onNodeWithTag("resumable_upload_card_upl-card-test")
      .assertIsDisplayed()

    composeTestRule.onNodeWithTag("pause_upload_upl-card-test")
      .assertIsDisplayed()
  }
}
