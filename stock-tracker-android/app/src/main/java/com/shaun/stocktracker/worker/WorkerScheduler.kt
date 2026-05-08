package com.shaun.stocktracker.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import java.util.Calendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Schedules all WorkManager workers at the correct times.
 * Call WorkerScheduler.scheduleAll(context) from your Application.onCreate()
 * or from the Activity that starts the monitoring flow.
 */
object WorkerScheduler {

    private const val TAG = "WorkerScheduler"
    private val IST = TimeZone.getTimeZone("Asia/Kolkata")

    /**
     * Schedules all three workers:
     * 1. SessionRefreshWorker — periodic, every 30 minutes
     * 2. DailySummaryWorker — one-time, at 3:30 PM IST (re-enqueues daily)
     * 3. MidnightResetWorker — one-time, at midnight IST (re-enqueues daily)
     */
    fun scheduleAll(context: Context) {
        scheduleSessionRefresh(context)
        scheduleDailySummary(context)
        scheduleMidnightReset(context)
        Log.i(TAG, "All workers scheduled")
    }

    /**
     * Session refresh: runs every 30 minutes to keep Angel One session alive.
     * Uses KEEP policy — won't replace an existing schedule.
     */
    private fun scheduleSessionRefresh(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SessionRefreshWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SessionRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        Log.d(TAG, "Session refresh worker scheduled (every 30 min)")
    }

    /**
     * Daily summary: runs once at 3:30 PM IST.
     * Calculates the initial delay from now to the next 3:30 PM IST.
     * Must be re-scheduled after each execution (call from DailySummaryWorker.doWork).
     */
    fun scheduleDailySummary(context: Context) {
        val delayMs = getDelayUntil(15, 30) // 3:30 PM IST
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DailySummaryWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
            DailySummaryWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
        Log.d(TAG, "Daily summary scheduled in ${delayMs / 60000} minutes")
    }

    /**
     * Midnight reset: runs once at 12:00 AM IST.
     * Calculates the initial delay from now to the next midnight IST.
     * Must be re-scheduled after each execution.
     */
    fun scheduleMidnightReset(context: Context) {
        val delayMs = getDelayUntil(0, 0) // 12:00 AM IST
        val request = OneTimeWorkRequestBuilder<MidnightResetWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
            MidnightResetWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
        Log.d(TAG, "Midnight reset scheduled in ${delayMs / 60000} minutes")
    }

    /**
     * Calculates milliseconds from now until the next occurrence of
     * the given hour:minute in IST. If the time has already passed
     * today, it targets the same time tomorrow.
     */
    private fun getDelayUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance(IST)
        val target = Calendar.getInstance(IST).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the target time has already passed today, schedule for tomorrow
        if (target.before(now) || target == now) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
