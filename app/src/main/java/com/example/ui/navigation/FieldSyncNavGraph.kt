package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.FieldSyncTopNavHeader
import com.example.ui.components.GuidedDemoStepper
import com.example.ui.components.PersistentConnectivityStatusBar
import com.example.ui.screens.AuditHistoryScreen
import com.example.ui.screens.CameraCaptureScreen
import com.example.ui.screens.ConflictCenterScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InspectAIScreen
import com.example.ui.screens.InspectionDetailScreen
import com.example.ui.screens.InspectionsListScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SyncCenterScreen
import com.example.viewmodel.FieldSyncViewModel

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
  object Dashboard : NavItem("dashboard", "Dashboard", Icons.Default.Dashboard)
  object Inspections : NavItem("inspections", "Inspect", Icons.Default.Assignment)
  object Conflicts : NavItem("conflict_center", "Conflicts", Icons.Default.CompareArrows)
  object Sync : NavItem("sync_center", "Sync", Icons.Default.Sync)
  object InspectAI : NavItem("inspect_ai", "InspectAI", Icons.Default.AutoAwesome)
  object Audit : NavItem("audit_history", "Audit", Icons.Default.History)
  object Settings : NavItem("settings", "Settings", Icons.Default.Settings)
}

val NAV_ITEMS = listOf(
  NavItem.Dashboard,
  NavItem.Inspections,
  NavItem.Conflicts,
  NavItem.Sync,
  NavItem.InspectAI,
  NavItem.Audit,
  NavItem.Settings
)

