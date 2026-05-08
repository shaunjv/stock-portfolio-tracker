package com.shaun.stocktracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.shaun.stocktracker.data.AppDatabase
import com.shaun.stocktracker.repository.AlertRepository
import com.shaun.stocktracker.repository.AuthRepository
import com.shaun.stocktracker.repository.PortfolioRepository
import com.shaun.stocktracker.util.AlertEvaluator
import com.shaun.stocktracker.util.EventLogger
import com.shaun.stocktracker.util.NetworkResult
import com.shaun.stocktracker.util.NotepadWriter
import com.shaun.stocktracker.util.NotificationDeduplicator
import com.shaun.stocktracker.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/**
 * Foreground service that polls the FastAPI backend for live stock prices,
 * evaluates alert conditions, and writes triggered alerts to the notepad file.
 *
 * Architecture:
 *   Repository → Service → Evaluator
 *   (Service orchestrates only — no direct networking or evaluation logic)
 *
 * Market-aware polling:
 *   - 9:15 AM – 3:30 PM IST → poll every 12 seconds
 *   - Outside market hours → poll every 10 minutes (reduced battery/API usage)
 *
 * Session handling:
 *   - Detects 401/403 from repository results
 *   - Calls AuthRepository.refreshSession() then retries the failed request
 */
class PricePollingService : Service() {

    companion object {
        private const val TAG = "PricePollingService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "price_polling_channel"
        private const val CHANNEL_NAME = "Price Monitoring"
        private const val ALERT_CHANNEL_ID = "price_alert_channel"
        private const val ALERT_CHANNEL_NAME = "Price Alerts"

        /** Active market hours polling interval */
        private const val MARKET_POLLING_MS = 12_000L // 12 seconds

        /** Off-market polling interval (battery optimization) */
        private const val OFF_MARKET_POLLING_MS = 10 * 60 * 1000L // 10 minutes

        /** Market open: 9:15 AM IST */
        private const val MARKET_OPEN_HOUR = 9
        private const val MARKET_OPEN_MINUTE = 15

        /** Market close: 3:30 PM IST */
        private const val MARKET_CLOSE_HOUR = 15
        private const val MARKET_CLOSE_MINUTE = 30

        private val IST = TimeZone.getTimeZone("Asia/Kolkata")

        fun startService(context: Context) {
            val intent = Intent(context, PricePollingService::class.java)
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PricePollingService::class.java)
            context.stopService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var pollingJob: Job? = null

    private lateinit var portfolioRepository: PortfolioRepository
    private lateinit var alertRepository: AlertRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var notepadWriter: NotepadWriter
    private lateinit var sessionManager: SessionManager
    private lateinit var notificationDeduplicator: NotificationDeduplicator

    override fun onCreate() {
        super.onCreate()

        sessionManager = SessionManager(this)
        portfolioRepository = PortfolioRepository(sessionManager)
        authRepository = AuthRepository(sessionManager)
        notepadWriter = NotepadWriter(this)
        notificationDeduplicator = NotificationDeduplicator()

        val db = AppDatabase.getInstance(this)
        alertRepository = AlertRepository(db.alertDao(), db.alertHistoryDao())

        createNotificationChannels()
        EventLogger.log(EventLogger.Event.WORKER_STARTED, "PricePollingService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        startPolling()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel()
        serviceScope.cancel()
        EventLogger.log(EventLogger.Event.WORKER_COMPLETED, "PricePollingService destroyed")
    }

    /**
     * Starts the coroutine-based polling loop with market-aware intervals.
     */
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = serviceScope.launch {
            Log.i(TAG, "Polling started")

            while (isActive) {
                val interval = getCurrentPollingInterval()
                val marketOpen = isMarketOpen()

                try {
                    if (marketOpen) {
                        pollAndEvaluate()
                        EventLogger.log(EventLogger.Event.POLL_SUCCESS, "Market hours poll completed")
                    } else {
                        // Off-market: still poll (for after-hours prices) but at reduced rate
                        pollAndEvaluate()
                        EventLogger.log(EventLogger.Event.POLL_SUCCESS, "Off-market poll completed")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Polling cycle error", e)
                    EventLogger.log(EventLogger.Event.POLL_FAILURE, "Polling error: ${e.message}")
                }

                delay(interval)
            }
        }
    }

    /**
     * Returns the appropriate polling interval based on market hours.
     */
    private fun getCurrentPollingInterval(): Long {
        return if (isMarketOpen()) MARKET_POLLING_MS else OFF_MARKET_POLLING_MS
    }

    /**
     * Checks if Indian stock market is currently open.
     * Market hours: Monday–Friday, 9:15 AM – 3:30 PM IST.
     */
    private fun isMarketOpen(): Boolean {
        val now = Calendar.getInstance(IST)
        val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)

        // Weekend check
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return false
        }

        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val currentMinutes = hour * 60 + minute
        val openMinutes = MARKET_OPEN_HOUR * 60 + MARKET_OPEN_MINUTE
        val closeMinutes = MARKET_CLOSE_HOUR * 60 + MARKET_CLOSE_MINUTE

        return currentMinutes in openMinutes..closeMinutes
    }

