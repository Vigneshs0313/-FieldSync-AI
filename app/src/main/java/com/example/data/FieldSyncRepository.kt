package com.example.data

import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChecklistItemEntity
import com.example.data.entity.ConflictEntity
import com.example.data.entity.InspectionEntity
import com.example.data.entity.InspectionPhotoEntity
import com.example.data.entity.MediaUploadEntity
import com.example.data.entity.SyncQueueItemEntity
import com.example.media.ResumableMediaUploadEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

enum class NetworkMode {
  ONLINE,
  OFFLINE,
  SLOW_2G
}

class FieldSyncRepository(private val database: FieldSyncDatabase) {
  private val inspectionDao = database.inspectionDao()
  private val checklistDao = database.checklistDao()
  private val photoDao = database.photoDao()
  private val syncQueueDao = database.syncQueueDao()
  private val conflictDao = database.conflictDao()
  private val auditDao = database.auditDao()
  private val mediaUploadDao = database.mediaUploadDao()

  private val _networkMode = MutableStateFlow(NetworkMode.ONLINE)
  val networkMode: StateFlow<NetworkMode> = _networkMode.asStateFlow()

  private val _isOnline = MutableStateFlow(true)
  val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val _syncMessage = MutableStateFlow<String?>(null)
  val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

  // Resumable Media Upload Engine
  val uploadEngine = ResumableMediaUploadEngine(
    mediaUploadDao = mediaUploadDao,
    auditDao = auditDao,
    isOnlineFlow = _isOnline,
    networkModeFlow = _networkMode
  )

  val allMediaUploads: Flow<List<MediaUploadEntity>> = uploadEngine.allUploads
  val activeMediaUploadCount: Flow<Int> = uploadEngine.activeCount

  // Streams
  val allInspections: Flow<List<InspectionEntity>> = inspectionDao.getAllInspections()
  val allConflicts: Flow<List<ConflictEntity>> = conflictDao.getAllConflicts()
  val unresolvedConflicts: Flow<List<ConflictEntity>> = conflictDao.getUnresolvedConflicts()
  val unresolvedConflictCount: Flow<Int> = conflictDao.getUnresolvedCount()
  val syncQueue: Flow<List<SyncQueueItemEntity>> = syncQueueDao.getAllQueueItems()
  val pendingQueueCount: Flow<Int> = syncQueueDao.getPendingQueueCount()
  val auditLogs: Flow<List<AuditLogEntity>> = auditDao.getAllLogs()

  fun getInspection(id: String): Flow<InspectionEntity?> = inspectionDao.getInspectionById(id)
  fun getChecklistItems(inspectionId: String): Flow<List<ChecklistItemEntity>> = checklistDao.getItemsForInspection(inspectionId)
  fun getPhotos(inspectionId: String): Flow<List<InspectionPhotoEntity>> = photoDao.getPhotosForInspection(inspectionId)
  fun getMediaUploadsForInspection(inspectionId: String): Flow<List<MediaUploadEntity>> = uploadEngine.getUploadsForInspection(inspectionId)

  fun setNetworkMode(mode: NetworkMode) {
    _networkMode.value = mode
    _isOnline.value = mode != NetworkMode.OFFLINE
    CoroutineScope(Dispatchers.IO).launch {
      val details = when (mode) {
        NetworkMode.ONLINE -> "Network restored to HIGH-SPEED CLOUD SYNC"
        NetworkMode.OFFLINE -> "Network switched to OFFLINE MODE (Local Room persistence enabled)"
        NetworkMode.SLOW_2G -> "Network throttled to SLOW 2G (High latency & packet drop simulation)"
      }
      logAudit(
        actor = "NetworkManager",
        action = "CONNECTIVITY_CHANGE",
        target = "System",
        details = details
      )
    }
  }

