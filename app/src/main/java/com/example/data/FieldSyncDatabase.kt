package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AuditDao
import com.example.data.dao.ChecklistDao
import com.example.data.dao.ConflictDao
import com.example.data.dao.InspectionDao
import com.example.data.dao.MediaUploadDao
import com.example.data.dao.PhotoDao
import com.example.data.dao.SyncQueueDao
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.ChecklistItemEntity
import com.example.data.entity.ConflictEntity
import com.example.data.entity.InspectionEntity
import com.example.data.entity.InspectionPhotoEntity
import com.example.data.entity.MediaUploadEntity
import com.example.data.entity.SyncQueueItemEntity

@Database(
  entities = [
    InspectionEntity::class,
    ChecklistItemEntity::class,
    InspectionPhotoEntity::class,
    SyncQueueItemEntity::class,
    ConflictEntity::class,
    AuditLogEntity::class,
    MediaUploadEntity::class
  ],
  version = 3,
  exportSchema = false
)
abstract class FieldSyncDatabase : RoomDatabase() {
  abstract fun inspectionDao(): InspectionDao
  abstract fun checklistDao(): ChecklistDao
  abstract fun photoDao(): PhotoDao
  abstract fun syncQueueDao(): SyncQueueDao
  abstract fun conflictDao(): ConflictDao
  abstract fun auditDao(): AuditDao
  abstract fun mediaUploadDao(): MediaUploadDao

  companion object {
    @Volatile
    private var INSTANCE: FieldSyncDatabase? = null

    fun getDatabase(context: Context): FieldSyncDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          FieldSyncDatabase::class.java,
          "fieldsync_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
