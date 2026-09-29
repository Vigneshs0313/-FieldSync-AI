package com.example.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.FieldSyncDatabase
import com.example.data.repository.FieldSyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FieldSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = FieldSyncDatabase.getInstance(applicationContext)
            val repository = FieldSyncRepository(database.fieldSyncDao())
            
            val unsyncedItems = database.fieldSyncDao().getUnsyncedItems()
            if (unsyncedItems.isNotEmpty()) {
                repository.processSyncQueue()
                Result.success()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
