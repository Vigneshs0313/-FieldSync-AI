package com.example.data.repository

import android.content.Context
import com.example.data.local.AuditEventEntity
import com.example.data.local.ChecklistItemEntity
import com.example.data.local.ConflictEntity
import com.example.data.local.FieldSyncDao
import com.example.data.local.FieldSyncDatabase
import com.example.data.local.InspectionEntity
import com.example.data.local.SampleData
import com.example.data.local.SyncQueueEntity
import com.example.data.model.ConflictResolution
import com.example.data.model.ConflictStatus
import com.example.data.model.ItemStatus
import com.example.data.model.NetworkStatus
import com.example.data.model.Severity
import com.example.data.model.SyncStatus
import com.example.data.model.TechnicianUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class FieldSyncRepository(private val dao: FieldSyncDao) {

    private val _networkStatus = MutableStateFlow(NetworkStatus.ONLINE)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _currentUser = MutableStateFlow(SampleData.USERS[0]) // Raja by default
    val currentUser: StateFlow<TechnicianUser> = _currentUser.asStateFlow()

    private val _currentDeviceId = MutableStateFlow("FIELD-TAB-01")
    val currentDeviceId: StateFlow<String> = _currentDeviceId.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    val allInspections: Flow<List<InspectionEntity>> = dao.getAllInspections()
    val syncQueue: Flow<List<SyncQueueEntity>> = dao.getAllSyncItems()
    val pendingSyncCount: Flow<Int> = dao.getPendingCountFlow()
    val allConflicts: Flow<List<ConflictEntity>> = dao.getAllConflicts()
    val unresolvedConflicts: Flow<List<ConflictEntity>> = dao.getUnresolvedConflicts()
    val unresolvedConflictCount: Flow<Int> = dao.getUnresolvedConflictCountFlow()
    val auditEvents: Flow<List<AuditEventEntity>> = dao.getAllAuditEvents()

    fun getInspection(id: String): Flow<InspectionEntity?> = dao.getInspectionById(id)
    fun getItemsForInspection(inspectionId: String): Flow<List<ChecklistItemEntity>> =
        dao.getItemsForInspection(inspectionId)
    fun getAuditEventsForInspection(inspectionId: String): Flow<List<AuditEventEntity>> =
        dao.getAuditEventsForInspection(inspectionId)

    suspend fun initializeIfEmpty() = withContext(Dispatchers.IO) {
        val existing = dao.getAllInspections().firstOrNull()
        if (existing.isNullOrEmpty()) {
            dao.insertInspections(SampleData.getInitialInspections())
            dao.insertChecklistItems(SampleData.getInitialChecklistItems())
            for (audit in SampleData.getInitialAuditEvents()) {
                dao.insertAuditEvent(audit)
            }
        }
    }

    fun setNetworkStatus(status: NetworkStatus) {
        _networkStatus.value = status
    }

    fun setCurrentUser(user: TechnicianUser) {
        _currentUser.value = user
    }

    fun setDeviceId(deviceId: String) {
        _currentDeviceId.value = deviceId
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    suspend fun updateChecklistItem(
        itemId: String,
        newStatus: ItemStatus,
        newSeverity: Severity,
        newNotes: String,
        newRecommendation: String,
        photoAttached: String? = null
    ) = withContext(Dispatchers.IO) {
        val existingItem = dao.getItemById(itemId) ?: return@withContext
        val user = _currentUser.value
        val device = _currentDeviceId.value
        val now = System.currentTimeMillis()

        val photos = if (!photoAttached.isNullOrBlank()) {
            if (existingItem.photosJson.isBlank()) photoAttached else "${existingItem.photosJson},$photoAttached"
        } else {
            existingItem.photosJson
        }

        val updatedItem = existingItem.copy(
            status = newStatus.name,
            severity = newSeverity.name,
            notes = newNotes,
            recommendation = newRecommendation,
            photosJson = photos,
            lastModifiedBy = user.name,
            lastModifiedAt = now,
            deviceId = device,
            version = existingItem.version + 1
        )
        dao.updateChecklistItem(updatedItem)

        // Recompute parent inspection stats
        recomputeInspectionStats(existingItem.inspectionId)

        // Record Audit Event
        dao.insertAuditEvent(
            AuditEventEntity(
                inspectionId = existingItem.inspectionId,
                userName = user.name,
                userRole = user.role,
                action = "CHECKLIST_UPDATED",
                targetItem = existingItem.title,
                oldValue = "${existingItem.status} (${existingItem.severity})",
                newValue = "${newStatus.name} (${newSeverity.name})",
                deviceId = device,
                timestamp = now,
                details = if (newNotes.isNotBlank()) "Notes: $newNotes" else "Field checklist status updated."
            )
        )

        // Queue Sync Operation (Idempotent with opId)
        val opId = "op_${UUID.randomUUID().toString().take(8)}"
        val syncItem = SyncQueueEntity(
            opId = opId,
            inspectionId = existingItem.inspectionId,
            entityType = "CHECKLIST_ITEM",
            action = "UPDATE",
            payload = "Item: ${existingItem.title} -> ${newStatus.name} | Mod by ${user.name}",
            status = if (_networkStatus.value == NetworkStatus.OFFLINE) SyncStatus.PENDING.name else SyncStatus.PENDING.name,
            retryCount = 0,
            progress = 0,
            createdAt = now
        )
        dao.insertSyncItem(syncItem)

        if (_networkStatus.value == NetworkStatus.ONLINE) {
            // Auto sync in background when online
            processSyncQueue()
        }
    }

    suspend fun attachPhoto(itemId: String, photoName: String) = withContext(Dispatchers.IO) {
        val existingItem = dao.getItemById(itemId) ?: return@withContext
        val user = _currentUser.value
        val device = _currentDeviceId.value
        val now = System.currentTimeMillis()

        val newPhotos = if (existingItem.photosJson.isBlank()) photoName else "${existingItem.photosJson},$photoName"
        dao.updateChecklistItem(existingItem.copy(photosJson = newPhotos, lastModifiedAt = now))

        // Record Audit Event
        dao.insertAuditEvent(
            AuditEventEntity(
                inspectionId = existingItem.inspectionId,
                userName = user.name,
                userRole = user.role,
                action = "PHOTO_ATTACHED",
                targetItem = existingItem.title,
                oldValue = "Count: ${existingItem.photosJson.split(",").filter { it.isNotBlank() }.size}",
                newValue = photoName,
                deviceId = device,
                timestamp = now,
                details = "Simulated high-res site photo saved to local cache."
            )
        )

        // Queue photo sync operation
        val opId = "op_photo_${UUID.randomUUID().toString().take(8)}"
        dao.insertSyncItem(
            SyncQueueEntity(
                opId = opId,
                inspectionId = existingItem.inspectionId,
                entityType = "PHOTO",
                action = "UPLOAD",
                payload = "Photo asset: $photoName (2.1 MB) for ${existingItem.title}",
                status = SyncStatus.PENDING.name,
                retryCount = 0,
                progress = 0,
                createdAt = now
            )
        )

        if (_networkStatus.value == NetworkStatus.ONLINE) {
            processSyncQueue()
        }
    }

    suspend fun simulateTechnicianBConflict(inspectionId: String = "insp_factory_a") = withContext(Dispatchers.IO) {
        // Specifically implements:
        // Technician A (Raja): Emergency Switch = FAILED
        // Technician B (Tamil): Emergency Switch = PASSED
        // Detect conflict and show both versions, users, timestamps, then provide: Keep A / Keep B / Merge

        val items = dao.getItemsForInspectionSync(inspectionId)
        val emergencySwitch = items.firstOrNull { it.title.contains("Emergency switch", ignoreCase = true) }
            ?: items.firstOrNull() ?: return@withContext

        val now = System.currentTimeMillis()
        val conflictId = "cnf_${emergencySwitch.id}_${UUID.randomUUID().toString().take(6)}"

        // Ensure Tech A (Local) has a value
        val techAStatus = if (emergencySwitch.status == ItemStatus.FAIL.name) ItemStatus.FAIL.name else ItemStatus.FAIL.name
        val techASeverity = Severity.HIGH.name
        val techANotes = "EMERGENCY STOP BUTTON IS STUCK! Mechanical latch failed to release on physical twist test. Dangerous safety hazard!"
        val techADevice = "FIELD-TAB-01"

        // Tech B (Remote simulation)
        val techBStatus = ItemStatus.PASS.name
        val techBSeverity = Severity.NONE.name
        val techBNotes = "Emergency stop tested and reset properly. Contact block continuity verified OK with multimeter."
        val techBDevice = "FIELD-UNIT-02"

        // Update local item to Tech A's state first
        dao.updateChecklistItem(
            emergencySwitch.copy(
                status = techAStatus,
                severity = techASeverity,
                notes = techANotes,
                lastModifiedBy = "Raja",
                lastModifiedAt = now - 120000,
                deviceId = techADevice
            )
        )

        val conflict = ConflictEntity(
            id = conflictId,
            inspectionId = inspectionId,
            itemId = emergencySwitch.id,
            itemTitle = emergencySwitch.title,
            techAName = "Raja",
            techARole = "Lead Technician (Local)",
            techAStatus = techAStatus,
            techASeverity = techASeverity,
            techANotes = techANotes,
            techADevice = techADevice,
            techATimestamp = now - 120000,
            techBName = "Tamil",
            techBRole = "Field Technician (Remote Cloud)",
            techBStatus = techBStatus,
            techBSeverity = techBSeverity,
            techBNotes = techBNotes,
            techBDevice = techBDevice,
            techBTimestamp = now - 45000,
            status = ConflictStatus.UNRESOLVED.name
        )

        dao.insertConflict(conflict)

        // Mark inspection as having active conflict
        val inspection = dao.getInspectionByIdSync(inspectionId)
        if (inspection != null) {
            dao.updateInspection(inspection.copy(hasConflict = true))
        }

        // Add audit event
        dao.insertAuditEvent(
            AuditEventEntity(
                inspectionId = inspectionId,
                userName = "System Sync Engine",
                userRole = "CRDT Conflict Detector",
                action = "CONFLICT_DETECTED",
                targetItem = emergencySwitch.title,
                oldValue = "Tech A (Raja): $techAStatus",
                newValue = "Tech B (Tamil): $techBStatus",
                deviceId = "SERVER-EDGE-01",
                timestamp = now,
                details = "Concurrent edit conflict detected! Local state vs Remote state diverged. Silent overwrite prevented."
            )
        )

        _syncMessage.value = "⚠️ Conflict detected on '${emergencySwitch.title}'. Check Conflict Center!"
    }

    suspend fun resolveConflict(
        conflictId: String,
        resolution: ConflictResolution,
        customMergedNotes: String? = null
    ) = withContext(Dispatchers.IO) {
        val conflict = dao.getConflictById(conflictId) ?: return@withContext
        val item = dao.getItemById(conflict.itemId) ?: return@withContext
        val currentUser = _currentUser.value
        val now = System.currentTimeMillis()

        val finalStatus: String
        val finalSeverity: String
        val finalNotes: String
        val resolutionStatusName: String
        val summaryText: String

        when (resolution) {
            ConflictResolution.KEEP_A -> {
                finalStatus = conflict.techAStatus
                finalSeverity = conflict.techASeverity
                finalNotes = conflict.techANotes
                resolutionStatusName = ConflictStatus.RESOLVED_KEEP_A.name
                summaryText = "Kept Technician A (${conflict.techAName}) decision: $finalStatus."
            }
            ConflictResolution.KEEP_B -> {
                finalStatus = conflict.techBStatus
                finalSeverity = conflict.techBSeverity
                finalNotes = conflict.techBNotes
                resolutionStatusName = ConflictStatus.RESOLVED_KEEP_B.name
                summaryText = "Kept Technician B (${conflict.techBName}) decision: $finalStatus."
            }
            ConflictResolution.MERGE -> {
                finalStatus = ItemStatus.FAIL.name // Default to safer precautionary state on merge
                finalSeverity = Severity.MEDIUM.name
                finalNotes = customMergedNotes ?: "MERGED REVIEW: Raja reported latch sticking, while Tamil noted contacts OK. Flagged for precautionary replacement and supervisor re-test."
                resolutionStatusName = ConflictStatus.RESOLVED_MERGED.name
                summaryText = "Merged both observations. Safety precaution prioritized."
            }
        }

        // 1. Update item in database
        dao.updateChecklistItem(
            item.copy(
                status = finalStatus,
                severity = finalSeverity,
                notes = finalNotes,
                lastModifiedBy = currentUser.name,
                lastModifiedAt = now,
                deviceId = _currentDeviceId.value,
                version = item.version + 1
            )
        )

        // 2. Mark conflict resolved
        dao.updateConflict(
            conflict.copy(
                status = resolutionStatusName,
                resolvedBy = currentUser.name,
                resolvedAt = now,
                resolutionSummary = summaryText
            )
        )

        // 3. Check if any unresolved conflicts remain for inspection
        val remainingConflicts = dao.getUnresolvedConflicts().firstOrNull()?.filter {
            it.inspectionId == conflict.inspectionId && it.id != conflictId
        }
        val inspection = dao.getInspectionByIdSync(conflict.inspectionId)
        if (inspection != null && remainingConflicts.isNullOrEmpty()) {
            dao.updateInspection(inspection.copy(hasConflict = false))
        }

        // 4. Log Audit Event
        dao.insertAuditEvent(
            AuditEventEntity(
                inspectionId = conflict.inspectionId,
                userName = currentUser.name,
                userRole = currentUser.role,
                action = "CONFLICT_RESOLVED",
                targetItem = conflict.itemTitle,
                oldValue = "A: ${conflict.techAStatus} vs B: ${conflict.techBStatus}",
                newValue = "$finalStatus ($resolutionStatusName)",
                deviceId = _currentDeviceId.value,
                timestamp = now,
                details = summaryText
            )
        )

        // 5. Enqueue sync of resolution
        val opId = "op_resolve_${UUID.randomUUID().toString().take(8)}"
        dao.insertSyncItem(
            SyncQueueEntity(
                opId = opId,
                inspectionId = conflict.inspectionId,
                entityType = "CONFLICT_RESOLUTION",
                action = "RESOLVE",
                payload = "Conflict on ${conflict.itemTitle} resolved by ${currentUser.name} ($resolutionStatusName)",
                status = SyncStatus.PENDING.name,
                retryCount = 0,
                progress = 0,
                createdAt = now
            )
        )

        recomputeInspectionStats(conflict.inspectionId)
        _syncMessage.value = "✅ Conflict resolved: $summaryText"
    }

    suspend fun processSyncQueue() = withContext(Dispatchers.IO) {
        if (_networkStatus.value == NetworkStatus.OFFLINE) {
            _syncMessage.value = "Cannot sync while OFFLINE. Network is disconnected."
            return@withContext
        }

        val unsynced = dao.getUnsyncedItems()
        if (unsynced.isEmpty()) {
            _syncMessage.value = "All items are already synced!"
            return@withContext
        }

        _networkStatus.value = NetworkStatus.SYNCING

        val stepDelay = if (_networkStatus.value == NetworkStatus.SLOW_NETWORK) 400L else 150L

        for (item in unsynced) {
            // Update to uploading
            dao.updateSyncItem(item.copy(status = SyncStatus.UPLOADING.name, progress = 25))
            delay(stepDelay)

            // Resumable progress simulation (25% -> 60% -> 90% -> 100%)
            dao.updateSyncItem(item.copy(status = SyncStatus.UPLOADING.name, progress = 60))
            delay(stepDelay)

            dao.updateSyncItem(item.copy(status = SyncStatus.UPLOADING.name, progress = 90))
            delay(stepDelay)

            // Mark completed
            dao.updateSyncItem(item.copy(status = SyncStatus.SYNCED.name, progress = 100))
        }

        _networkStatus.value = NetworkStatus.ONLINE
        _syncMessage.value = "Synced ${unsynced.size} operations successfully!"

        // Record audit
        dao.insertAuditEvent(
            AuditEventEntity(
                inspectionId = unsynced.firstOrNull()?.inspectionId ?: "global",
                userName = _currentUser.value.name,
                userRole = _currentUser.value.role,
                action = "SYNC_COMPLETED",
                targetItem = "Sync Engine",
                oldValue = "Pending: ${unsynced.size}",
                newValue = "Synced: ${unsynced.size}",
                deviceId = _currentDeviceId.value,
                timestamp = System.currentTimeMillis(),
                details = "Idempotent batch upload completed with 0 errors."
            )
        )
    }

    suspend fun clearSyncedQueue() = withContext(Dispatchers.IO) {
        dao.deleteSyncedItems()
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        dao.clearInspections()
        dao.clearChecklistItems()
        dao.clearSyncQueue()
        dao.clearConflicts()
        dao.clearAuditEvents()

        dao.insertInspections(SampleData.getInitialInspections())
        dao.insertChecklistItems(SampleData.getInitialChecklistItems())
        for (audit in SampleData.getInitialAuditEvents()) {
            dao.insertAuditEvent(audit)
        }
        _syncMessage.value = "Reset to initial sample data!"
    }

    private suspend fun recomputeInspectionStats(inspectionId: String) {
        val items = dao.getItemsForInspectionSync(inspectionId)
        val total = items.size
        val completed = items.count { it.status != ItemStatus.UNCHECKED.name }
        val failed = items.count { it.status == ItemStatus.FAIL.name }
        val percent = if (total > 0) (completed * 100) / total else 0

        val inspection = dao.getInspectionByIdSync(inspectionId) ?: return
        val newStatus = when {
            completed == total && failed == 0 -> "COMPLETED"
            failed > 0 -> "IN_PROGRESS"
            completed > 0 -> "IN_PROGRESS"
            else -> inspection.status
        }

        dao.updateInspection(
            inspection.copy(
                progressPercent = percent,
                completedItems = completed,
                failedItems = failed,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: FieldSyncRepository? = null

        fun getInstance(context: Context): FieldSyncRepository {
            return INSTANCE ?: synchronized(this) {
                val db = FieldSyncDatabase.getInstance(context)
                val repo = FieldSyncRepository(db.fieldSyncDao())
                INSTANCE = repo
                repo
            }
        }
    }
}
