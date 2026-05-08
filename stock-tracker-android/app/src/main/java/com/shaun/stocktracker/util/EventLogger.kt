package com.shaun.stocktracker.util

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Lightweight internal event logger for debugging and diagnostics.
 * Tracks session refreshes, polling results, alerts, worker executions, and errors.
 *
 * - Timestamped and structured
 * - Capped at MAX_ENTRIES to prevent memory leaks
 * - Thread-safe via synchronized access
 * - No external dependencies
 */
object EventLogger {

    private const val TAG = "EventLogger"
    private const val MAX_ENTRIES = 500

    private val IST_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    enum class Event {
        SESSION_REFRESH,
        SESSION_EXPIRED,
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        POLL_SUCCESS,
        POLL_FAILURE,
        POLL_SKIPPED,
        ALERT_TRIGGERED,
        NOTIFICATION_SENT,
        NOTIFICATION_DEDUPED,
        WORKER_STARTED,
        WORKER_COMPLETED,
        WORKER_FAILED,
        API_ERROR,
        DB_CLEANUP,
        FILE_WRITE,
        FILE_WRITE_ERROR
    }

    data class LogEntry(
        val timestamp: Long,
        val event: Event,
        val message: String,
        val extras: Map<String, String> = emptyMap()
    ) {
        fun formatted(): String {
            val time = IST_FORMAT.format(Date(timestamp))
            val extrasStr = if (extras.isNotEmpty()) {
                " | ${extras.entries.joinToString(", ") { "${it.key}=${it.value}" }}"
            } else ""
            return "[$time] [$event] $message$extrasStr"
        }
    }

    private val logs = mutableListOf<LogEntry>()

    /**
     * Log an event with an optional message and key-value extras.
     */
    @Synchronized
    fun log(event: Event, message: String, extras: Map<String, String> = emptyMap()) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            event = event,
            message = message,
            extras = extras
        )
        logs.add(entry)
        Log.d(TAG, entry.formatted())

        // Trim oldest entries if over capacity
        if (logs.size > MAX_ENTRIES) {
            val excess = logs.size - MAX_ENTRIES
            repeat(excess) { logs.removeFirst() }
        }
    }

    /** Get the most recent N log entries. */
    @Synchronized
    fun getRecentLogs(count: Int = 50): List<LogEntry> =
        logs.takeLast(count).reversed()

    /** Get all logs for a specific event type. */
    @Synchronized
    fun getLogsByEvent(event: Event): List<LogEntry> =
        logs.filter { it.event == event }

    /** Get all logs as formatted strings (useful for debug screens). */
    @Synchronized
    fun dump(): String =
        logs.joinToString("\n") { it.formatted() }

    /** Clear all logs. */
    @Synchronized
    fun clear() = logs.clear()
}