  suspend fun startResumableUpload(
    inspectionId: String,
    fileName: String,
    fileType: String,
    mimeType: String = "image/jpeg",
    totalBytes: Long,
    checklistItemId: String? = null
  ): String {
    return uploadEngine.createAndStartUpload(
      inspectionId = inspectionId,
      fileName = fileName,
      fileType = fileType,
      mimeType = mimeType,
      totalBytes = totalBytes,
      checklistItemId = checklistItemId
    )
  }

  fun pauseMediaUpload(uploadId: String) {
    uploadEngine.pauseUpload(uploadId, isUserAction = true)
  }

  fun resumeMediaUpload(uploadId: String) {
    uploadEngine.resumeUpload(uploadId)
  }

  fun cancelMediaUpload(uploadId: String) {
    uploadEngine.cancelUpload(uploadId)
  }

  fun retryMediaUpload(uploadId: String) {
    uploadEngine.retryUpload(uploadId)
  }

  fun deleteMediaUpload(uploadId: String) {
    uploadEngine.deleteUpload(uploadId)
  }

  fun pauseAllMediaUploads() {
    uploadEngine.pauseAll()
  }

  fun resumeAllMediaUploads() {
    uploadEngine.resumeAll()
  }


  suspend fun initializeSeedDataIfNeeded() {
    val existing = inspectionDao.getAllInspections().first()
    if (existing.isNotEmpty()) return

    val now = System.currentTimeMillis()
    val insp1 = InspectionEntity(
      id = "insp-001",
      title = "Turbine Generator Unit 4B Maintenance",
      assetName = "Turbine Gen-4B",
      assetTag = "TG-4020",
      facility = "Delta High Pressure Plant",
      status = "IN_PROGRESS",
      assignedTechnician = "Tech Alex (You)",
      createdAt = now - 3600000 * 2,
      updatedAt = now,
      syncedAt = now - 3600000,
      syncVersion = 1,
      hasLocalChanges = false
    )

    val insp2 = InspectionEntity(
      id = "insp-002",
      title = "Cooling Substation Delta-9 Safety Audit",
      assetName = "Cooling Substation Delta-9",
      assetTag = "CS-9011",
      facility = "North Grid Annex",
      status = "COMPLETED",
      assignedTechnician = "Tech Alex (You)",
      createdAt = now - 86400000,
      updatedAt = now - 43200000,
      syncedAt = now - 43200000,
      syncVersion = 2,
      hasLocalChanges = false
    )

    val insp3 = InspectionEntity(
      id = "insp-003",
      title = "Hydraulic Pump Array H-12 Periodic Check",
      assetName = "Hydraulic Pump Array H-12",
      assetTag = "HP-1200",
      facility = "Sector 7 Compressor Station",
      status = "PENDING",
      assignedTechnician = "Tech Alex (You)",
      createdAt = now - 1800000,
      updatedAt = now - 1800000,
      syncedAt = now - 1800000,
      syncVersion = 1,
      hasLocalChanges = false
    )

    inspectionDao.insertInspections(listOf(insp1, insp2, insp3))

    // Checklist items for insp-001
    val items1 = listOf(
      ChecklistItemEntity(
        id = "chk-101",
        inspectionId = "insp-001",
        itemName = "Motor condition",
        category = "Mechanical",
        status = "PASS",
        valueType = "STATUS",
        notes = "Vibration amplitude within nominal ISO 10816 limits (1.8 mm/s).",
        severity = "NORMAL",
        lastModifiedBy = "Tech Alex"
      ),
      ChecklistItemEntity(
        id = "chk-102",
        inspectionId = "insp-001",
        itemName = "Oil level",
        category = "Fluid Dynamics",
        status = "PASS",
        valueType = "NUMERIC",
        numericValue = 84.0,
        unit = "%",
        notes = "Viscosity clear amber; reservoir filled to nominal level.",
        severity = "NORMAL",
        lastModifiedBy = "Tech Alex"
      ),
      ChecklistItemEntity(
        id = "chk-103",
        inspectionId = "insp-001",
        itemName = "Temperature",
        category = "Thermal",
        status = "PASS",
        valueType = "NUMERIC",
        numericValue = 68.5,
        unit = "°C",
        notes = "Stator and bearing thermal sensors operating under 75°C threshold.",
        severity = "NORMAL",
        lastModifiedBy = "Tech Alex"
      ),
      ChecklistItemEntity(
        id = "chk-104",
        inspectionId = "insp-001",
        itemName = "Emergency switch",
        category = "Safety",
        status = "PASS",
        valueType = "STATUS",
        notes = "Manual trip button disengages instantaneously. Spring return active.",
        severity = "NORMAL",
        lastModifiedBy = "Tech Alex"
      ),
      ChecklistItemEntity(
        id = "chk-105",
        inspectionId = "insp-001",
        itemName = "Panel cover",
        category = "Electrical",
        status = "PASS",
        valueType = "STATUS",
        notes = "NEMA-4 weatherproof seal secure; no moisture ingress.",
        severity = "NORMAL",
        lastModifiedBy = "Tech Alex"
      ),
      ChecklistItemEntity(
        id = "chk-106",
        inspectionId = "insp-001",
        itemName = "Safety equipment",
        category = "Compliance",
        status = "PASS",
        valueType = "STATUS",
        notes = "Fire suppression canister pressure green, PPE checklist verified.",
        severity = "NORMAL",
        lastModifiedBy = "Tech Alex"
      )
    )

    checklistDao.insertItems(items1)

    // Seed initial photos for insp-001
    val photos = listOf(
      InspectionPhotoEntity(
        id = "photo-001",
        inspectionId = "insp-001",
        caption = "Motor Drive Shaft Coupling & Vibration Sensor",
        photoType = "MOTOR",
        timestamp = now - 3600000
      ),
      InspectionPhotoEntity(
        id = "photo-002",
        inspectionId = "insp-001",
        caption = "Primary Thermal IR Imaging - Stator Core",
        photoType = "THERMAL",
        timestamp = now - 3000000
      )
    )
    photoDao.insertPhotos(photos)

    // Seed sample resumable media uploads for insp-001 & insp-002
    val seedUploads = listOf(
      MediaUploadEntity(
        id = "upl-001",
        inspectionId = "insp-001",
        checklistItemId = "item-001",
        fileName = "4K_Stator_Thermal_Scan.mp4",
        fileType = "VIDEO",
        mimeType = "video/mp4",
        localUri = "file:///storage/emulated/0/FieldSync/4K_Stator_Thermal_Scan.mp4",
        totalBytes = 13107200L, // 12.5 MB
        bytesUploaded = 8388608L, // 8.0 MB (64%)
        chunkSizeBytes = 262144, // 256 KB
        currentChunkIndex = 32,
        totalChunks = 50,
        status = "PAUSED_OFFLINE",
        uploadSpeedKbps = 0.0,
        createdAt = now - 1800000,
        updatedAt = now - 900000
      ),
      MediaUploadEntity(
        id = "upl-002",
        inspectionId = "insp-001",
        checklistItemId = "item-002",
        fileName = "Bearing_Harmonic_Acoustic.wav",
        fileType = "AUDIO",
        mimeType = "audio/wav",
        localUri = "file:///storage/emulated/0/FieldSync/Bearing_Harmonic_Acoustic.wav",
        totalBytes = 4404019L, // 4.2 MB
        bytesUploaded = 4404019L,
        chunkSizeBytes = 262144,
        currentChunkIndex = 17,
        totalChunks = 17,
        status = "COMPLETED",
        uploadSpeedKbps = 0.0,
        remoteBlobUrl = "https://storage.fieldsync.ai/inspections/insp-001/media/Bearing_Harmonic_Acoustic.wav",
        sha256Checksum = "9e107d9d372bb6826bd81d3542a419d6a3fc",
        createdAt = now - 3600000,
        updatedAt = now - 3000000,
        completedAt = now - 3000000
      ),
      MediaUploadEntity(
        id = "upl-003",
        inspectionId = "insp-002",
        fileName = "Transformer_Oil_Colorimetry_HiRes.raw",
        fileType = "THERMAL",
        mimeType = "image/x-raw",
        localUri = "file:///storage/emulated/0/FieldSync/Transformer_Oil_Colorimetry_HiRes.raw",
        totalBytes = 8808038L, // 8.4 MB
        bytesUploaded = 2097152L, // 2.0 MB (23%)
        chunkSizeBytes = 262144,
        currentChunkIndex = 8,
        totalChunks = 34,
        status = "PAUSED_USER",
        uploadSpeedKbps = 0.0,
        createdAt = now - 2400000,
        updatedAt = now - 1200000
      )
    )
    mediaUploadDao.insertUploads(seedUploads)

    logAudit(
      actor = "System",
      action = "SEED_INITIAL_DATA",
      target = "Database",
      details = "Seeded 3 industrial inspections, 6 checklist items, and 3 resumable media upload sessions."
    )
  }


