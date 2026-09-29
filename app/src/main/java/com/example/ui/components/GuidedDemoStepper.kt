package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

data class DemoStepInfo(
  val stepNumber: Int,
  val title: String,
  val description: String,
  val actionLabel: String
)

val DEMO_STEPS = listOf(
  DemoStepInfo(
    stepNumber = 1,
    title = "Dashboard & Active Assets",
    description = "Explore active industrial assets and open Turbine Gen-4B inspection details.",
    actionLabel = "Open Inspection"
  ),
  DemoStepInfo(
    stepNumber = 2,
    title = "Simulate Offline Mode",
    description = "Field technician enters high-interference subterranean vault with zero cellular signal.",
    actionLabel = "Disconnect (Offline)"
  ),
  DemoStepInfo(
    stepNumber = 3,
    title = "Perform Offline Inspection",
    description = "Log failures on Emergency Switch, flag anomalous Motor vibration, and record 88.5°C temperature.",
    actionLabel = "Log Offline Edits"
  ),
  DemoStepInfo(
    stepNumber = 4,
    title = "Verify Room Local DB",
    description = "Inspect local SQLite table state; edits are guaranteed to survive restarts and crashes.",
    actionLabel = "Verify Room DB"
  ),
  DemoStepInfo(
    stepNumber = 5,
    title = "Inspect Outbox Sync Queue",
    description = "Review pending mutation events queued in the local outbox waiting for connectivity.",
    actionLabel = "View Sync Center"
  ),
  DemoStepInfo(
    stepNumber = 6,
    title = "Simulate Remote Field Collision",
    description = "Tech Jordan remotely updates Oil Level and resets Motor telemetry in the cloud.",
    actionLabel = "Simulate Collision"
  ),
  DemoStepInfo(
    stepNumber = 7,
    title = "Restore Connectivity & Sync",
    description = "Technician emerges from vault. Network returns to ONLINE and sync triggers.",
    actionLabel = "Restore & Sync"
  ),
  DemoStepInfo(
    stepNumber = 8,
    title = "3-Way Conflict Center",
    description = "Compare Local (Alex) vs Remote (Jordan). Execute CRDT field-by-field merge.",
    actionLabel = "Open Conflict Center"
  ),
  DemoStepInfo(
    stepNumber = 9,
    title = "Immutable Audit Trail",
    description = "Inspect cryptographic SHA-256 audit log detailing all edits, collisions, and resolutions.",
    actionLabel = "View Audit Trail"
  ),
  DemoStepInfo(
    stepNumber = 10,
    title = "InspectAI Risk Assessment",
    description = "Run AI diagnostics to compute risk scores, root cause prediction, and generate Work Order.",
    actionLabel = "Run InspectAI"
  )
)

@Composable
fun GuidedDemoStepper(
  currentStep: Int,
  onExecuteStepAction: (Int) -> Unit,
  onResetDemo: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(true) }
  val currentStepInfo = DEMO_STEPS.getOrNull(currentStep - 1) ?: DEMO_STEPS.first()

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp)
      .testTag("guided_demo_card"),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF0F172A), // Dark slate
      contentColor = Color.White
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Guided Demo",
            tint = Color(0xFF38BDF8),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "10-Step Interactive Demo",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color(0xFFE2E8F0)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF0284C7))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Step $currentStep/10",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Row {
          IconButton(
            onClick = onResetDemo,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.RestartAlt,
              contentDescription = "Reset Demo",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = "Toggle Collapse",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      LinearProgressIndicator(
        progress = { (currentStep / 10f).coerceIn(0.1f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = Color(0xFF38BDF8),
        trackColor = Color(0xFF334155)
      )

      AnimatedVisibility(visible = isExpanded) {
        Column {
          Text(
            text = "${currentStepInfo.stepNumber}. ${currentStepInfo.title}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFFF8FAFC)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = currentStepInfo.description,
            fontSize = 12.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 16.sp
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = { onExecuteStepAction(currentStep) },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("execute_demo_step_button")
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = currentStepInfo.actionLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
