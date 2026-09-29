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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.ai.AIAnalysisResult
import com.example.ui.components.SectionHeader
import com.example.viewmodel.FieldSyncViewModel

@Composable
fun InspectAIScreen(
  viewModel: FieldSyncViewModel,
  modifier: Modifier = Modifier
) {
  val inspection by viewModel.selectedInspection.collectAsState()
  val analysisResult by viewModel.aiAnalysisResult.collectAsState()
  val isAnalyzing by viewModel.isAnalyzing.collectAsState()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1065)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFC084FC),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "InspectAI Diagnostic Suite",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF581C87))
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "Offline & Cloud AI",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE9D5FF)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Automated risk assessment, failure mode & effects analysis (FMEA), and preventative work order generation computed from field checklist telemetry.",
            fontSize = 12.sp,
            color = Color(0xFFD8B4FE),
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = { viewModel.runInspectAI() },
            enabled = !isAnalyzing,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF9333EA),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("reanalyze_ai_button")
          ) {
            if (isAnalyzing) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Synthesizing Telemetry...", fontSize = 12.sp)
            } else {
              Icon(imageVector = Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (analysisResult == null) "Run InspectAI Diagnostics" else "Re-run Diagnostic Assessment",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    if (analysisResult == null && !isAnalyzing) {
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
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Ready to Analyze ${inspection?.assetName ?: "Asset"}",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap 'Run InspectAI Diagnostics' to compute risk index, predict mechanical root cause, and formulate preventative work order.",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 16.sp
            )
          }
        }
      }
    } else if (analysisResult != null) {
      val result = analysisResult!!

      // Risk Gauge & Hazard Level Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(14.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Calculated Risk Score",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "${result.riskScore}/100",
                  fontSize = 28.sp,
                  fontWeight = FontWeight.Black,
                  color = Color(result.hazardColor)
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(result.hazardColor).copy(alpha = 0.15f))
                  .border(1.dp, Color(result.hazardColor), RoundedCornerShape(8.dp))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = result.hazardLevel,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color(result.hazardColor)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { result.riskScore / 100f },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = Color(result.hazardColor),
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = result.executiveSummary,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 17.sp
            )
          }
        }
      }

      // Root Cause Prediction
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Root-Cause Engineering Prediction",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = result.rootCausePrediction,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              lineHeight = 16.sp
            )
          }
        }
      }

      // Recommended Actions
      item {
        SectionHeader(
          title = "Recommended Immediate Actions (${result.recommendedActions.size})",
          subtitle = "Step-by-step mitigation procedures"
        )
      }

      items(result.recommendedActions) { action ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = Color(0xFF16A34A),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = action,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 16.sp
            )
          }
        }
      }

      // Generated Work Order Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.AssignmentTurnedIn,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Formulated Work Order",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color.White
                )
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (result.workOrderPriority.contains("EMERGENCY")) Color(0xFFDC2626) else Color(0xFF0284C7))
                  .padding(horizontal = 8.dp, vertical = 3.dp)
              ) {
                Text(
                  text = result.workOrderPriority,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = result.workOrderTitle,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFF1F5F9)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Estimated Downtime: ${result.estimatedDowntimeHours} hrs • Dispatched to Plant Maintenance",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Required Replacement Parts & Fluids:",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFCBD5E1)
            )
            result.requiredParts.forEach { part ->
              Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Build,
                  contentDescription = null,
                  tint = Color(0xFF38BDF8),
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = part, fontSize = 11.sp, color = Color(0xFFE2E8F0))
              }
            }
          }
        }
      }
    }
  }
}