  suspend fun updateChecklistItem(
    item: ChecklistItemEntity,
    newStatus: String,
    numericVal: Double? = null,
    notes: String? = null,
    severity: String? = null
  ) {
    val previousState = "${item.status} | ${item.numericValue ?: ""} ${item.unit ?: ""} | ${item.notes}"
    val updated = item.copy(
      status = newStatus,
      numericValue = numericVal ?: item.numericValue,
      notes = notes ?: item.notes,
      severity = severity ?: when (newStatus) {
        "FAIL" -> "CRITICAL"
        "FLAG" -> "HIGH"
        else -> "NORMAL"
      },
      updatedAt = System.currentTimeMillis(),
      lastModifiedBy = "Tech Alex (You)"
    )
    val nextState = "${updated.status} | ${updated.numericValue ?: ""} ${updated.unit ?: ""} | ${updated.notes}"

    checklistDao.updateItem(updated)

    // Update parent inspection status & flag local changes
    val inspection = inspectionDao.getInspectionDirect(item.inspectionId)
    if (inspection != null) {
      inspectionDao.updateInspection(
        inspection.copy(
          hasLocalChanges = true,
          updatedAt = System.currentTimeMillis(),
          status = "IN_PROGRESS"
        )
      )
    }

    // Queue for sync
    val queueItem = SyncQueueItemEntity(
      id = UUID.randomUUID().toString(),
      entityType = "CHECKLIST",
      entityId = updated.id,
      action = "UPDATE",
      summary = "Updated ${updated.itemName} -> $newStatus (${updated.numericValue ?: ""} ${updated.unit ?: ""})",
      timestamp = System.currentTimeMillis(),
      status = "PENDING"
    )
    syncQueueDao.insertQueueItem(queueItem)

    // Audit log
    val isOffline = _networkMode.value == NetworkMode.OFFLINE
    logAudit(
      actor = "Tech Alex (You)",
      action = if (isOffline) "OFFLINE_CHECKLIST_UPDATE" else "CHECKLIST_UPDATE",
      target = "${item.itemName} (${item.inspectionId})",
      details = "Item updated while ${if (isOffline) "OFFLINE" else "ONLINE"}. Queued in local outbox.",
      beforeState = previousState,
      afterState = nextState
    )
  }

