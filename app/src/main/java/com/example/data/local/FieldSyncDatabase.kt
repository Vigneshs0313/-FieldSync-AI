package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        InspectionEntity::class,
        ChecklistItemEntity::class,
        SyncQueueEntity::class,
        ConflictEntity::class,
        AuditEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FieldSyncDatabase : RoomDatabase() {
    abstract fun fieldSyncDao(): FieldSyncDao

    companion object {
        @Volatile
        private var INSTANCE: FieldSyncDatabase? = null

        fun getInstance(context: Context): FieldSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FieldSyncDatabase::class.java,
                    "fieldsync_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
