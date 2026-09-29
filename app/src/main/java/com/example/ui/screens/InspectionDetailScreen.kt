package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.InspectAIReportSection
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChecklistItemEntity
import com.example.data.entity.InspectionPhotoEntity
import com.example.data.entity.MediaUploadEntity
import com.example.ui.components.ResumableMediaUploadCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SeverityBadge
import com.example.ui.components.StatusBadge
import com.example.viewmodel.FieldSyncViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InspectionDetailScreen(
  viewModel: FieldSyncViewModel,
  inspectionId: String,
  onBack: () -> Unit,
  onNavigateToAI: () -> Unit,
  onNavigateToCamera: (String, String?) -> Unit,
  modifier: Modifier = Modifier
) {
  val inspection by viewModel.selectedInspection.collectAsState()
  val checklistItems by viewModel.checklistItems.collectAsState()
  val photos by viewModel.photos.collectAsState()
  val mediaUploads by viewModel.inspectionMediaUploads.collectAsState()

  var showPhotoDialog by remember { mutableStateOf(false) }
  var showUploadDialog by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<ChecklistItemEntity?>(null) }


  if (inspection == null) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text("Inspection not found.", color = MaterialTheme.colorScheme.onSurface)
    }
    return
  }

  val insp = inspection!!

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Top Bar with Back and AI Action
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("back_button")
          ) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
          Column {
            Text(
              text = insp.assetTag,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = insp.assetName,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Button(
          onClick = {
            viewModel.runInspectAI()
            onNavigateToAI()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7C3AED),
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("run_inspect_ai_button")
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("InspectAI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // Asset Header Overview Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = insp.facility,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusBadge(status = insp.status)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = insp.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Assigned: ${insp.assignedTechnician}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (insp.hasLocalChanges) {
              Text(
                text = "● Offline edits in Room",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
              )
            } else {
              Text(
                text = "✓ Synced with Cloud",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
              )
            }
          }
        }
      }
    }

    // InspectAI Report Intelligence Section
    item {
      InspectAIReportSection(
        inspection = insp,
        items = checklistItems
      )
    }
    // Mandatory Checklist Section (Motor condition, Oil level, Temperature, Emergency switch, Panel cover, Safety equipment)
    item {
      SectionHeader(
        title = "Mandatory Field Checklist",
        subtitle = "Tap status or edit values to persist offline instantly"
      )
    }

    items(checklistItems) { item ->
      ChecklistItemCard(
        item = item,
        onStatusChange = { newStatus ->
          viewModel.toggleChecklistStatus(item, newStatus)
        },
        onEditClick = {
          editingItem = item
        },
        onCameraClick = {
          onNavigateToCamera(inspectionId, item.id)
        }
      )
    }

    // Photo Attachments Section
    item {
      Spacer(modifier = Modifier.height(6.dp))
      SectionHeader(
        title = "Verification Photos (${photos.size})",
        subtitle = "Photo documentation stored offline via CameraX",
        actionText = "+ Camera Capture",
        onAction = { onNavigateToCamera(inspectionId, null) }
      )
    }

    if (photos.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Image,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "No verification photos attached. Tap '+ Attach Photo' to document asset state.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(photos) { photo ->
            PhotoThumbnailCard(photo = photo)
          }
        }
      }
    }

    // Resumable Media Uploads Section (TUS chunked binary transfers)
    item {
      Spacer(modifier = Modifier.height(10.dp))
      SectionHeader(
        title = "Resumable Media Streams (${mediaUploads.size})",
        subtitle = "Chunked binary ranges with offline auto-resume",
        actionText = "+ Stream Media",
        onAction = { showUploadDialog = true }
      )
    }

    if (mediaUploads.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CloudUpload,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "No large media streams attached. Tap '+ Stream Media' to test resumable chunked video, audio, or thermograms.",
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

  // Dialog for editing item values & notes
  if (editingItem != null) {
    EditChecklistItemDialog(
      item = editingItem!!,
      onDismiss = { editingItem = null },
      onSave = { value, notes ->
        viewModel.updateNumericAndNotes(editingItem!!, value, notes)
        editingItem = null
      }
    )
  }

  // Dialog for adding photo
  if (showPhotoDialog) {
    AddPhotoDialog(
      onDismiss = { showPhotoDialog = false },
      onAddPhoto = { caption, type ->
        viewModel.addPhoto(caption, type)
        showPhotoDialog = false
      }
    )
  }

  // Dialog for initiating resumable media upload
  if (showUploadDialog) {
    StartResumableUploadDialog(
      onDismiss = { showUploadDialog = false },
      onStartUpload = { fileName, fileType, mimeType, totalBytes ->
        viewModel.startResumableUpload(
          fileName = fileName,
          fileType = fileType,
          mimeType = mimeType,
          totalBytes = totalBytes
        )
        showUploadDialog = false
      }
    )
  }
}