  suspend fun addInspectionPhoto(
    inspectionId: String,
    caption: String,
    photoType: String
  ) {
    val photo = InspectionPhotoEntity(
      id = UUID.randomUUID().toString(),
      inspectionId = inspectionId,
      caption = caption,
      photoType = photoType,
      timestamp = System.currentTimeMillis()
    )
    photoDao.insertPhoto(photo)

    // Queue for sync
    val queueItem = SyncQueueItemEntity(
      id = UUID.randomUUID().toString(),
      entityType = "PHOTO",
      entityId = photo.id,
      action = "UPLOAD_PHOTO",
      summary = "Photo attached: $caption [$photoType]",
      timestamp = System.currentTimeMillis(),
      status = "PENDING"
    )
    syncQueueDao.insertQueueItem(queueItem)

    logAudit(
      actor = "Tech Alex (You)",
      action = "ATTACH_PHOTO",
      target = "Inspection $inspectionId",
      details = "Added verification photo: $caption ($photoType)"
    )
  }

  suspend fun addInspectionPhotoCustom(
    inspectionId: String,
    checklistItemId: String?,
    caption: String,
    photoType: String,
    filePath: String
  ) {
    val photo = InspectionPhotoEntity(
      id = UUID.randomUUID().toString(),
      inspectionId = inspectionId,
      checklistItemId = checklistItemId,
      caption = caption,
      photoType = photoType,
      timestamp = System.currentTimeMillis()
    )
    photoDao.insertPhoto(photo)

    // Queue for sync
    val queueItem = SyncQueueItemEntity(
      id = UUID.randomUUID().toString(),
      entityType = "PHOTO",
      entityId = photo.id,
      action = "UPLOAD_PHOTO",
      summary = "Camera photo attached${if (checklistItemId != null) " to item $checklistItemId" else ""}: $caption [$photoType]",
      timestamp = System.currentTimeMillis(),
      status = "PENDING"
    )
    syncQueueDao.insertQueueItem(queueItem)

    val file = java.io.File(filePath)
    uploadEngine.createAndStartUpload(
      inspectionId = inspectionId,
      fileName = file.name,
      fileType = "PHOTO",
      mimeType = "image/jpeg",
      totalBytes = if (file.exists()) file.length() else 2048576L,
      checklistItemId = checklistItemId
    )

    logAudit(
      actor = "Tech Alex (You)",
      action = "ATTACH_CAMERA_PHOTO",
      target = "Inspection $inspectionId ${if (checklistItemId != null) "-> Item $checklistItemId" else ""}",
      details = "Captured camera photo: $caption ($photoType) at path $filePath"
    )
  }

