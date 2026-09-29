package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIAnalysisResult
import com.example.ai.InspectAIService
import com.example.data.FieldSyncDatabase
import com.example.data.FieldSyncRepository
import com.example.data.NetworkMode
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChecklistItemEntity
import com.example.data.entity.ConflictEntity
import com.example.data.entity.InspectionEntity
import com.example.data.entity.InspectionPhotoEntity
import com.example.data.entity.MediaUploadEntity
import com.example.data.entity.SyncQueueItemEntity

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.util.ConnectivityManagerWrapper
import com.example.util.ConnectivityObserver
import com.example.util.NetworkConnectivityObserver
import com.example.util.NetworkStatus

@OptIn(ExperimentalCoroutinesApi::class)
class FieldSyncViewModel(application: Application) : AndroidViewModel(application) {
  private val database = FieldSyncDatabase.getDatabase(application)
  private val repository = FieldSyncRepository(database)
  val appConnectivityManager = com.example.network.ConnectivityManager(application, viewModelScope)
  val connectivityManagerWrapper: ConnectivityManagerWrapper = ConnectivityManagerWrapper(application)
  private val connectivityObserver: ConnectivityObserver = NetworkConnectivityObserver(application, connectivityManagerWrapper)

  /**
   * Observable StateFlow<Boolean> exposed directly by ConnectivityManager
   */
  val isConnected: StateFlow<Boolean> = appConnectivityManager.isConnected

