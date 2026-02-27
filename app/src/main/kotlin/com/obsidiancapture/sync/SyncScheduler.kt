package com.obsidiancapture.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.obsidiancapture.data.local.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules and manages background sync workers.
 *
 * Two workers run independently:
 * - SyncWorker: Periodic fetch from server → Room (configurable interval)
 * - UploadWorker: Periodic upload of pending local changes → server (same interval)
 *
 * Both require network connectivity and use exponential backoff on failure.
 * Interval is read from PreferencesManager (default: 15 min, minimum: 15 min).
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
) {
    private val workManager = WorkManager.getInstance(context)

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /**
     * Schedule periodic sync using the user's configured interval.
     * WorkManager enforces a 15-minute minimum, so intervals below that are clamped.
     * Uses REPLACE policy so changing the interval reschedules immediately.
     */
    suspend fun schedulePeriodicSync() {
        val intervalMinutes = preferencesManager.syncIntervalMinutes.first()
            .coerceAtLeast(MIN_INTERVAL_MINUTES)

        val flexMinutes = (intervalMinutes / 3).coerceAtLeast(5)

        // Periodic server → local sync
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = flexMinutes,
            flexTimeIntervalUnit = TimeUnit.MINUTES,
        )
            .setConstraints(networkConstraint)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS,
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            syncRequest,
        )

        // Periodic local → server upload
        val uploadRequest = PeriodicWorkRequestBuilder<UploadWorker>(
            repeatInterval = intervalMinutes,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = flexMinutes,
            flexTimeIntervalUnit = TimeUnit.MINUTES,
        )
            .setConstraints(networkConstraint)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS,
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            UPLOAD_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            uploadRequest,
        )
    }

    fun triggerImmediateSync() {
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraint)
            .build()

        workManager.enqueueUniqueWork(
            SYNC_IMMEDIATE_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest,
        )
    }

    fun triggerImmediateUpload() {
        val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(networkConstraint)
            .build()

        workManager.enqueueUniqueWork(
            UPLOAD_IMMEDIATE_NAME,
            ExistingWorkPolicy.REPLACE,
            uploadRequest,
        )
    }

    fun cancelAll() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        workManager.cancelUniqueWork(UPLOAD_WORK_NAME)
    }

    companion object {
        const val SYNC_WORK_NAME = "periodic_sync"
        const val UPLOAD_WORK_NAME = "periodic_upload"
        const val SYNC_IMMEDIATE_NAME = "immediate_sync"
        const val UPLOAD_IMMEDIATE_NAME = "immediate_upload"
        const val MIN_INTERVAL_MINUTES = 15L
    }
}
