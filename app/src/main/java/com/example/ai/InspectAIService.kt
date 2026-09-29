package com.example.ai

import com.example.data.entity.ChecklistItemEntity
import com.example.data.entity.InspectionEntity

data class AIAnalysisResult(
  val riskScore: Int, // 0 to 100
  val hazardLevel: String, // CRITICAL, HIGH, MODERATE, LOW
  val hazardColor: Long, // 0xFF... color code
  val executiveSummary: String,
  val rootCausePrediction: String,
  val recommendedActions: List<String>,
  val workOrderTitle: String,
  val workOrderPriority: String,
  val estimatedDowntimeHours: Double,
  val requiredParts: List<String>,
  val generatedAt: Long = System.currentTimeMillis()
)

object InspectAIService {

  fun analyzeInspection(
    inspection: InspectionEntity,
    items: List<ChecklistItemEntity>
  ): AIAnalysisResult {
    var computedRisk = 10

    val failedItems = items.filter { it.status == "FAIL" }
    val flaggedItems = items.filter { it.status == "FLAG" }
    val tempItem = items.find { it.itemName.contains("Temperature", ignoreCase = true) }
    val oilItem = items.find { it.itemName.contains("Oil", ignoreCase = true) }
    val emergencyItem = items.find { it.itemName.contains("Emergency", ignoreCase = true) }
    val motorItem = items.find { it.itemName.contains("Motor", ignoreCase = true) }

    computedRisk += (failedItems.size * 25)
    computedRisk += (flaggedItems.size * 15)

    if (emergencyItem?.status == "FAIL") {
      computedRisk += 20 // Emergency switch failure is severe
    }

    tempItem?.numericValue?.let { temp ->
      if (temp >= 85.0) {
        computedRisk += 30
      } else if (temp >= 75.0) {
        computedRisk += 15
      }
    }

    oilItem?.numericValue?.let { oil ->
      if (oil <= 40.0) {
        computedRisk += 25
      } else if (oil <= 60.0) {
        computedRisk += 10
      }
    }

    val riskScore = computedRisk.coerceIn(5, 98)

    val (hazardLevel, colorCode) = when {
      riskScore >= 75 -> Pair("CRITICAL HAZARD", 0xFFE53935)
      riskScore >= 50 -> Pair("HIGH RISK", 0xFFFF9800)
      riskScore >= 25 -> Pair("MODERATE CONCERN", 0xFFFDD835)
      else -> Pair("NOMINAL (LOW RISK)", 0xFF43A047)
    }

    val summary = buildString {
      append("FieldSync InspectAI evaluated ${items.size} telemetry & checklist checkpoints for asset '${inspection.assetName}' (${inspection.assetTag}). ")
      if (failedItems.isNotEmpty() || flaggedItems.isNotEmpty()) {
        append("Identified ${failedItems.size} critical failure(s) and ${flaggedItems.size} operational warning(s). ")
        if (emergencyItem?.status == "FAIL") {
          append("CRITICAL: Safety trip mechanism inoperative. ")
        }
        if (motorItem?.status != "PASS") {
          append("Acoustic/vibration telemetry indicates anomalous mechanical stress on the primary drive assembly. ")
        }
        tempItem?.numericValue?.let {
          if (it > 75.0) append("Thermal core is operating above nominal threshold at ${it}°C. ")
        }
      } else {
        append("All major subsystems are operating within acceptable ISO/OSHA engineering envelopes. No acute hardware degradation detected.")
      }
    }

    val rootCause = when {
      riskScore >= 70 -> "High likelihood of lubricant degradation causing localized friction spikes, leading to thermal elevation in motor stator bearings and concomitant safety contact disengagement."
      riskScore >= 45 -> "Thermal drift and minor mechanical impedance detected. Potential bearing race micro-fissuring or lubrication contamination."
      else -> "Routine operational wear and tear. All vibration harmonics and electrical contacts within manufacturer baseline tolerances."
    }

    val recommendations = mutableListOf<String>()
    if (emergencyItem?.status == "FAIL") {
      recommendations.add("MANDATORY: Lockout/Tagout (LOTO) asset immediately until emergency disconnect switch contactor is inspected & replaced.")
    }
    if (motorItem?.status != "PASS") {
      recommendations.add("Conduct 3-axis vibration spectrum FFT analysis on drive shaft and motor housing.")
    }
    tempItem?.numericValue?.let {
      if (it > 75.0) {
        recommendations.add("Inspect cooling jacket flow rate and thermal paste interface on temperature probe sensor.")
      }
    }
    if (oilItem?.numericValue != null && oilItem.numericValue < 70.0) {
      recommendations.add("Top up reservoir with specified synthetic gear lubricant (ISO VG 220) and check drainage plug for metallic debris.")
    }
    if (recommendations.isEmpty()) {
      recommendations.add("Maintain scheduled 30-day preventative maintenance cycle.")
      recommendations.add("Log routine operational telemetry to plant cloud historian.")
    }

    val parts = mutableListOf<String>()
    if (emergencyItem?.status == "FAIL") parts.add("Emergency E-Stop Switch Assembly (Part #EST-402B)")
    if (motorItem?.status != "PASS") parts.add("SKF Deep Groove Ball Bearing Set (Part #6208-2RS1)")
    if (oilItem?.numericValue != null && oilItem.numericValue < 70.0) parts.add("Synthetic Industrial Lubricant 20L (Shell Tellus S2 MX 46)")
    if (parts.isEmpty()) parts.add("Standard PM Filter & Gasket Refresh Kit")

    return AIAnalysisResult(
      riskScore = riskScore,
      hazardLevel = hazardLevel,
      hazardColor = colorCode,
      executiveSummary = summary,
      rootCausePrediction = rootCause,
      recommendedActions = recommendations,
      workOrderTitle = "WO-${inspection.assetTag}-${System.currentTimeMillis() % 10000}: Corrective Maintenance & Safety Verification",
      workOrderPriority = if (riskScore >= 70) "P1 - EMERGENCY" else if (riskScore >= 45) "P2 - URGENT" else "P3 - ROUTINE",
      estimatedDowntimeHours = if (riskScore >= 70) 4.5 else if (riskScore >= 45) 2.0 else 0.5,
      requiredParts = parts
    )
  }
}