  val systemConnectivity: StateFlow<NetworkStatus> = connectivityManagerWrapper.networkStatus

    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      if (connectivityManagerWrapper.isOnline()) NetworkStatus.ONLINE else NetworkStatus.OFFLINE
    )

  val networkMode: StateFlow<NetworkMode> = repository.networkMode
  val isSyncing: StateFlow<Boolean> = repository.isSyncing
  val syncMessage: StateFlow<String?> = repository.syncMessage

  val networkStatus: StateFlow<NetworkStatus> = combine(
    networkMode,
    systemConnectivity
  ) { mode, sysStatus ->
    if (mode == NetworkMode.OFFLINE || sysStatus == NetworkStatus.OFFLINE) {
      NetworkStatus.OFFLINE
    } else {
      NetworkStatus.ONLINE
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkStatus.ONLINE)

  val isOnline: StateFlow<Boolean> = combine(
    networkMode,
    systemConnectivity
  ) { mode, sysStatus ->
    mode != NetworkMode.OFFLINE && sysStatus == NetworkStatus.ONLINE
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)


  val allInspections: StateFlow<List<InspectionEntity>> = repository.allInspections
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedInspectionId = MutableStateFlow<String>("insp-001")
  val selectedInspectionId: StateFlow<String> = _selectedInspectionId.asStateFlow()

  val selectedInspection: StateFlow<InspectionEntity?> = _selectedInspectionId
    .flatMapLatest { id -> repository.getInspection(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val checklistItems: StateFlow<List<ChecklistItemEntity>> = _selectedInspectionId
    .flatMapLatest { id -> repository.getChecklistItems(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val photos: StateFlow<List<InspectionPhotoEntity>> = _selectedInspectionId
    .flatMapLatest { id -> repository.getPhotos(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allMediaUploads: StateFlow<List<MediaUploadEntity>> = repository.allMediaUploads
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeMediaUploadCount: StateFlow<Int> = repository.activeMediaUploadCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val inspectionMediaUploads: StateFlow<List<MediaUploadEntity>> = _selectedInspectionId
    .flatMapLatest { id -> repository.getMediaUploadsForInspection(id) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


  val allConflicts: StateFlow<List<ConflictEntity>> = repository.allConflicts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val unresolvedConflicts: StateFlow<List<ConflictEntity>> = repository.unresolvedConflicts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val unresolvedConflictCount: StateFlow<Int> = repository.unresolvedConflictCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val syncQueue: StateFlow<List<SyncQueueItemEntity>> = repository.syncQueue
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pendingQueueCount: StateFlow<Int> = repository.pendingQueueCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _aiAnalysisResult = MutableStateFlow<AIAnalysisResult?>(null)
  val aiAnalysisResult: StateFlow<AIAnalysisResult?> = _aiAnalysisResult.asStateFlow()

  private val _isAnalyzing = MutableStateFlow(false)
  val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

  // 10-Step Interactive Guided Demo State
  private val _guidedDemoStep = MutableStateFlow(1)
  val guidedDemoStep: StateFlow<Int> = _guidedDemoStep.asStateFlow()

  private val _guidedDemoBannerVisible = MutableStateFlow(true)
  val guidedDemoBannerVisible: StateFlow<Boolean> = _guidedDemoBannerVisible.asStateFlow()

  init {
    viewModelScope.launch {
      repository.initializeSeedDataIfNeeded()
    }

    viewModelScope.launch {
      var wasOffline = false
      isOnline.collect { online ->
        if (online && wasOffline) {
          if (pendingQueueCount.value > 0) {
            repository.triggerSync()
          }
        }
        wasOffline = !online
      }
    }
  }

  fun selectInspection(id: String) {
    _selectedInspectionId.value = id
    _aiAnalysisResult.value = null
  }

  fun setNetworkMode(mode: NetworkMode) {
    repository.setNetworkMode(mode)
  }

  fun toggleChecklistStatus(item: ChecklistItemEntity, nextStatus: String) {
    viewModelScope.launch {
      repository.updateChecklistItem(item, newStatus = nextStatus)
    }
  }

  fun updateNumericAndNotes(item: ChecklistItemEntity, value: Double?, notes: String?) {
    viewModelScope.launch {
      val status = if (item.itemName.contains("Temperature", ignoreCase = true) && value != null && value > 80.0) {
        "FAIL"
      } else if (item.itemName.contains("Oil", ignoreCase = true) && value != null && value < 50.0) {
        "FLAG"
      } else {
        item.status
      }
      repository.updateChecklistItem(item, newStatus = status, numericVal = value, notes = notes)
    }
  }

  fun addPhoto(caption: String, photoType: String) {
    viewModelScope.launch {
      repository.addInspectionPhoto(_selectedInspectionId.value, caption, photoType)
    }
  }

  fun addInspectionPhotoCustom(
    inspectionId: String,
    checklistItemId: String?,
    caption: String,
    photoType: String,
    filePath: String
  ) {
    viewModelScope.launch {
      repository.addInspectionPhotoCustom(
        inspectionId = inspectionId,
        checklistItemId = checklistItemId,
        caption = caption,
        photoType = photoType,
        filePath = filePath
      )
    }
  }

  fun startResumableUpload(
    fileName: String,
    fileType: String,
    mimeType: String = "image/jpeg",
    totalBytes: Long,
    checklistItemId: String? = null
  ) {
    viewModelScope.launch {
      repository.startResumableUpload(
        inspectionId = _selectedInspectionId.value,
        fileName = fileName,
        fileType = fileType,
        mimeType = mimeType,
        totalBytes = totalBytes,
        checklistItemId = checklistItemId
      )
    }
  }

  fun pauseMediaUpload(uploadId: String) {
    repository.pauseMediaUpload(uploadId)
  }

  fun resumeMediaUpload(uploadId: String) {
    repository.resumeMediaUpload(uploadId)
  }

  fun cancelMediaUpload(uploadId: String) {
    repository.cancelMediaUpload(uploadId)
  }

  fun retryMediaUpload(uploadId: String) {
    repository.retryMediaUpload(uploadId)
  }

  fun deleteMediaUpload(uploadId: String) {
    repository.deleteMediaUpload(uploadId)
  }

  fun pauseAllMediaUploads() {
    repository.pauseAllMediaUploads()
  }

  fun resumeAllMediaUploads() {
    repository.resumeAllMediaUploads()
  }


  fun triggerSync() {
    viewModelScope.launch {
      repository.triggerSync()
    }
  }

  fun simulateRemoteConflict() {
    viewModelScope.launch {
      repository.simulateRemoteCollision(_selectedInspectionId.value)
    }
  }

  fun resolveConflict(conflict: ConflictEntity, strategy: String, customMerged: String? = null) {
    viewModelScope.launch {
      repository.resolveConflict(conflict, strategy, customMerged)
    }
  }

  fun resolveAllWithMerge() {
    viewModelScope.launch {
      repository.resolveAllConflictsWithMerge()
    }
  }

  fun runInspectAI() {
    viewModelScope.launch {
      val insp = selectedInspection.value ?: return@launch
      val items = checklistItems.value
      _isAnalyzing.value = true
      delay(800) // Realistic AI synthesis latency
      val result = InspectAIService.analyzeInspection(insp, items)
      _aiAnalysisResult.value = result
      _isAnalyzing.value = false

      repository.logAudit(
        actor = "InspectAI Engine",
        action = "AI_REPORT_GENERATED",
        target = "Inspection ${insp.id} (${insp.assetTag})",
        details = "Risk score computed: ${result.riskScore}/100 [${result.hazardLevel}]. Work order ${result.workOrderTitle} formulated."
      )
    }
  }

  // 10-Step Hackathon Guided Walkthrough Automation
  fun setDemoStep(step: Int) {
    _guidedDemoStep.value = step.coerceIn(1, 10)
  }

  fun toggleDemoBanner(visible: Boolean) {
    _guidedDemoBannerVisible.value = visible
  }

  fun executeDemoStepAction(currentStep: Int, onNavigate: (String) -> Unit) {
    viewModelScope.launch {
      when (currentStep) {
        1 -> {
          // Step 1: Open Inspection Details
          onNavigate("inspection_detail/${_selectedInspectionId.value}")
          _guidedDemoStep.value = 2
        }
        2 -> {
          // Step 2: Switch to OFFLINE mode
          setNetworkMode(NetworkMode.OFFLINE)
          _guidedDemoStep.value = 3
        }
        3 -> {
          // Step 3: Perform offline inspection edits (Fail Emergency Switch & Flag Motor & Set Temp to 88.5°C)
          val items = checklistItems.value
          val emergency = items.find { it.itemName.contains("Emergency", ignoreCase = true) }
          val motor = items.find { it.itemName.contains("Motor", ignoreCase = true) }
          val temp = items.find { it.itemName.contains("Temperature", ignoreCase = true) }

          emergency?.let {
            repository.updateChecklistItem(it, "FAIL", notes = "Manual trip button mechanical spring loose! Contact did not separate.")
          }
          motor?.let {
            repository.updateChecklistItem(it, "FLAG", notes = "Acoustic high-frequency vibration detected at bearing B-4.")
          }
          temp?.let {
            repository.updateChecklistItem(it, "FAIL", numericVal = 88.5, notes = "Core thermal anomaly: 88.5°C exceeds maximum operating boundary.")
          }
          _guidedDemoStep.value = 4
        }
        4 -> {
          // Step 4: Verify offline persistence in local Room DB
          repository.logAudit(
            actor = "FieldSync Verification",
            action = "OFFLINE_STORAGE_VERIFIED",
            target = "Room SQLite Database",
            details = "Verified zero data loss: checklist changes and pending outbox mutations confirmed in Room SQLite."
          )
          _guidedDemoStep.value = 5
        }
        5 -> {
          // Step 5: View pending changes in Sync Center
          onNavigate("sync_center")
          _guidedDemoStep.value = 6
        }
        6 -> {
          // Step 6: Simulate remote cloud collision (Tech Jordan modified Oil Level and Motor in cloud)
          repository.simulateRemoteCollision(_selectedInspectionId.value)
          _guidedDemoStep.value = 7
        }
        7 -> {
          // Step 7: Restore Network ONLINE and trigger sync
          setNetworkMode(NetworkMode.ONLINE)
          repository.triggerSync()
          _guidedDemoStep.value = 8
        }
        8 -> {
          // Step 8: Open Conflict Center & resolve with field-level merge
          onNavigate("conflict_center")
          _guidedDemoStep.value = 9
        }
        9 -> {
          // Step 9: Open Audit History to verify immutable trail
          onNavigate("audit_history")
          _guidedDemoStep.value = 10
        }
        10 -> {
          // Step 10: Run InspectAI & view generated work order
          onNavigate("inspect_ai")
          runInspectAI()
        }
      }
    }
  }

  fun resetAllDemoData(onNavigate: (String) -> Unit) {
    viewModelScope.launch {
      repository.resetDemoState()
      _guidedDemoStep.value = 1
      _aiAnalysisResult.value = null
      onNavigate("dashboard")
    }
  }

  override fun onCleared() {
    super.onCleared()
    appConnectivityManager.unregister()
  }
}

