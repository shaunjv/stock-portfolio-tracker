package com.shaun.stocktracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shaun.stocktracker.data.converter.Converters
import com.shaun.stocktracker.data.dao.AlertDao
import com.shaun.stocktracker.data.dao.AlertHistoryDao
import com.shaun.stocktracker.data.dao.PortfolioSnapshotDao
import com.shaun.stocktracker.data.entity.AlertCondition
import com.shaun.stocktracker.data.entity.AlertHistory
import com.shaun.stocktracker.data.entity.PortfolioSnapshot

@Database(
    entities = [
        AlertCondition::class,
        AlertHistory::class,
        PortfolioSnapshot::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun alertDao(): AlertDao
    abstract fun alertHistoryDao(): AlertHistoryDao
    abstract fun portfolioSnapshotDao(): PortfolioSnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stock_tracker_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