  /**
   * Simulates a remote collaboration conflict (Tech Jordan in the cloud modified the inspection concurrently).
   * This is part of the 10-step guided demo and tests the 3-way conflict resolver!
   */
  suspend fun simulateRemoteCollision(inspectionId: String = "insp-001") {
    val now = System.currentTimeMillis()

    // Conflict 1: Oil Level
    // Local (Alex): e.g. 38% (Low warning / Flag)
    // Remote (Jordan): 96% (Tech Jordan refilled oil reservoir at 14:20 PM)
    val conflict1 = ConflictEntity(
      id = "conf-oil-001",
      inspectionId = inspectionId,
      fieldName = "Oil level",
      localAuthor = "Tech Alex (You - Offline Edit)",
      localValue = "FLAGGED: 38.0% (Oil reservoir near lower threshold)",
      localTimestamp = now - 600000,
      remoteAuthor = "Tech Jordan (Cloud Sync - 14:22 UTC)",
      remoteValue = "PASSED: 96.0% (Reservoir topped up with Shell Tellus S2)",
      remoteTimestamp = now - 180000,
      isResolved = false
    )

    // Conflict 2: Emergency Switch
    // Local (Alex): FAILED (Mechanical latch loose)
    // Remote (Jordan): PASSED (Inspection bypass cleared)
    val conflict2 = ConflictEntity(
      id = "conf-switch-002",
      inspectionId = inspectionId,
      fieldName = "Emergency switch",
      localAuthor = "Tech Alex (You - Offline Edit)",
      localValue = "FAILED: Contact spring failure detected during manual press",
      localTimestamp = now - 450000,
      remoteAuthor = "Tech Jordan (Cloud Sync - 14:25 UTC)",
      remoteValue = "PASSED: Remote telemetry indicates contact closed",
      remoteTimestamp = now - 120000,
      isResolved = false
    )

    // Conflict 3: Motor condition
    val conflict3 = ConflictEntity(
      id = "conf-motor-003",
      inspectionId = inspectionId,
      fieldName = "Motor condition",
      localAuthor = "Tech Alex (You - Offline Edit)",
      localValue = "FLAGGED: Acoustic anomaly (high pitch bearing whine)",
      localTimestamp = now - 500000,
      remoteAuthor = "Tech Jordan (Cloud Sync - 14:28 UTC)",
      remoteValue = "PASSED: Nominal vibration specs on scada telemetry",
      remoteTimestamp = now - 60000,
      isResolved = false
    )

    conflictDao.insertConflicts(listOf(conflict1, conflict2, conflict3))

    // Set inspection to NEEDS_REVIEW
    val inspection = inspectionDao.getInspectionDirect(inspectionId)
    if (inspection != null) {
      inspectionDao.updateInspection(
        inspection.copy(
          status = "NEEDS_REVIEW",
          hasLocalChanges = true,
          updatedAt = now
        )
      )
    }

    logAudit(
      actor = "CloudSyncEngine",
      action = "CONFLICT_DETECTED",
      target = "Inspection $inspectionId",
      details = "3 concurrent field collisions detected between Tech Alex (offline) and Tech Jordan (cloud). Auto-merge halted; escalated to Conflict Center."
    )
  }

