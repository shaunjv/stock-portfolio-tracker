package com.shaun.stocktracker.data.dao

import androidx.room.*
import com.shaun.stocktracker.data.entity.AlertHistory

@Dao
interface AlertHistoryDao {

    @Query("SELECT * FROM alert_history ORDER BY triggeredAt DESC")
    suspend fun getAll(): List<AlertHistory>

    @Query("SELECT * FROM alert_history WHERE tradingSymbol = :symbol ORDER BY triggeredAt DESC")
    suspend fun getBySymbol(symbol: String): List<AlertHistory>

    @Query("SELECT * FROM alert_history WHERE triggeredAt BETWEEN :startTime AND :endTime ORDER BY triggeredAt DESC")
    suspend fun getByDateRange(startTime: Long, endTime: Long): List<AlertHistory>

    @Insert
    suspend fun insert(history: AlertHistory): Long

    @Query("DELETE FROM alert_history WHERE triggeredAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
