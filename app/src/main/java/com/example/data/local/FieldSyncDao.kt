package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldSyncDao {

    // Inspections
    @Query("SELECT * FROM inspections ORDER BY updatedAt DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id")
    fun getInspectionById(id: String): Flow<InspectionEntity?>

    @Query("SELECT * FROM inspections WHERE id = :id")
    suspend fun getInspectionByIdSync(id: String): InspectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspections(inspections: List<InspectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Update
    suspend fun updateInspection(inspection: InspectionEntity)

    // Checklist Items
    @Query("SELECT * FROM checklist_items WHERE inspectionId = :inspectionId ORDER BY id ASC")
    fun getItemsForInspection(inspectionId: String): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items WHERE inspectionId = :inspectionId")
    suspend fun getItemsForInspectionSync(inspectionId: String): List<ChecklistItemEntity>

    @Query("SELECT * FROM checklist_items WHERE id = :id")
    suspend fun getItemById(id: String): ChecklistItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)

    // Sync Queue
    @Query("SELECT * FROM sync_queue ORDER BY createdAt DESC")
    fun getAllSyncItems(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getUnsyncedItems(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncQueueEntity): Long

    @Update
    suspend fun updateSyncItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun deleteSyncedItems()

    @Query("DELETE FROM sync_queue")
    suspend fun clearSyncQueue()

    // Conflicts
    @Query("SELECT * FROM conflicts ORDER BY techATimestamp DESC")
    fun getAllConflicts(): Flow<List<ConflictEntity>>

    @Query("SELECT * FROM conflicts WHERE status = 'UNRESOLVED' ORDER BY techATimestamp DESC")
    fun getUnresolvedConflicts(): Flow<List<ConflictEntity>>

    @Query("SELECT COUNT(*) FROM conflicts WHERE status = 'UNRESOLVED'")
    fun getUnresolvedConflictCountFlow(): Flow<Int>

    @Query("SELECT * FROM conflicts WHERE id = :id")
    suspend fun getConflictById(id: String): ConflictEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: ConflictEntity)

    @Update
    suspend fun updateConflict(conflict: ConflictEntity)

    // Audit Events
    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC")
    fun getAllAuditEvents(): Flow<List<AuditEventEntity>>

    @Query("SELECT * FROM audit_events WHERE inspectionId = :inspectionId ORDER BY timestamp DESC")
    fun getAuditEventsForInspection(inspectionId: String): Flow<List<AuditEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditEvent(event: AuditEventEntity)

    // Database Reset
    @Query("DELETE FROM inspections")
    suspend fun clearInspections()

    @Query("DELETE FROM checklist_items")
    suspend fun clearChecklistItems()

    @Query("DELETE FROM conflicts")
    suspend fun clearConflicts()

    @Query("DELETE FROM audit_events")
    suspend fun clearAuditEvents()
}
