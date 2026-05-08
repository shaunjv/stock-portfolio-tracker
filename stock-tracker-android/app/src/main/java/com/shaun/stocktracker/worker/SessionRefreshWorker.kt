package com.shaun.stocktracker.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shaun.stocktracker.repository.AuthRepository
import com.shaun.stocktracker.util.EventLogger
import com.shaun.stocktracker.util.SessionManager

/**
 * FALLBACK session keepalive worker.
 *
 * Primary auth handling is done by PricePollingService, which detects
 * 401/403 responses and calls AuthRepository.refreshSession() directly.
 *
 * This worker runs every 30 minutes as a safety net to catch cases where:
 * - The polling service is temporarily stopped
 * - A session expires between polling cycles
 * - The app was backgrounded for an extended period
 *
 * It should NOT be relied upon as the primary auth mechanism.
 */
class SessionRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "SessionRefreshWorker"
        const val WORK_NAME = "session_refresh_work"
    }

    override suspend fun doWork(): Result {
        EventLogger.log(EventLogger.Event.WORKER_STARTED, "SessionRefreshWorker running (fallback)")
        Log.i(TAG, "Starting fallback session refresh check...")

        val sessionManager = SessionManager(applicationContext)
        val authRepository = AuthRepository(sessionManager)

        return try {
            val refreshed = authRepository.refreshSessionIfNeeded()
            if (refreshed) {
                EventLogger.log(EventLogger.Event.WORKER_COMPLETED,
                    "Session active or refreshed via fallback worker")
                Log.i(TAG, "Session is active or was refreshed successfully")
                Result.success()
            } else {
                EventLogger.log(EventLogger.Event.WORKER_FAILED,
                    "Fallback session refresh failed — will retry")
                Log.w(TAG, "Session refresh failed — will retry")
                Result.retry()
            }
        } catch (e: Exception) {
            EventLogger.log(EventLogger.Event.WORKER_FAILED,
                "Fallback session refresh error: ${e.message}")
            Log.e(TAG, "Session refresh error", e)
            Result.retry()
        }
    }
}