    /**
     * Single polling cycle:
     * 1. Fetch holdings via PortfolioRepository
     * 2. On 401/403 → refresh session and retry once
     * 3. Fetch active alerts via AlertRepository
     * 4. Evaluate via AlertEvaluator
     * 5. Process triggered alerts (write to notepad, update timestamps, notify)
     */
    private suspend fun pollAndEvaluate() {
        // Step 1: Fetch live portfolio data
        var result = portfolioRepository.getHoldings()

        // Step 2: Handle Unauthorized — refresh session and retry once
        if (result is NetworkResult.Unauthorized) {
            Log.w(TAG, "Session expired — attempting refresh and retry")
            EventLogger.log(EventLogger.Event.SESSION_EXPIRED, "401/403 detected, refreshing session")

            val refreshed = authRepository.refreshSession()
            if (refreshed) {
                EventLogger.log(EventLogger.Event.SESSION_REFRESH, "Session refreshed, retrying request")
                result = portfolioRepository.getHoldings() // Retry the failed request
            } else {
                EventLogger.log(EventLogger.Event.LOGIN_FAILURE, "Session refresh failed — skipping cycle")
                return
            }
        }

        // Handle other errors
        if (result is NetworkResult.Error) {
            Log.w(TAG, "Failed to fetch holdings: ${(result as NetworkResult.Error).message}")
            return
        }

        val holdings = result.getOrNull() ?: return
        if (holdings.isEmpty()) return

        // Step 3: Fetch active alert conditions from Room
        val activeAlerts = alertRepository.getActiveAlerts()
        if (activeAlerts.isEmpty()) return

        // Step 4: Evaluate alerts (pure function — no side effects)
        val triggeredAlerts = AlertEvaluator.evaluate(holdings, activeAlerts)

        // Step 5: Process each triggered alert
        for (triggered in triggeredAlerts) {
            // Write to notepad file
            notepadWriter.writeAlertEntry(
                symbol = triggered.symbol,
                alertType = triggered.alertType,
                currentPrice = triggered.currentPrice,
                targetPrice = triggered.targetPrice
            )

            // Update timestamp in Room + record history
            alertRepository.markTriggered(triggered.condition, triggered.currentPrice)

            // Send notification (with deduplication)
            if (notificationDeduplicator.shouldNotify(triggered.symbol, triggered.alertType.name)) {
                sendAlertNotification(triggered.symbol, triggered.alertType, triggered.currentPrice)
                EventLogger.log(EventLogger.Event.NOTIFICATION_SENT,
                    "${triggered.symbol} ${triggered.alertType} at ₹${triggered.currentPrice}")
            } else {
                EventLogger.log(EventLogger.Event.NOTIFICATION_DEDUPED,
                    "${triggered.symbol} ${triggered.alertType} — notification deduped")
            }

            EventLogger.log(EventLogger.Event.ALERT_TRIGGERED,
                "${triggered.symbol} ${triggered.alertType} at ₹${triggered.currentPrice}",
                mapOf("target" to "₹${triggered.targetPrice}"))
        }
    }

    // ── Notifications ─────────────────────────────────────────

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val pollingChannel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification while monitoring stock prices"
        }

        val alertChannel = NotificationChannel(
            ALERT_CHANNEL_ID, ALERT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications when a price alert is triggered"
            enableVibration(true)
        }

        manager.createNotificationChannel(pollingChannel)
        manager.createNotificationChannel(alertChannel)
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = if (launchIntent != null) {
            PendingIntent.getActivity(
                this, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val statusText = if (isMarketOpen()) {
            "Market open — checking every ${MARKET_POLLING_MS / 1000}s"
        } else {
            "Market closed — checking every ${OFF_MARKET_POLLING_MS / 60000} min"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("📈 Monitoring Stock Prices")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_menu_report_image)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun sendAlertNotification(symbol: String, alertType: com.shaun.stocktracker.data.entity.AlertType, price: Double) {
        val isAbove = alertType == com.shaun.stocktracker.data.entity.AlertType.ABOVE
        val title = if (isAbove) "🟢 $symbol Alert" else "🔴 $symbol Alert"
        val text = if (isAbove) {
            "$symbol crossed above target at ₹$price"
        } else {
            "$symbol dropped below stop loss at ₹$price"
        }

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notifId = (symbol.hashCode() + alertType.hashCode()) and 0x7FFFFFFF
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notifId, notification)
    }
}
