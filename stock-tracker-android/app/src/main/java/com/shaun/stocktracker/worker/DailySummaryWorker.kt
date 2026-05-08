package com.shaun.stocktracker.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shaun.stocktracker.data.AppDatabase
import com.shaun.stocktracker.repository.PortfolioRepository
import com.shaun.stocktracker.util.EventLogger
import com.shaun.stocktracker.util.NetworkResult
import com.shaun.stocktracker.util.NotepadWriter
import com.shaun.stocktracker.util.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Worker that runs at 3:30 PM IST to generate and log the daily portfolio summary.
 *
 * Schedule this as a OneTimeWorkRequest with an initial delay calculated
 * to hit 3:30 PM IST. Re-enqueues itself for the next day after completion.
 *
 * Optimized: Only stores 1 PortfolioSnapshot per trading day.
 * If a snapshot for today already exists, it is replaced (upsert via REPLACE strategy).
 */
class DailySummaryWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "DailySummaryWorker"
        const val WORK_NAME = "daily_summary_work"
    }

    override suspend fun doWork(): Result {
        EventLogger.log(EventLogger.Event.WORKER_STARTED, "DailySummaryWorker running")
        Log.i(TAG, "Running daily summary...")

        val sessionManager = SessionManager(applicationContext)
        val portfolioRepo = PortfolioRepository(sessionManager)
        val notepadWriter = NotepadWriter(applicationContext)
        val db = AppDatabase.getInstance(applicationContext)

        // Fetch current holdings via NetworkResult
        val holdingsResult = portfolioRepo.getHoldings()

        val holdings = when (holdingsResult) {
            is NetworkResult.Success -> holdingsResult.data
            is NetworkResult.Unauthorized -> {
                Log.e(TAG, "Session expired during daily summary")
                EventLogger.log(EventLogger.Event.WORKER_FAILED, "Daily summary: session expired")
                rescheduleForTomorrow()
                return Result.retry()
            }
            is NetworkResult.Error -> {
                Log.e(TAG, "Failed to fetch holdings: ${holdingsResult.message}")
                EventLogger.log(EventLogger.Event.WORKER_FAILED, "Daily summary: ${holdingsResult.message}")
                return Result.retry()
            }
            is NetworkResult.Loading -> return Result.retry()
        }

        if (holdings.isEmpty()) {
            Log.w(TAG, "No holdings found — skipping daily summary")
            rescheduleForTomorrow()
            return Result.success()
        }

        // Calculate summary metrics
        val totalValue = holdings.sumOf { it.ltp * it.quantity }
        val totalPnl = holdings.sumOf { it.pnl }
        val totalInvested = holdings.sumOf { it.avgPrice * it.quantity }
        val pnlPercentage = if (totalInvested > 0) (totalPnl / totalInvested) * 100 else 0.0

        val winners = holdings.filter { it.pnl > 0 }.sortedByDescending { it.pnlPercentage }.map { it.symbol }
        val losers = holdings.filter { it.pnl < 0 }.sortedBy { it.pnlPercentage }.map { it.symbol }

        // Write to notepad file
        notepadWriter.writeDailySummary(
            portfolioValue = totalValue,
            todayPnl = totalPnl,
            pnlPercentage = pnlPercentage,
            winners = winners,
            losers = losers
        )

        // Save snapshot to Room — limited to 1 per trading day
        // OnConflictStrategy.REPLACE in DAO ensures upsert behavior
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }
        val today = dateFormat.format(Date())

        // Check if snapshot already exists for today
        val existingSnapshot = db.portfolioSnapshotDao().getByDate(today)
        if (existingSnapshot != null) {
            Log.d(TAG, "Snapshot for $today already exists — updating")
        }

        db.portfolioSnapshotDao().insert(
            com.shaun.stocktracker.data.entity.PortfolioSnapshot(
                id = existingSnapshot?.id ?: 0,
                totalValue = totalValue,
                totalPnl = totalPnl,
                pnlPercentage = pnlPercentage,
                winners = winners.joinToString(","),
                losers = losers.joinToString(","),
                snapshotDate = today
            )
        )

        EventLogger.log(EventLogger.Event.WORKER_COMPLETED,
            "Daily summary written. Value: ₹$totalValue, P&L: ₹$totalPnl")
        Log.i(TAG, "Daily summary written. Value: ₹$totalValue, P&L: ₹$totalPnl")

        // Re-schedule for next trading day
        rescheduleForTomorrow()

        return Result.success()
    }

    /**
     * Re-enqueue this worker for the next day's 3:30 PM IST.
     * Uses REPLACE policy here since we're re-scheduling after completion.
     */
    private fun rescheduleForTomorrow() {
        WorkerScheduler.scheduleDailySummary(applicationContext)
    }
}
