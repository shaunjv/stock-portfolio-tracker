package com.shaun.stocktracker.data.dao

import androidx.room.*
import com.shaun.stocktracker.data.entity.AlertCondition
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Query("SELECT * FROM alert_conditions ORDER BY createdAt DESC")
    suspend fun getAll(): List<AlertCondition>

    @Query("SELECT * FROM alert_conditions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AlertCondition>>

    @Query("SELECT * FROM alert_conditions WHERE isEnabled = 1 ORDER BY createdAt DESC")
    suspend fun getActiveAlerts(): List<AlertCondition>

    @Query("SELECT * FROM alert_conditions WHERE isEnabled = 1 ORDER BY createdAt DESC")
    fun observeActiveAlerts(): Flow<List<AlertCondition>>

    @Query("SELECT * FROM alert_conditions WHERE tradingSymbol = :symbol ORDER BY createdAt DESC")
    suspend fun getAlertsBySymbol(symbol: String): List<AlertCondition>

    @Query("SELECT * FROM alert_conditions WHERE tradingSymbol = :symbol AND isEnabled = 1")
    suspend fun getActiveAlertsBySymbol(symbol: String): List<AlertCondition>

    @Query("SELECT * FROM alert_conditions WHERE lastTriggeredTimestamp IS NOT NULL ORDER BY lastTriggeredTimestamp DESC")
    suspend fun getTriggeredAlerts(): List<AlertCondition>

    @Query("SELECT * FROM alert_conditions WHERE isEnabled = 0 ORDER BY createdAt DESC")
    suspend fun getDisabledAlerts(): List<AlertCondition>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: AlertCondition): Long

    @Update
    suspend fun update(alert: AlertCondition)

    @Delete
    suspend fun delete(alert: AlertCondition)

    @Query("UPDATE alert_conditions SET isEnabled = 1 WHERE id = :id")
    suspend fun enableAlert(id: Long)

    @Query("UPDATE alert_conditions SET isEnabled = 0 WHERE id = :id")
    suspend fun disableAlert(id: Long)

    @Query("UPDATE alert_conditions SET lastTriggeredTimestamp = :timestamp WHERE id = :id")
    suspend fun updateTriggerTimestamp(id: Long, timestamp: Long)

    @Query("DELETE FROM alert_conditions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
