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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ConflictEntity
import com.example.viewmodel.FieldSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConflictCenterScreen(
  viewModel: FieldSyncViewModel,
  onNavigateToAudit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val conflicts by viewModel.allConflicts.collectAsState()
  val unresolvedConflicts = conflicts.filter { !it.isResolved }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Hero Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CompareArrows,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "3-Way Conflict Resolver",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (unresolvedConflicts.isNotEmpty()) Color(0xFFDC2626) else Color(0xFF16A34A))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "${unresolvedConflicts.size} Unresolved",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "CRDT Zero-Data-Loss Invariant: When local offline edits collide with cloud replica updates, FieldSync halts automated overwrites and provides 3-way reconciliation.",
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            lineHeight = 16.sp
          )

          if (unresolvedConflicts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = { viewModel.resolveAllWithMerge() },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("batch_resolve_merge_button")
            ) {
              Icon(
                imageVector = Icons.Default.CallMerge,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Batch Resolve All (${unresolvedConflicts.size}) with CRDT Merge",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    if (conflicts.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.CloudDone,
              contentDescription = null,
              tint = Color(0xFF16A34A),
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Zero Active Conflicts",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "All local Room SQLite replicas and cloud states are fully harmonized.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = { viewModel.simulateRemoteConflict() },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("simulate_collision_button")
            ) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Simulate Cloud Field Collision", fontSize = 12.sp)
            }
          }
        }
      }
    } else {
      items(conflicts) { conflict ->
        ThreeWayConflictCard(
          conflict = conflict,
          onResolve = { strategy ->
            viewModel.resolveConflict(conflict, strategy)
          }
        )
      }
    }
  }
}

@Composable
fun ThreeWayConflictCard(
  conflict: ConflictEntity,
  onResolve: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("conflict_card_${conflict.id}"),
    colors = CardDefaults.cardColors(
      containerColor = if (conflict.isResolved) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Collision on: ${conflict.fieldName}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Asset Target: ${conflict.inspectionId}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (conflict.isResolved) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFFE8F5E9))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "RESOLVED [${conflict.resolvedStrategy}]",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF2E7D32)
            )
          }
        } else {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFFFFEBEE))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "ACTION REQUIRED",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFC62828)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3-Way Diff Comparison Visualizer
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        // 1. Local Offline Replica (Alex)
        DiffBranchBox(
          tag = "LOCAL REPLICA (Offline)",
          author = conflict.localAuthor,
          time = formatTime(conflict.localTimestamp),
          value = conflict.localValue,
          borderColor = Color(0xFF0284C7),
          tagBg = Color(0xFFE0F2FE),
          tagText = Color(0xFF0369A1)
        )

        // 2. Remote Cloud Replica (Jordan)
        DiffBranchBox(
          tag = "REMOTE REPLICA (Cloud Sync)",
          author = conflict.remoteAuthor,
          time = formatTime(conflict.remoteTimestamp),
          value = conflict.remoteValue,
          borderColor = Color(0xFFF59E0B),
          tagBg = Color(0xFFFEF3C7),
          tagText = Color(0xFFB45309)
        )

        // 3. Merged Preview (CRDT Smart Result)
        DiffBranchBox(
          tag = "CRDT SMART MERGED CANDIDATE",
          author = "FieldSync CRDT Engine",
          time = "Realtime preview",
          value = "${conflict.remoteValue} [Verified locally: ${conflict.localValue}]",
          borderColor = Color(0xFF10B981),
          tagBg = Color(0xFFD1FAE5),
          tagText = Color(0xFF047857)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (!conflict.isResolved) {
        // 3 Options: Keep Local, Keep Remote, CRDT Merge
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          OutlinedButton(
            onClick = { onResolve("LOCAL") },
            modifier = Modifier.weight(1f).testTag("keep_local_button_${conflict.id}"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
          ) {
            Text("Keep Local", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          OutlinedButton(
            onClick = { onResolve("REMOTE") },
            modifier = Modifier.weight(1f).testTag("keep_remote_button_${conflict.id}"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
          ) {
            Text("Keep Remote", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }

          Button(
            onClick = { onResolve("FIELD_MERGE") },
            modifier = Modifier.weight(1.2f).testTag("merge_button_${conflict.id}"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
          ) {
            Icon(imageVector = Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("CRDT Merge", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
        ) {
          Text(
            text = "Effective Value: ${conflict.resolvedValue}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun DiffBranchBox(
  tag: String,
  author: String,
  time: String,
  value: String,
  borderColor: Color,
  tagBg: Color,
  tagText: Color
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
      .background(borderColor.copy(alpha = 0.05f))
      .padding(10.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(tagBg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = tag,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = tagText
          )
        }
        Text(
          text = time,
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = author,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

private fun formatTime(timestamp: Long): String {
  val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
