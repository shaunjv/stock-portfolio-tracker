package com.shaun.stocktracker.repository

import com.shaun.stocktracker.data.dao.AlertDao
import com.shaun.stocktracker.data.dao.AlertHistoryDao
import com.shaun.stocktracker.data.entity.AlertCondition
import com.shaun.stocktracker.data.entity.AlertHistory
import com.shaun.stocktracker.data.entity.AlertType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for alert conditions and alert history.
 * Supports multiple alerts per stock symbol.
 */
class AlertRepository(
    private val alertDao: AlertDao,
    private val alertHistoryDao: AlertHistoryDao
) {
    // ── Observe (Flow for UI) ─────────────────────────────────
    fun observeAllAlerts(): Flow<List<AlertCondition>> = alertDao.observeAll()
    fun observeActiveAlerts(): Flow<List<AlertCondition>> = alertDao.observeActiveAlerts()

    // ── Query ─────────────────────────────────────────────────
    suspend fun getAllAlerts(): List<AlertCondition> = alertDao.getAll()
    suspend fun getActiveAlerts(): List<AlertCondition> = alertDao.getActiveAlerts()
    suspend fun getAlertsBySymbol(symbol: String): List<AlertCondition> = alertDao.getAlertsBySymbol(symbol)
    suspend fun getTriggeredAlerts(): List<AlertCondition> = alertDao.getTriggeredAlerts()
    suspend fun getDisabledAlerts(): List<AlertCondition> = alertDao.getDisabledAlerts()

    // ── Create ────────────────────────────────────────────────
    suspend fun createAlert(
        tradingSymbol: String,
        alertType: AlertType,
        targetPrice: Double
    ): Long {
        return alertDao.insert(
            AlertCondition(
                tradingSymbol = tradingSymbol,
                alertType = alertType,
                targetPrice = targetPrice
            )
        )
    }

    // ── Update ────────────────────────────────────────────────
    suspend fun updateAlert(alert: AlertCondition) = alertDao.update(alert)
    suspend fun enableAlert(id: Long) = alertDao.enableAlert(id)
    suspend fun disableAlert(id: Long) = alertDao.disableAlert(id)

    // ── Delete ────────────────────────────────────────────────
    suspend fun deleteAlert(alert: AlertCondition) = alertDao.delete(alert)
    suspend fun deleteAlertById(id: Long) = alertDao.deleteById(id)

    // ── Trigger tracking ──────────────────────────────────────
    suspend fun markTriggered(alert: AlertCondition, triggeredPrice: Double) {
        val now = System.currentTimeMillis()
        alertDao.updateTriggerTimestamp(alert.id, now)
        alertHistoryDao.insert(
            AlertHistory(
                tradingSymbol = alert.tradingSymbol,
                alertType = alert.alertType,
                targetPrice = alert.targetPrice,
                triggeredPrice = triggeredPrice,
                triggeredAt = now
            )
        )
    }

    // ── History ───────────────────────────────────────────────
    suspend fun getAlertHistory(): List<AlertHistory> = alertHistoryDao.getAll()
    suspend fun getHistoryBySymbol(symbol: String): List<AlertHistory> = alertHistoryDao.getBySymbol(symbol)
    suspend fun getHistoryByDateRange(start: Long, end: Long): List<AlertHistory> = alertHistoryDao.getByDateRange(start, end)
    suspend fun cleanupOldHistory(olderThan: Long) = alertHistoryDao.deleteOlderThan(olderThan)
}