  /**
   * Resolves a conflict with specified strategy.
   * Strategy: "LOCAL" (Alex's edit), "REMOTE" (Jordan's cloud edit), or "FIELD_MERGE" (CRDT smart blend).
   */
  suspend fun resolveConflict(
    conflict: ConflictEntity,
    strategy: String,
    customMergedValue: String? = null
  ) {
    val finalValue = when (strategy) {
      "LOCAL" -> conflict.localValue
      "REMOTE" -> conflict.remoteValue
      "FIELD_MERGE" -> customMergedValue ?: "${conflict.remoteValue} [Verified by Tech Alex: ${conflict.localValue}]"
      else -> conflict.localValue
    }

    val updatedConflict = conflict.copy(
      isResolved = true,
      resolvedValue = finalValue,
      resolvedStrategy = strategy
    )
    conflictDao.updateConflict(updatedConflict)

    // Also update the actual checklist item in Room DB!
    val item = checklistDao.getItemByName(conflict.inspectionId, conflict.fieldName)
    if (item != null) {
      val (newStatus, numeric) = when {
        finalValue.contains("FAILED", ignoreCase = true) -> Pair("FAIL", item.numericValue)
        finalValue.contains("FLAGGED", ignoreCase = true) -> Pair("FLAG", item.numericValue)
        finalValue.contains("PASSED", ignoreCase = true) -> Pair("PASS", if (conflict.fieldName.contains("Oil", ignoreCase = true)) 96.0 else item.numericValue)
        else -> Pair(item.status, item.numericValue)
      }

      val updatedItem = item.copy(
        status = newStatus,
        numericValue = if (conflict.fieldName.contains("Oil", ignoreCase = true) && strategy != "LOCAL") 96.0 else item.numericValue,
        notes = "CRDT Merged [$strategy]: $finalValue",
        updatedAt = System.currentTimeMillis(),
        lastModifiedBy = "CRDT Resolver ($strategy)"
      )
      checklistDao.updateItem(updatedItem)
    }

    // Check if all conflicts for this inspection are resolved
    val remaining = conflictDao.getUnresolvedConflicts().first()
    if (remaining.isEmpty()) {
      val inspection = inspectionDao.getInspectionDirect(conflict.inspectionId)
      if (inspection != null) {
        inspectionDao.updateInspection(
          inspection.copy(
            status = "IN_PROGRESS",
            hasLocalChanges = true,
            updatedAt = System.currentTimeMillis()
          )
        )
      }
    }

    logAudit(
      actor = "Tech Alex (Resolver)",
      action = "CONFLICT_RESOLVED",
      target = "${conflict.fieldName} (${conflict.inspectionId})",
      details = "Resolved using strategy: $strategy. Result: $finalValue",
      beforeState = "Local: ${conflict.localValue} vs Remote: ${conflict.remoteValue}",
      afterState = finalValue
    )
  }

