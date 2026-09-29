package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class InspectionEntity(
  @PrimaryKey val id: String,
  val title: String,
  val assetName: String,
  val assetTag: String,
  val facility: String,
  val status: String, // PENDING, IN_PROGRESS, NEEDS_REVIEW, COMPLETED
  val assignedTechnician: String,
  val createdAt: Long,
  val updatedAt: Long,
  val syncedAt: Long?,
  val syncVersion: Int,
  val hasLocalChanges: Boolean
)

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
  @PrimaryKey val id: String,
  val inspectionId: String,
  val itemName: String,
  val category: String,
  val status: String, // PASS, FAIL, FLAG, PENDING
  val valueType: String, // STATUS, NUMERIC, TEXT
  val numericValue: Double? = null,
  val unit: String? = null,
  val notes: String = "",
  val severity: String = "NORMAL", // NORMAL, LOW, MEDIUM, HIGH, CRITICAL
  val updatedAt: Long = System.currentTimeMillis(),
  val lastModifiedBy: String = "Tech Alex"
)

@Entity(tableName = "inspection_photos")
data class InspectionPhotoEntity(
  @PrimaryKey val id: String,
  val inspectionId: String,
  val checklistItemId: String? = null,
  val caption: String,
  val photoType: String = "GENERAL", // MOTOR, THERMAL, SWITCH, PANEL, OIL, GENERAL
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_queue")
data class SyncQueueItemEntity(
  @PrimaryKey val id: String,
  val entityType: String, // INSPECTION, CHECKLIST, PHOTO, MERGE
  val entityId: String,
  val action: String, // UPSERT, UPDATE, RESOLVE_CONFLICT
  val summary: String,
  val timestamp: Long = System.currentTimeMillis(),
  val status: String = "PENDING", // PENDING, SYNCING, COMPLETED, FAILED
  val retryCount: Int = 0,
  val errorMessage: String? = null
)

@Entity(tableName = "conflicts")
data class ConflictEntity(
  @PrimaryKey val id: String,
  val inspectionId: String,
  val fieldName: String,
  val localAuthor: String,
  val localValue: String,
  val localTimestamp: Long,
  val remoteAuthor: String,
  val remoteValue: String,
  val remoteTimestamp: Long,
  val isResolved: Boolean = false,
  val resolvedValue: String? = null,
  val resolvedStrategy: String? = null // LOCAL, REMOTE, FIELD_MERGE
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
  @PrimaryKey val id: String,
  val timestamp: Long = System.currentTimeMillis(),
  val actor: String,
  val action: String, // OFFLINE_EDIT, SYNC_ATTEMPT, CONFLICT_DETECTED, CONFLICT_RESOLVED, AI_ASSESSMENT
  val targetEntity: String,
  val details: String,
  val beforeState: String? = null,
  val afterState: String? = null,
  val hashSignature: String = ""
)
