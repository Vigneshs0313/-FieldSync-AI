package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.InspectionEntity

import com.example.ui.components.KpiMetricCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.viewmodel.FieldSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: FieldSyncViewModel,
  onNavigateToInspection: (String) -> Unit,
  onNavigateToAllInspections: () -> Unit,
  onNavigateToConflicts: () -> Unit,
  onNavigateToSync: () -> Unit,
  onNavigateToAudit: () -> Unit,
  onNavigateToAI: () -> Unit,
  modifier: Modifier = Modifier
) {
  val inspections by viewModel.allInspections.collectAsState()
  val conflicts by viewModel.unresolvedConflicts.collectAsState()
  val pendingCount by viewModel.pendingQueueCount.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Brand Logo Header
    item {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("app_brand_header"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_app_logo),
            contentDescription = "FieldSync AI Brand Logo",
            modifier = Modifier
              .size(52.dp)
              .clip(RoundedCornerShape(12.dp))
              .testTag("app_logo_image"),
            contentScale = ContentScale.Crop
          )
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "FieldSync AI",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "ENTERPRISE",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Offline-First Field Inspection & CRDT Engine",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Top Hero / Workflow Pipeline Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "FieldSync AI Pipeline",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Offline → Edit → Sync → Conflict → Resolve → Audit → AI Analysis",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Zero silent overwrites. Technician edits are preserved locally in Room SQLite, resolved via CRDT merges, and verified with tamper-evident audit trails.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
            lineHeight = 15.sp
          )
        }
      }
    }

    // KPI Metrics Grid
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiMetricCard(
          title = "Active Inspections",
          value = "${inspections.size}",
          subtitle = "3 Plant Units",
          icon = Icons.Default.Assignment,
          accentColor = Color(0xFF0284C7),
          modifier = Modifier.weight(1f),
          onClick = onNavigateToAllInspections
        )
        KpiMetricCard(
          title = "Outbox Queue",
          value = "$pendingCount",
          subtitle = if (pendingCount > 0) "Pending sync" else "All synced",
          icon = Icons.Default.Sync,
          accentColor = if (pendingCount > 0) Color(0xFFF59E0B) else Color(0xFF10B981),
          modifier = Modifier.weight(1f),
          onClick = onNavigateToSync
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiMetricCard(
          title = "Conflict Center",
          value = "${conflicts.size}",
          subtitle = if (conflicts.isNotEmpty()) "Action required" else "0 Collisions",
          icon = Icons.Default.Warning,
          accentColor = if (conflicts.isNotEmpty()) Color(0xFFEF4444) else Color(0xFF10B981),
          modifier = Modifier.weight(1f),
          onClick = onNavigateToConflicts
        )
        KpiMetricCard(
          title = "InspectAI",
          value = "Ready",
          subtitle = "Diagnostic engine",
          icon = Icons.Default.AutoAwesome,
          accentColor = Color(0xFF8B5CF6),
          modifier = Modifier.weight(1f),
          onClick = onNavigateToAI
        )
      }
    }

    // Active Inspections Section
    item {
      SectionHeader(
        title = "Plant Inspections",
        subtitle = "Industrial telemetry & checklists",
        actionText = "View All",
        onAction = onNavigateToAllInspections
      )
    }

    items(inspections) { inspection ->
      InspectionCardItem(
        inspection = inspection,
        onClick = {
          viewModel.selectInspection(inspection.id)
          onNavigateToInspection(inspection.id)
        }
      )
    }

    // Recent Audit Stream
    item {
      Spacer(modifier = Modifier.height(4.dp))
      SectionHeader(
        title = "Immutable Audit Log Stream",
        subtitle = "Recent events & cryptographic stamps",
        actionText = "Full Trail",
        onAction = onNavigateToAudit
      )
    }

    items(auditLogs.take(3)) { log ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToAudit() },
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(10.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
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
                text = log.action,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = formatTime(log.timestamp),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "${log.actor} • ${log.details}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1
            )
          }
        }
      }
    }
  }
}

@Composable
fun InspectionCardItem(
  inspection: InspectionEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("inspection_card_${inspection.id}"),
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
        Text(
          text = inspection.assetTag,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        StatusBadge(status = inspection.status)
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = inspection.title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${inspection.assetName} • ${inspection.facility}",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (inspection.hasLocalChanges) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFFFF3E0))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "Unsynced Local Edits",
                fontSize = 10.sp,
                color = Color(0xFFE65100),
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
          }
          Text(
            text = "Tech: ${inspection.assignedTechnician}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = "Open",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

private fun formatTime(timestamp: Long): String {
  val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
