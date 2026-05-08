package com.shaun.stocktracker.data.dao

import androidx.room.*
import com.shaun.stocktracker.data.entity.PortfolioSnapshot

@Dao
interface PortfolioSnapshotDao {

    @Query("SELECT * FROM portfolio_snapshots ORDER BY capturedAt DESC")
    suspend fun getAll(): List<PortfolioSnapshot>

    @Query("SELECT * FROM portfolio_snapshots WHERE snapshotDate = :date LIMIT 1")
    suspend fun getByDate(date: String): PortfolioSnapshot?

    @Query("SELECT * FROM portfolio_snapshots ORDER BY capturedAt DESC LIMIT 1")
    suspend fun getLatest(): PortfolioSnapshot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(snapshot: PortfolioSnapshot): Long

    @Query("DELETE FROM portfolio_snapshots WHERE capturedAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}
