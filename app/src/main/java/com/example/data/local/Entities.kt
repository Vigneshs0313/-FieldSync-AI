package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val facility: String,
    val status: String, // IN_PROGRESS, COMPLETED, PENDING_REVIEW
    val assignedTechnician: String,
    val technicianRole: String,
    val scheduledDate: String,
    val updatedAt: Long,
    val progressPercent: Int,
    val totalItems: Int,
    val completedItems: Int,
    val failedItems: Int,
    val hasConflict: Boolean = false
)

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    @PrimaryKey val id: String,
    val inspectionId: String,
    val title: String,
    val category: String,
    val status: String, // PASS, FAIL, NA, UNCHECKED
    val severity: String, // NONE, LOW, MEDIUM, HIGH, CRITICAL
    val notes: String = "",
    val recommendation: String = "",
    val photosJson: String = "", // Comma-separated or serialized labels/paths
    val lastModifiedBy: String,
    val lastModifiedAt: Long,
    val deviceId: String,
    val version: Int = 1
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val opId: String,
    val inspectionId: String,
    val entityType: String, // CHECKLIST_ITEM, PHOTO, NOTE, CONFLICT_RESOLUTION
    val action: String, // UPDATE, UPLOAD, RESOLVE
    val payload: String,
    val status: String, // PENDING, UPLOADING, SYNCED, FAILED
    val retryCount: Int = 0,
    val progress: Int = 0, // 0 to 100
    val createdAt: Long = System.currentTimeMillis(),
    val errorMsg: String? = null
)

@Entity(tableName = "conflicts")
data class ConflictEntity(
    @PrimaryKey val id: String,
    val inspectionId: String,
    val itemId: String,
    val itemTitle: String,
    val techAName: String,
    val techARole: String,
    val techAStatus: String,
    val techASeverity: String,
    val techANotes: String,
    val techADevice: String,
    val techATimestamp: Long,
    val techBName: String,
    val techBRole: String,
    val techBStatus: String,
    val techBSeverity: String,
    val techBNotes: String,
    val techBDevice: String,
    val techBTimestamp: Long,
    val status: String, // UNRESOLVED, RESOLVED_KEEP_A, RESOLVED_KEEP_B, RESOLVED_MERGED
    val resolvedBy: String? = null,
    val resolvedAt: Long? = null,
    val resolutionSummary: String? = null
)

@Entity(tableName = "audit_events")
data class AuditEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inspectionId: String,
    val userName: String,
    val userRole: String,
    val action: String,
    val targetItem: String,
    val oldValue: String,
    val newValue: String,
    val deviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)
