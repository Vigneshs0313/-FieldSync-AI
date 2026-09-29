package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.FieldSyncViewModel

@Composable
fun InspectionsListScreen(
  viewModel: FieldSyncViewModel,
  onNavigateToInspection: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val inspections by viewModel.allInspections.collectAsState()
  var selectedFilter by remember { mutableStateOf("ALL") }

  val filteredInspections = when (selectedFilter) {
    "IN_PROGRESS" -> inspections.filter { it.status == "IN_PROGRESS" }
    "NEEDS_REVIEW" -> inspections.filter { it.status == "NEEDS_REVIEW" }
    "COMPLETED" -> inspections.filter { it.status == "COMPLETED" }
    "UNSYNCED" -> inspections.filter { it.hasLocalChanges }
    else -> inspections
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "All Plant Inspections",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = "Select an asset to view checklist items and record field observations.",
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      val filters = listOf(
        Pair("ALL", "All (${inspections.size})"),
        Pair("IN_PROGRESS", "In Progress"),
        Pair("NEEDS_REVIEW", "Needs Review"),
        Pair("COMPLETED", "Completed"),
        Pair("UNSYNCED", "Unsynced")
      )
      items(filters) { (key, label) ->
        FilterChip(
          selected = selectedFilter == key,
          onClick = { selectedFilter = key },
          label = { Text(label, fontSize = 12.sp) },
          modifier = Modifier.testTag("filter_chip_$key")
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(bottom = 80.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      items(filteredInspections) { inspection ->
        InspectionCardItem(
          inspection = inspection,
          onClick = {
            viewModel.selectInspection(inspection.id)
            onNavigateToInspection(inspection.id)
          }
        )
      }
    }
  }
}
