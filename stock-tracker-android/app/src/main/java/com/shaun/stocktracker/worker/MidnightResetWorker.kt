package com.shaun.stocktracker.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shaun.stocktracker.data.AppDatabase
import com.shaun.stocktracker.util.EventLogger
import java.util.Calendar
import java.util.TimeZone

/**
 * Worker that runs at midnight IST to perform daily cleanup tasks.
 *
 * Responsibilities:
 * - Clean up alert history older than 30 days
 * - Clean up portfolio snapshots older than 90 days
 *
 * Note: We do NOT "reset" alert triggered states because we use
 * lastTriggeredTimestamp with cooldown logic instead of a boolean flag.
 * The cooldown naturally expires — no midnight reset needed for that.
 *
 * Schedule as a OneTimeWorkRequest with initial delay to midnight IST.
 * Re-enqueue for the next midnight after completion.
 */
class MidnightResetWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "MidnightResetWorker"
        const val WORK_NAME = "midnight_reset_work"

        // Retention periods
        private const val ALERT_HISTORY_RETENTION_DAYS = 30
        private const val SNAPSHOT_RETENTION_DAYS = 90
    }

    override suspend fun doWork(): Result {
        EventLogger.log(EventLogger.Event.WORKER_STARTED, "MidnightResetWorker running")
        Log.i(TAG, "Running midnight cleanup...")

        val db = AppDatabase.getInstance(applicationContext)

        try {
            // Clean up old alert history (older than 30 days)
            val alertCutoff = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
                add(Calendar.DAY_OF_YEAR, -ALERT_HISTORY_RETENTION_DAYS)
            }.timeInMillis

            db.alertHistoryDao().deleteOlderThan(alertCutoff)
            Log.i(TAG, "Cleaned alert history older than $ALERT_HISTORY_RETENTION_DAYS days")

            // Clean up old portfolio snapshots (older than 90 days)
            val snapshotCutoff = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).apply {
                add(Calendar.DAY_OF_YEAR, -SNAPSHOT_RETENTION_DAYS)
            }.timeInMillis

            db.portfolioSnapshotDao().deleteOlderThan(snapshotCutoff)
            EventLogger.log(EventLogger.Event.DB_CLEANUP,
                "Cleaned snapshots older than $SNAPSHOT_RETENTION_DAYS days")
            Log.i(TAG, "Cleaned snapshots older than $SNAPSHOT_RETENTION_DAYS days")

            // Re-schedule for next midnight
            WorkerScheduler.scheduleMidnightReset(applicationContext)

            EventLogger.log(EventLogger.Event.WORKER_COMPLETED, "Midnight cleanup done")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Midnight cleanup error", e)
            EventLogger.log(EventLogger.Event.WORKER_FAILED, "Midnight cleanup error: ${e.message}")
            return Result.retry()
        }
    }
}
