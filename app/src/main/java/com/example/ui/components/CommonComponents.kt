package com.example.ui.components

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, icon) = when (status.uppercase()) {
    "PASS", "PASSED", "COMPLETED", "SYNCED" -> Triple(
      Color(0xFFE8F5E9),
      Color(0xFF2E7D32),
      Icons.Default.CheckCircle
    )
    "FAIL", "FAILED", "CRITICAL" -> Triple(
      Color(0xFFFFEBEE),
      Color(0xFFC62828),
      Icons.Default.Error
    )
    "FLAG", "FLAGGED", "NEEDS_REVIEW", "WARNING" -> Triple(
      Color(0xFFFFF3E0),
      Color(0xFFEF6C00),
      Icons.Default.Warning
    )
    "IN_PROGRESS", "SYNCING" -> Triple(
      Color(0xFFE3F2FD),
      Color(0xFF1565C0),
      Icons.Default.HourglassEmpty
    )
    else -> Triple(
      Color(0xFFF5F5F5),
      Color(0xFF616161),
      Icons.Default.HourglassEmpty
    )
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bgColor)
      .padding(horizontal = 7.dp, vertical = 3.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = textColor,
      modifier = Modifier.size(13.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = status.replace("_", " "),
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun SeverityBadge(
  severity: String,
  modifier: Modifier = Modifier
) {
  val (color, label) = when (severity.uppercase()) {
    "CRITICAL" -> Pair(Color(0xFFD32F2F), "CRITICAL")
    "HIGH" -> Pair(Color(0xFFF57C00), "HIGH")
    "MEDIUM" -> Pair(Color(0xFFFBC02D), "MEDIUM")
    "LOW" -> Pair(Color(0xFF388E3C), "LOW")
    else -> Pair(Color(0xFF757575), "NORMAL")
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(color.copy(alpha = 0.15f))
      .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(
      text = label,
      color = color,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun KpiMetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
      )
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  subtitle: String? = null,
  actionText: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
    if (actionText != null && onAction != null) {
      Text(
        text = actionText,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
          .clickable { onAction() }
          .padding(4.dp)
      )
    }
  }
}
