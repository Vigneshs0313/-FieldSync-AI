package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Modern minimalist vector logo component for FieldSync AI.
 * Supports compact rendering for navigation bars and large animated rendering for splash screens.
 */
@Composable
fun FieldSyncLogo(
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
  animated: Boolean = false,
  showGlow: Boolean = false
) {
  val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
  
  val glowAlpha by if (showGlow) {
    infiniteTransition.animateFloat(
      initialValue = 0.25f,
      targetValue = 0.7f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1600, easing = LinearEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "glowAlpha"
    )
  } else {
    androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
  }

  val rotationAngle by if (animated) {
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 8000, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "logoRotation"
    )
  } else {
    androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
  }

  Box(
    modifier = Modifier
      .testTag("fieldsync_vector_logo_container")
      .then(modifier)
      .size(size),
    contentAlignment = Alignment.Center
  ) {
    // Optional glowing ambient background for splash screen hero
    if (showGlow) {
      Box(
        modifier = Modifier
          .size(size * 1.25f)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(
                Color(0xFF10B981).copy(alpha = glowAlpha),
                Color(0xFF06B6D4).copy(alpha = glowAlpha * 0.5f),
                Color.Transparent
              )
            )
          )
      )
    }

    // Outer vector container with rounded geometry
    Surface(
      shape = RoundedCornerShape(size * 0.26f),
      color = Color(0xFF0F172A),
      shadowElevation = 4.dp,
      modifier = Modifier
        .size(size)
        .border(
          width = (size * 0.035f).coerceAtLeast(1.dp),
          brush = Brush.linearGradient(
            colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4))
          ),
          shape = RoundedCornerShape(size * 0.26f)
        )
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(size * 0.12f)
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_fieldsync_logo),
          contentDescription = "FieldSync AI Vector Logo",
          modifier = Modifier
            .size(size * 0.76f)
            .testTag("fieldsync_vector_logo_image"),
          contentScale = ContentScale.Fit
        )
      }
    }
  }
}

/**
 * Top Navigation Header branding with the FieldSync AI vector logo.
 */
@Composable
fun FieldSyncTopNavHeader(
  modifier: Modifier = Modifier,
  onLogoClick: (() -> Unit)? = null
) {
  Surface(
    modifier = modifier
      .testTag("fieldsync_top_nav_header"),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FieldSyncLogo(
        size = 32.dp,
        animated = false,
        showGlow = false
      )
      
      Spacer(modifier = Modifier.width(10.dp))
      
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "FieldSync",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.testTag("top_nav_app_name")
          )
          Spacer(modifier = Modifier.width(4.dp))
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF06B6D4).copy(alpha = 0.18f)
          ) {
            Text(
              text = "AI",
              fontSize = 11.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF0891B2),
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
            )
          }
        }
        Text(
          text = "Offline-First Enterprise Engine",
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
