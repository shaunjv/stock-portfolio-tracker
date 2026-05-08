package com.shaun.stocktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A single alert condition. Multiple alerts per stock are supported —
 * each row is an independent condition evaluated separately.
 */
@Entity(tableName = "alert_conditions")
data class AlertCondition(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tradingSymbol: String,
    val alertType: AlertType,
    val targetPrice: Double,
    val isEnabled: Boolean = true,
    val lastTriggeredTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
