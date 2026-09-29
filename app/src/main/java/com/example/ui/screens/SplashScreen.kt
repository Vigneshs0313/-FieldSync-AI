package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FieldSyncLogo
import kotlinx.coroutines.delay

/**
 * Modern minimalist Splash Screen featuring the FieldSync AI vector logo.
 * Displays initializing services and auto-navigates into the application.
 */
@Composable
fun SplashScreen(
  onNavigateToDashboard: () -> Unit,
  modifier: Modifier = Modifier,
  autoNavigateDelayMs: Long = 1800L
) {
  var isReady by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(autoNavigateDelayMs)
    isReady = true
    onNavigateToDashboard()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFF090D16),
            Color(0xFF0F172A),
            Color(0xFF0B1329)
          )
        )
      )
      .clickable { onNavigateToDashboard() }
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 28.dp)
    ) {
      // Prominent Vector Logo with glowing aura
      Box(
        modifier = Modifier.testTag("splash_vector_logo"),
        contentAlignment = Alignment.Center
      ) {
        FieldSyncLogo(
          size = 110.dp,
          animated = false,
          showGlow = true
        )
      }

      Spacer(modifier = Modifier.height(28.dp))


          // Brand Title
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = "FieldSync",
              fontWeight = FontWeight.Black,
              fontSize = 32.sp,
              color = Color.White,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFF06B6D4).copy(alpha = 0.22f),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFF06B6D4).copy(alpha = 0.6f)
              )
            ) {
              Text(
                text = "AI",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF38BDF8),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Subtitle / Enterprise Purpose
          Text(
            text = "Offline-First Enterprise Field Inspection",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
          )

          Text(
            text = "CRDT Conflict Resolution • Immutable Audit Trails",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
          )

          Spacer(modifier = Modifier.height(36.dp))

          // Architecture status chips
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
          ) {
            SplashFeaturePill(
              icon = Icons.Default.Storage,
              text = "Room SQLite Local Outbox",
              status = "ACTIVE"
            )
            SplashFeaturePill(
              icon = Icons.Default.CloudDone,
              text = "Background Auto-Sync Engine",
              status = "ONLINE"
            )
            SplashFeaturePill(
              icon = Icons.Default.CheckCircle,
              text = "LWW-Element CRDT Merging",
              status = "READY"
            )
          }

          Spacer(modifier = Modifier.height(36.dp))

          // Launch Button & Progress
          Button(
            onClick = { onNavigateToDashboard() },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF10B981)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth(0.7f)
              .testTag("splash_start_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "Launch Workspace",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }
    }
  }
}


@Composable
private fun SplashFeaturePill(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String,
  status: String
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = Color.White.copy(alpha = 0.06f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = Color(0xFF38BDF8),
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = text,
          fontSize = 12.sp,
          color = Color(0xFFE2E8F0),
          fontWeight = FontWeight.Normal
        )
      }
      Text(
        text = status,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF34D399)
      )
    }
  }
}
