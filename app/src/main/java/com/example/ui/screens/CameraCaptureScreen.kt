package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.viewmodel.FieldSyncViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraCaptureScreen(
  viewModel: FieldSyncViewModel,
  inspectionId: String,
  checklistItemId: String? = null,
  onBack: () -> Unit,
  onPhotoSaved: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var hasCameraPermission by remember { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasCameraPermission = isGranted
  }

  LaunchedEffect(Unit) {
    val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
    hasCameraPermission = permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED
    if (!hasCameraPermission) {
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  if (!hasCameraPermission) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(Color.Black),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Camera,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Camera Permission Required",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "FieldSync AI needs camera access to capture inspection photos and attach them to checklist items while offline.",
          color = Color.LightGray,
          fontSize = 14.sp,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
          onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Text("Grant Camera Permission")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onBack) {
          Text("Cancel", color = Color.White)
        }
      }
    }
    return
  }

  var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
  var caption by remember { mutableStateOf("") }
  var selectedPhotoType by remember { mutableStateOf("GENERAL") }
  val photoTypes = listOf("GENERAL", "MOTOR", "THERMAL", "SWITCH", "PANEL", "OIL")

  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
  val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    AndroidView(
      factory = { ctx ->
        val previewView = PreviewView(ctx).apply {
          scaleType = PreviewView.ScaleType.FILL_CENTER
        }
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
          it.setSurfaceProvider(previewView.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder()
          .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
          .build()

        val cameraSelector = CameraSelector.Builder()
          .requireLensFacing(lensFacing)
          .build()

        try {
          cameraProvider.unbindAll()
          cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
          )
        } catch (exc: Exception) {
          exc.printStackTrace()
        }
        previewView
      },
      modifier = Modifier.fillMaxSize()
    )

    // Top Controls Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .align(Alignment.TopCenter),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .background(Color.Black.copy(alpha = 0.5f), CircleShape)
          .testTag("camera_back_button")
      ) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = "Back",
          tint = Color.White
        )
      }

      Text(
        text = if (checklistItemId != null) "Attach to Checklist" else "Inspection Photo",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
      )

      IconButton(
        onClick = {
          lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
          } else {
            CameraSelector.LENS_FACING_BACK
          }
        },
        modifier = Modifier
          .background(Color.Black.copy(alpha = 0.5f), CircleShape)
          .testTag("camera_switch_button")
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Switch Camera",
          tint = Color.White
        )
      }
    }

    // Bottom Controls Sheet
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.BottomCenter)
        .background(Color.Black.copy(alpha = 0.75f))
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(photoTypes) { type ->
          val isSelected = selectedPhotoType == type
          FilterChip(
            selected = isSelected,
            onClick = { selectedPhotoType = type },
            label = { Text(type, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = Color.White,
              containerColor = Color.DarkGray,
              labelColor = Color.LightGray
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = caption,
        onValueChange = { caption = it },
        placeholder = { Text("Enter photo caption or observation notes...") },
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = Color.Gray,
          focusedTextColor = Color.White,
          unfocusedTextColor = Color.White,
          focusedPlaceholderColor = Color.LightGray,
          unfocusedPlaceholderColor = Color.LightGray
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("photo_caption_input")
      )

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Button(
          onClick = {
            val capture = imageCapture ?: return@Button
            val photoFile = File(
              context.filesDir,
              "inspection_${inspectionId}_${System.currentTimeMillis()}.jpg"
            )
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            capture.takePicture(
              outputOptions,
              ContextCompat.getMainExecutor(context),
              object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                  Toast.makeText(context, "Photo capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                  val finalCaption = if (caption.isNotBlank()) caption else "$selectedPhotoType Inspection Photo"
                  viewModel.addInspectionPhotoCustom(
                    inspectionId = inspectionId,
                    checklistItemId = checklistItemId,
                    caption = finalCaption,
                    photoType = selectedPhotoType,
                    filePath = photoFile.absolutePath
                  )
                  Toast.makeText(context, "Photo captured & attached successfully (Offline Ready)", Toast.LENGTH_SHORT).show()
                  onPhotoSaved()
                }
              }
            )
          },
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(containerColor = Color.White),
          modifier = Modifier
            .size(72.dp)
            .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .testTag("shutter_button")
        ) {
          Box(
            modifier = Modifier
              .size(56.dp)
              .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Check,
              contentDescription = "Capture",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
        }
      }
    }
  }
}
