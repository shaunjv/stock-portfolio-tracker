package com.shaun.stocktracker.util

/**
 * Prevents duplicate notifications during rapid price fluctuations.
 *
 * Uses a combination of event key hashing and time-windowed deduplication.
 * An alert for the same symbol + type won't fire a notification again
 * within the dedup window, even if the evaluator re-triggers it
 * (which it shouldn't, due to AlertEvaluator's cooldown — this is a second safety net).
 */
class NotificationDeduplicator {

    companion object {
        /** Deduplication window: 5 minutes */
        private const val DEDUP_WINDOW_MS = 5 * 60 * 1000L

        /** Max entries before forced cleanup */
        private const val MAX_ENTRIES = 200
    }

    /** Map of "SYMBOL:ALERT_TYPE" → last notification timestamp */
    private val recentNotifications = mutableMapOf<String, Long>()

    /**
     * Check if a notification should be sent for this alert.
     * Returns true if enough time has passed since the last notification
     * for the same symbol + alertType combination.
     *
     * @param symbol    Stock symbol (e.g., "RELIANCE")
     * @param alertType "ABOVE" or "BELOW"
     * @return true if the notification should be sent
     */
    @Synchronized
    fun shouldNotify(symbol: String, alertType: String): Boolean {
        val key = "$symbol:$alertType"
        val now = System.currentTimeMillis()
        val lastTime = recentNotifications[key]

        if (lastTime != null && (now - lastTime) < DEDUP_WINDOW_MS) {
            return false
        }

        recentNotifications[key] = now

        // Prevent unbounded growth
        if (recentNotifications.size > MAX_ENTRIES) {
            cleanup(now)
        }

        return true
    }

    /**
     * Remove expired entries from the dedup map.
     */
    @Synchronized
    fun cleanup(now: Long = System.currentTimeMillis()) {
        recentNotifications.entries.removeAll { (now - it.value) > DEDUP_WINDOW_MS }
    }
}