@Composable
fun ChecklistItemCard(
  item: ChecklistItemEntity,
  onStatusChange: (String) -> Unit,
  onEditClick: () -> Unit,
  onCameraClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("checklist_item_${item.id}"),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = item.itemName,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            SeverityBadge(severity = item.severity)
          }
          Text(
            text = "Category: ${item.category} • Updated by ${item.lastModifiedBy}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onCameraClick,
            modifier = Modifier.size(32.dp).testTag("camera_button_${item.id}")
          ) {
            Icon(
              imageVector = Icons.Default.AddAPhoto,
              contentDescription = "Capture photo for checklist item",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          IconButton(
            onClick = onEditClick,
            modifier = Modifier.size(32.dp).testTag("edit_button_${item.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit values & notes",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // Display numeric value if available
      if (item.numericValue != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Observed Reading: ${item.numericValue} ${item.unit ?: ""}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      if (item.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Note: ${item.notes}",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 16.sp
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status selector buttons: PASS, FAIL, FLAG
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // PASS Button
        val isPass = item.status == "PASS"
        Button(
          onClick = { onStatusChange("PASS") },
          modifier = Modifier
            .weight(1f)
            .testTag("pass_button_${item.id}"),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isPass) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
            contentColor = if (isPass) Color.White else Color(0xFF2E7D32)
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("PASS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // FAIL Button
        val isFail = item.status == "FAIL"
        Button(
          onClick = { onStatusChange("FAIL") },
          modifier = Modifier
            .weight(1f)
            .testTag("fail_button_${item.id}"),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isFail) Color(0xFFC62828) else Color(0xFFFFEBEE),
            contentColor = if (isFail) Color.White else Color(0xFFC62828)
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("FAIL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        // FLAG Button
        val isFlag = item.status == "FLAG"
        Button(
          onClick = { onStatusChange("FLAG") },
          modifier = Modifier
            .weight(1f)
            .testTag("flag_button_${item.id}"),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isFlag) Color(0xFFEF6C00) else Color(0xFFFFF3E0),
            contentColor = if (isFlag) Color.White else Color(0xFFEF6C00)
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("FLAG", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun PhotoThumbnailCard(photo: InspectionPhotoEntity) {
  Card(
    modifier = Modifier
      .width(160.dp)
      .height(130.dp),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(8.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(70.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(
            when (photo.photoType) {
              "THERMAL" -> Color(0xFFB91C1C)
              "MOTOR" -> Color(0xFF1E3A8A)
              "SWITCH" -> Color(0xFFB45309)
              else -> Color(0xFF334155)
            }
          ),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "[${photo.photoType} VIEW]",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Column {
        Text(
          text = photo.caption,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1
        )
        Text(
          text = formatTime(photo.timestamp),
          fontSize = 9.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun EditChecklistItemDialog(
  item: ChecklistItemEntity,
  onDismiss: () -> Unit,
  onSave: (Double?, String) -> Unit
) {
  var valueText by remember { mutableStateOf(item.numericValue?.toString() ?: "") }
  var notesText by remember { mutableStateOf(item.notes) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Edit ${item.itemName}",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Update readings and observations. Changes will be saved in local Room DB and queued for sync.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = valueText,
          onValueChange = { valueText = it },
          label = { Text("Reading Value (${item.unit ?: "units"})") },
          modifier = Modifier.fillMaxWidth().testTag("numeric_value_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = notesText,
          onValueChange = { notesText = it },
          label = { Text("Technician Notes") },
          modifier = Modifier.fillMaxWidth().testTag("technician_notes_input"),
          minLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val num = valueText.toDoubleOrNull()
          onSave(num, notesText)
        },
        modifier = Modifier.testTag("save_checklist_edit_button")
      ) {
        Text("Save Offline")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun AddPhotoDialog(
  onDismiss: () -> Unit,
  onAddPhoto: (String, String) -> Unit
) {
  var caption by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf("MOTOR") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Attach Verification Photo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Select photo documentation category and enter verification caption.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text("Photo Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("MOTOR", "THERMAL", "SWITCH", "GENERAL").forEach { type ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (selectedType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { selectedType = type }
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Text(
                text = type,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedType == type) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        OutlinedTextField(
          value = caption,
          onValueChange = { caption = it },
          label = { Text("Photo Caption / Telemetry Details") },
          placeholder = { Text("e.g. Stator core thermal scan showing 88°C hot-spot") },
          modifier = Modifier.fillMaxWidth().testTag("photo_caption_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (caption.isNotBlank()) {
            onAddPhoto(caption, selectedType)
          }
        },
        modifier = Modifier.testTag("confirm_attach_photo_button")
      ) {
        Text("Attach & Queue")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private fun formatTime(timestamp: Long): String {
  val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  return sdf.format(Date(timestamp))
}

@Composable
fun StartResumableUploadDialog(
  onDismiss: () -> Unit,
  onStartUpload: (fileName: String, fileType: String, mimeType: String, totalBytes: Long) -> Unit
) {
  val presets = listOf(
    UploadPreset(
      name = "4K_Stator_Thermal_Scan.mp4",
      label = "4K Stator Thermal Scan Video",
      type = "VIDEO",
      mimeType = "video/mp4",
      sizeBytes = 13107200L, // 12.5 MB
      sizeDesc = "12.5 MB • 50 Chunks (256 KB/chunk)"
    ),
    UploadPreset(
      name = "Bearing_Harmonic_Acoustic.wav",
      label = "Bearing Harmonic Acoustic Waveform",
      type = "AUDIO",
      mimeType = "audio/wav",
      sizeBytes = 4404019L, // 4.2 MB
      sizeDesc = "4.2 MB • 17 Chunks (256 KB/chunk)"
    ),
    UploadPreset(
      name = "Transformer_IR_Thermogram.raw",
      label = "Transformer IR Thermogram Multi-Frame",
      type = "THERMAL",
      mimeType = "image/x-raw",
      sizeBytes = 8808038L, // 8.4 MB
      sizeDesc = "8.4 MB • 34 Chunks (256 KB/chunk)"
    ),
    UploadPreset(
      name = "Shaft_Coupling_MicroFracture_HiRes.jpg",
      label = "High-Res Optical Micro-Fracture Capture",
      type = "IMAGE",
      mimeType = "image/jpeg",
      sizeBytes = 3250585L, // 3.1 MB
      sizeDesc = "3.1 MB • 13 Chunks (256 KB/chunk)"
    )
  )

  var selectedPresetIndex by remember { mutableStateOf(0) }
  var isCustom by remember { mutableStateOf(false) }
  var customName by remember { mutableStateOf("Turbine_Vibration_Telemetry.dat") }
  var customSizeMb by remember { mutableDoubleStateOf(5.0) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CloudUpload,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Initiate Resumable Media Stream", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Resumable uploads use RFC-TUS chunked binary ranges. If network drops, the transfer pauses and auto-resumes at the exact byte offset.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 15.sp
        )

        Text("Select Media Stream Preset:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          presets.forEachIndexed { index, preset ->
            val isSelected = !isCustom && selectedPresetIndex == index
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable {
                  selectedPresetIndex = index
                  isCustom = false
                }
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = {
                  selectedPresetIndex = index
                  isCustom = false
                },
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = preset.label,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${preset.type} • ${preset.sizeDesc}",
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          // Custom Option
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(if (isCustom) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
              .clickable { isCustom = true }
              .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = isCustom,
              onClick = { isCustom = true },
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Custom Large Payload / Binary Stream",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Define custom file name and byte size",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        if (isCustom) {
          OutlinedTextField(
            value = customName,
            onValueChange = { customName = it },
            label = { Text("Stream File Name") },
            modifier = Modifier.fillMaxWidth().testTag("custom_stream_name_input")
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Payload Size: ${String.format(Locale.US, "%.1f", customSizeMb)} MB", fontSize = 11.sp)
            Text("${(customSizeMb * 4).toInt()} Chunks", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
          }
          Slider(
            value = customSizeMb.toFloat(),
            onValueChange = { customSizeMb = it.toDouble() },
            valueRange = 1f..30f,
            steps = 29
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (isCustom) {
            val totalBytes = (customSizeMb * 1024 * 1024).toLong()
            onStartUpload(customName, "DOCUMENT", "application/octet-stream", totalBytes)
          } else {
            val preset = presets[selectedPresetIndex]
            onStartUpload(preset.name, preset.type, preset.mimeType, preset.sizeBytes)
          }
        },
        modifier = Modifier.testTag("confirm_start_stream_upload_button")
      ) {
        Text("Start Resumable Stream")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private data class UploadPreset(
  val name: String,
  val label: String,
  val type: String,
  val mimeType: String,
  val sizeBytes: Long,
  val sizeDesc: String
)

