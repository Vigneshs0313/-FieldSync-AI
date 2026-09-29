package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChecklistItemEntity
import com.example.data.entity.ConflictEntity
import com.example.data.entity.InspectionEntity
import com.example.data.entity.InspectionPhotoEntity
import com.example.data.entity.MediaUploadEntity
import com.example.data.entity.SyncQueueItemEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface InspectionDao {
  @Query("SELECT * FROM inspections ORDER BY updatedAt DESC")
  fun getAllInspections(): Flow<List<InspectionEntity>>

  @Query("SELECT * FROM inspections WHERE id = :id")
  fun getInspectionById(id: String): Flow<InspectionEntity?>

  @Query("SELECT * FROM inspections WHERE id = :id")
  suspend fun getInspectionDirect(id: String): InspectionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInspection(inspection: InspectionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInspections(inspections: List<InspectionEntity>)

  @Update
  suspend fun updateInspection(inspection: InspectionEntity)

  @Delete
  suspend fun deleteInspection(inspection: InspectionEntity)
}

@Dao
interface ChecklistDao {
  @Query("SELECT * FROM checklist_items WHERE inspectionId = :inspectionId")
  fun getItemsForInspection(inspectionId: String): Flow<List<ChecklistItemEntity>>

  @Query("SELECT * FROM checklist_items WHERE inspectionId = :inspectionId")
  suspend fun getItemsDirect(inspectionId: String): List<ChecklistItemEntity>

  @Query("SELECT * FROM checklist_items WHERE id = :id")
  fun getItemById(id: String): Flow<ChecklistItemEntity?>

  @Query("SELECT * FROM checklist_items WHERE inspectionId = :inspectionId AND itemName = :name LIMIT 1")
  suspend fun getItemByName(inspectionId: String, name: String): ChecklistItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItems(items: List<ChecklistItemEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ChecklistItemEntity)

  @Update
  suspend fun updateItem(item: ChecklistItemEntity)

  @Delete
  suspend fun deleteItem(item: ChecklistItemEntity)
}

@Dao
interface PhotoDao {
  @Query("SELECT * FROM inspection_photos WHERE inspectionId = :inspectionId ORDER BY timestamp DESC")
  fun getPhotosForInspection(inspectionId: String): Flow<List<InspectionPhotoEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPhoto(photo: InspectionPhotoEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPhotos(photos: List<InspectionPhotoEntity>)

  @Delete
  suspend fun deletePhoto(photo: InspectionPhotoEntity)
}

@Dao
interface SyncQueueDao {
  @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
  fun getAllQueueItems(): Flow<List<SyncQueueItemEntity>>

  @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY timestamp ASC")
  suspend fun getPendingQueueItems(): List<SyncQueueItemEntity>

  @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING' OR status = 'SYNCING'")
  fun getPendingQueueCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertQueueItem(item: SyncQueueItemEntity)

  @Update
  suspend fun updateQueueItem(item: SyncQueueItemEntity)

  @Delete
  suspend fun deleteQueueItem(item: SyncQueueItemEntity)

  @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
  suspend fun clearCompleted()

  @Query("DELETE FROM sync_queue")
  suspend fun clearAll()
}

@Dao
interface ConflictDao {
  @Query("SELECT * FROM conflicts ORDER BY localTimestamp DESC")
  fun getAllConflicts(): Flow<List<ConflictEntity>>

  @Query("SELECT * FROM conflicts WHERE isResolved = 0")
  fun getUnresolvedConflicts(): Flow<List<ConflictEntity>>

  @Query("SELECT COUNT(*) FROM conflicts WHERE isResolved = 0")
  fun getUnresolvedCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertConflict(conflict: ConflictEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertConflicts(conflicts: List<ConflictEntity>)

  @Update
  suspend fun updateConflict(conflict: ConflictEntity)

  @Delete
  suspend fun deleteConflict(conflict: ConflictEntity)

  @Query("DELETE FROM conflicts")
  suspend fun clearAll()
}

@Dao
interface AuditDao {
  @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
  fun getAllLogs(): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: AuditLogEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLogs(logs: List<AuditLogEntity>)

  @Query("DELETE FROM audit_logs")
  suspend fun clearLogs()
}

@Dao
interface MediaUploadDao {
  @Query("SELECT * FROM media_uploads ORDER BY createdAt DESC")
  fun getAllUploads(): Flow<List<MediaUploadEntity>>

  @Query("SELECT * FROM media_uploads WHERE inspectionId = :inspectionId ORDER BY createdAt DESC")
  fun getUploadsForInspection(inspectionId: String): Flow<List<MediaUploadEntity>>

  @Query("SELECT * FROM media_uploads WHERE id = :id")
  fun getUploadById(id: String): Flow<MediaUploadEntity?>

  @Query("SELECT * FROM media_uploads WHERE id = :id")
  suspend fun getUploadByIdDirect(id: String): MediaUploadEntity?

  @Query("SELECT * FROM media_uploads WHERE status = 'UPLOADING'")
  suspend fun getActiveUploads(): List<MediaUploadEntity>

  @Query("SELECT * FROM media_uploads WHERE status = 'PAUSED_OFFLINE' OR status = 'QUEUED'")
  suspend fun getPendingOrOfflinePausedUploads(): List<MediaUploadEntity>

  @Query("SELECT COUNT(*) FROM media_uploads WHERE status = 'UPLOADING' OR status = 'QUEUED' OR status = 'PAUSED_OFFLINE'")
  fun getActiveOrPendingCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUpload(upload: MediaUploadEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUploads(uploads: List<MediaUploadEntity>)

  @Update
  suspend fun updateUpload(upload: MediaUploadEntity)

  @Delete
  suspend fun deleteUpload(upload: MediaUploadEntity)

  @Query("DELETE FROM media_uploads WHERE id = :id")
  suspend fun deleteUploadById(id: String)

  @Query("DELETE FROM media_uploads")
  suspend fun clearAll()
}

