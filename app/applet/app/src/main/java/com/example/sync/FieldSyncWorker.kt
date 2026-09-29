package com.example.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.FieldSyncDatabase
import com.example.data.FieldSyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FieldSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = FieldSyncDatabase.getDatabase(applicationContext)
            val repository = FieldSyncRepository(database)
            
            val success = repository.triggerSync()
            if (success) {
                Result.success()
            } else {
                Result.retry()
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
