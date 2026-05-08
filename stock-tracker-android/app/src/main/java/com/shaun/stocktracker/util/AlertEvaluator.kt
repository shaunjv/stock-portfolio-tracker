package com.shaun.stocktracker.util

import com.shaun.stocktracker.data.entity.AlertCondition
import com.shaun.stocktracker.data.entity.AlertType
import com.shaun.stocktracker.network.model.Holding

/**
 * Pure evaluation engine — compares live prices against alert conditions.
 * Evaluates ALL matching alerts per stock (not just one).
 * Disabled alerts are skipped. Cooldown logic prevents spam.
 */
object AlertEvaluator {

    private const val COOLDOWN_MS = 30 * 60 * 1000L // 30 minutes

    /**
     * Evaluates all enabled alerts against current holdings.
     * Multiple alerts per symbol are evaluated independently.
     */
    fun evaluate(
        holdings: List<Holding>,
        alerts: List<AlertCondition>
    ): List<TriggeredAlert> {
        val now = System.currentTimeMillis()
        val holdingMap = holdings.associateBy { it.symbol }
        val triggered = mutableListOf<TriggeredAlert>()

        for (alert in alerts) {
            if (!alert.isEnabled) continue
            if (isOnCooldown(alert, now)) continue

            val holding = holdingMap[alert.tradingSymbol] ?: continue
            val ltp = holding.ltp

            val matched = when (alert.alertType) {
                AlertType.ABOVE -> ltp >= alert.targetPrice
                AlertType.BELOW -> ltp <= alert.targetPrice
            }

            if (matched) {
                triggered.add(
                    TriggeredAlert(
                        condition = alert,
                        currentPrice = ltp,
                        symbol = alert.tradingSymbol,
                        alertType = alert.alertType,
                        targetPrice = alert.targetPrice
                    )
                )
            }
        }

        return triggered
    }

    private fun isOnCooldown(alert: AlertCondition, now: Long): Boolean {
        val lastTriggered = alert.lastTriggeredTimestamp ?: return false
        return (now - lastTriggered) < COOLDOWN_MS
    }
}

data class TriggeredAlert(
    val condition: AlertCondition,
    val currentPrice: Double,
    val symbol: String,
    val alertType: AlertType,
    val targetPrice: Double
)