@Composable
fun FieldSyncApp(
  viewModel: FieldSyncViewModel,
  startDestination: String = "splash"
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
  val isSplashScreen = currentRoute == "splash"

  val isOnline by viewModel.isOnline.collectAsState()
  val networkMode by viewModel.networkMode.collectAsState()
  val isSyncing by viewModel.isSyncing.collectAsState()
  val pendingCount by viewModel.pendingQueueCount.collectAsState()
  val conflictCount by viewModel.unresolvedConflictCount.collectAsState()
  val syncMessage by viewModel.syncMessage.collectAsState()
  val demoStep by viewModel.guidedDemoStep.collectAsState()

  Scaffold(
    topBar = {
      if (!isSplashScreen) {
        Column {
          FieldSyncTopNavHeader(
            onLogoClick = {
              navController.navigate(NavItem.Dashboard.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            }
          )

          PersistentConnectivityStatusBar(
            isOnline = isOnline,
            networkMode = networkMode,
            isSyncing = isSyncing,
            pendingCount = pendingCount,
            conflictCount = conflictCount,
            syncMessage = syncMessage,
            onToggleMode = { viewModel.setNetworkMode(it) },
            onTriggerSync = { viewModel.triggerSync() },
            onNavigateToConflicts = {
              navController.navigate(NavItem.Conflicts.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            },
            onNavigateToSync = {
              navController.navigate(NavItem.Sync.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
              }
            }
          )

          GuidedDemoStepper(
            currentStep = demoStep,
            onExecuteStepAction = { step ->
              viewModel.executeDemoStepAction(step) { targetRoute ->
                navController.navigate(targetRoute)
              }
            },
            onResetDemo = {
              viewModel.resetAllDemoData { targetRoute ->
                navController.navigate(targetRoute)
              }
            }
          )
        }
      }
    },
    bottomBar = {
      if (!isSplashScreen) {
        NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
          NAV_ITEMS.forEach { item ->
            val selected = currentRoute == item.route ||
              (item == NavItem.Inspections && currentRoute?.startsWith("inspection_detail") == true)

            NavigationBarItem(
              icon = {
                if (item == NavItem.Conflicts && conflictCount > 0) {
                  BadgedBox(badge = { Badge { Text("$conflictCount") } }) {
                    Icon(item.icon, contentDescription = item.title)
                  }
                } else if (item == NavItem.Sync && pendingCount > 0) {
                  BadgedBox(badge = { Badge { Text("$pendingCount") } }) {
                    Icon(item.icon, contentDescription = item.title)
                  }
                } else {
                  Icon(item.icon, contentDescription = item.title)
                }
              },
              label = { Text(item.title) },
              selected = selected,
              onClick = {
                navController.navigate(item.route) {
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                  }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              modifier = Modifier.testTag("nav_tab_${item.route}")
            )
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(if (isSplashScreen) PaddingValues(0.dp) else innerPadding)
    ) {
      NavHost(
        navController = navController,
        startDestination = startDestination
      ) {
        composable("splash") {
          SplashScreen(
            onNavigateToDashboard = {
              navController.navigate(NavItem.Dashboard.route) {
                popUpTo("splash") { inclusive = true }
                launchSingleTop = true
              }
            }
          )
        }

        composable(NavItem.Dashboard.route) {
          DashboardScreen(
            viewModel = viewModel,
            onNavigateToInspection = { id ->
              navController.navigate("inspection_detail/$id")
            },
            onNavigateToAllInspections = {
              navController.navigate(NavItem.Inspections.route)
            },
            onNavigateToConflicts = {
              navController.navigate(NavItem.Conflicts.route)
            },
            onNavigateToSync = {
              navController.navigate(NavItem.Sync.route)
            },
            onNavigateToAudit = {
              navController.navigate(NavItem.Audit.route)
            },
            onNavigateToAI = {
              navController.navigate(NavItem.InspectAI.route)
            }
          )
        }

        composable(NavItem.Inspections.route) {
          InspectionsListScreen(
            viewModel = viewModel,
            onNavigateToInspection = { id ->
              navController.navigate("inspection_detail/$id")
            }
          )
        }

        composable(
          route = "inspection_detail/{id}",
          arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
          val id = backStackEntry.arguments?.getString("id") ?: "insp-001"
          InspectionDetailScreen(
            viewModel = viewModel,
            inspectionId = id,
            onBack = { navController.popBackStack() },
            onNavigateToAI = {
              navController.navigate(NavItem.InspectAI.route)
            },
            onNavigateToCamera = { inspId, itemId ->
              val route = if (itemId != null) "camera_capture/$inspId?checklistItemId=$itemId" else "camera_capture/$inspId"
              navController.navigate(route)
            }
          )
        }

        composable(
          route = "camera_capture/{inspectionId}?checklistItemId={checklistItemId}",
          arguments = listOf(
            navArgument("inspectionId") { type = NavType.StringType },
            navArgument("checklistItemId") { type = NavType.StringType; nullable = true; defaultValue = null }
          )
        ) { backStackEntry ->
          val inspId = backStackEntry.arguments?.getString("inspectionId") ?: "insp-001"
          val itemId = backStackEntry.arguments?.getString("checklistItemId")
          CameraCaptureScreen(
            viewModel = viewModel,
            inspectionId = inspId,
            checklistItemId = itemId,
            onBack = { navController.popBackStack() },
            onPhotoSaved = { navController.popBackStack() }
          )
        }

        composable(NavItem.Conflicts.route) {
          ConflictCenterScreen(
            viewModel = viewModel,
            onNavigateToAudit = {
              navController.navigate(NavItem.Audit.route)
            }
          )
        }

        composable(NavItem.Sync.route) {
          SyncCenterScreen(
            viewModel = viewModel,
            onNavigateToConflicts = {
              navController.navigate(NavItem.Conflicts.route)
            }
          )
        }

        composable(NavItem.InspectAI.route) {
          InspectAIScreen(
            viewModel = viewModel
          )
        }

        composable(NavItem.Audit.route) {
          AuditHistoryScreen(
            viewModel = viewModel
          )
        }

        composable(NavItem.Settings.route) {
          SettingsScreen(
            viewModel = viewModel,
            onNavigateToDashboard = {
              navController.navigate(NavItem.Dashboard.route)
            },
            onNavigateToSplash = {
              navController.navigate("splash")
            }
          )
        }
      }
    }
  }
}

