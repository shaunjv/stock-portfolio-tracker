package com.shaun.stocktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records every triggered alert for analytics and history.
 */
@Entity(tableName = "alert_history")
data class AlertHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tradingSymbol: String,
    val alertType: AlertType,
    val targetPrice: Double,
    val triggeredPrice: Double,
    val triggeredAt: Long = System.currentTimeMillis()
)