  suspend fun resolveAllConflictsWithMerge() {
    val unresolved = conflictDao.getUnresolvedConflicts().first()
    for (c in unresolved) {
      resolveConflict(c, "FIELD_MERGE")
    }
  }

  suspend fun triggerSync(): Boolean {
    if (_networkMode.value == NetworkMode.OFFLINE) {
      _syncMessage.value = "Cannot sync: Device is in OFFLINE mode. Please connect to network."
      delay(2500)
      _syncMessage.value = null
      return false
    }

    _isSyncing.value = true
    _syncMessage.value = "Connecting to FieldSync Cloud..."

    val latency = if (_networkMode.value == NetworkMode.SLOW_2G) 3000L else 1200L
    delay(latency)

    val pending = syncQueueDao.getPendingQueueItems()
    for (item in pending) {
      syncQueueDao.updateQueueItem(item.copy(status = "SYNCING"))
    }

    delay(latency / 2)

    val now = System.currentTimeMillis()
    for (item in pending) {
      syncQueueDao.updateQueueItem(item.copy(status = "COMPLETED"))
    }

    // Check if there are unresolved conflicts
    val conflicts = conflictDao.getUnresolvedConflicts().first()
    if (conflicts.isNotEmpty()) {
      _syncMessage.value = "Sync completed with ${conflicts.size} conflicts requiring resolution."
      logAudit(
        actor = "SyncEngine",
        action = "SYNC_CONFLICT_WARNING",
        target = "SyncQueue",
        details = "Synced pending changes, but ${conflicts.size} remote conflicts require technician intervention."
      )
    } else {
      // Mark all inspections as synced
      val inspections = inspectionDao.getAllInspections().first()
      for (insp in inspections) {
        if (insp.hasLocalChanges) {
          inspectionDao.updateInspection(
            insp.copy(
              hasLocalChanges = false,
              syncedAt = now,
              syncVersion = insp.syncVersion + 1,
              status = if (insp.status == "NEEDS_REVIEW") "IN_PROGRESS" else insp.status
            )
          )
        }
      }
      _syncMessage.value = "Sync successful! All local changes pushed to Cloud Node."
      logAudit(
        actor = "SyncEngine",
        action = "SYNC_DISPATCH_SUCCESS",
        target = "SyncQueue",
        details = "Synchronized ${pending.size} pending mutations to FieldSync Cloud Gateway."
      )
    }

    _isSyncing.value = false
    delay(2500)
    _syncMessage.value = null
    return true
  }

  suspend fun resetDemoState() {
    conflictDao.clearAll()
    syncQueueDao.clearAll()
    auditDao.clearLogs()
    mediaUploadDao.clearAll()
    // Re-seed
    val existing = inspectionDao.getAllInspections().first()
    for (e in existing) {
      inspectionDao.deleteInspection(e)
    }
    initializeSeedDataIfNeeded()
    _networkMode.value = NetworkMode.ONLINE
  }

  suspend fun logAudit(
    actor: String,
    action: String,
    target: String,
    details: String,
    beforeState: String? = null,
    afterState: String? = null
  ) {
    val id = UUID.randomUUID().toString()
    val ts = System.currentTimeMillis()
    val hash = generateHash("$id:$ts:$actor:$action:$target:$details")
    val log = AuditLogEntity(
      id = id,
      timestamp = ts,
      actor = actor,
      action = action,
      targetEntity = target,
      details = details,
      beforeState = beforeState,
      afterState = afterState,
      hashSignature = hash
    )
    auditDao.insertLog(log)
  }

  private fun generateHash(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(input.toByteArray())
    return digest.take(8).joinToString("") { "%02x".format(it) }
  }
}
