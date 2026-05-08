package com.shaun.stocktracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Daily snapshot of portfolio value, P&L, winners, and losers.
 * Captured once per day at 3:30 PM IST by DailySummaryWorker.
 */
@Entity(tableName = "portfolio_snapshots")
data class PortfolioSnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val totalValue: Double,
    val totalPnl: Double,
    val pnlPercentage: Double,
    val winners: String,
    val losers: String,
    val snapshotDate: String,
    val capturedAt: Long = System.currentTimeMillis()
)
