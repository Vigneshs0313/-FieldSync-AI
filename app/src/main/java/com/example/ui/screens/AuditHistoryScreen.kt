package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.AuditLogEntity
import com.example.viewmodel.FieldSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditHistoryScreen(
  viewModel: FieldSyncViewModel,
  modifier: Modifier = Modifier
) {
  val auditLogs by viewModel.auditLogs.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = Color(0xFF38BDF8),
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Cryptographic Audit Ledger",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = Color.White
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Immutable, append-only record of every local edit, network toggle, remote conflict, and CRDT resolution. Compliant with OSHA & ISO plant governance standards.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            lineHeight = 16.sp
          )
        }
      }
    }

    if (auditLogs.isEmpty()) {
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
              text = "No audit entries recorded yet.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(auditLogs) { log ->
        AuditLogEntryCard(log = log)
      }
    }
  }
}

@Composable
fun AuditLogEntryCard(log: AuditLogEntity) {
  val (actionBg, actionText) = when {
    log.action.contains("CONFLICT", ignoreCase = true) -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
    log.action.contains("OFFLINE", ignoreCase = true) -> Pair(Color(0xFFE0F2FE), Color(0xFF0284C7))
    log.action.contains("RESOLVED", ignoreCase = true) -> Pair(Color(0xFFDCFCE7), Color(0xFF16A34A))
    log.action.contains("AI", ignoreCase = true) -> Pair(Color(0xFFF3E8FF), Color(0xFF9333EA))
    else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(10.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(actionBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = log.action,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = actionText
          )
        }

        Text(
          text = formatFullTime(log.timestamp),
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = log.details,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 16.sp
      )

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Actor: ${log.actor} • Target: ${log.targetEntity}",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      // Before & After Diff if present
      if (log.beforeState != null || log.afterState != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp)
        ) {
          if (log.beforeState != null) {
            Text(
              text = "- Before: ${log.beforeState}",
              fontSize = 10.sp,
              color = Color(0xFFB91C1C),
              fontFamily = FontFamily.Monospace
            )
          }
          if (log.afterState != null) {
            Text(
              text = "+ After:  ${log.afterState}",
              fontSize = 10.sp,
              color = Color(0xFF15803D),
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // SHA-256 Stamp
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Fingerprint,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "SHA-256: #${log.hashSignature.take(12)}",
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
      }
    }
  }
}

private fun formatFullTime(timestamp: Long): String {
  val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
