package com.shaun.stocktracker.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.shaun.stocktracker.data.dao.AlertDao;
import com.shaun.stocktracker.data.dao.AlertDao_Impl;
import com.shaun.stocktracker.data.dao.AlertHistoryDao;
import com.shaun.stocktracker.data.dao.AlertHistoryDao_Impl;
import com.shaun.stocktracker.data.dao.PortfolioSnapshotDao;
import com.shaun.stocktracker.data.dao.PortfolioSnapshotDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile AlertDao _alertDao;

  private volatile AlertHistoryDao _alertHistoryDao;

  private volatile PortfolioSnapshotDao _portfolioSnapshotDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `alert_conditions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tradingSymbol` TEXT NOT NULL, `alertType` TEXT NOT NULL, `targetPrice` REAL NOT NULL, `isEnabled` INTEGER NOT NULL, `lastTriggeredTimestamp` INTEGER, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `alert_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tradingSymbol` TEXT NOT NULL, `alertType` TEXT NOT NULL, `targetPrice` REAL NOT NULL, `triggeredPrice` REAL NOT NULL, `triggeredAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `portfolio_snapshots` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `totalValue` REAL NOT NULL, `totalPnl` REAL NOT NULL, `pnlPercentage` REAL NOT NULL, `winners` TEXT NOT NULL, `losers` TEXT NOT NULL, `snapshotDate` TEXT NOT NULL, `capturedAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '6c916105ba7960ec9f44328c8c2f4e5a')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `alert_conditions`");
        db.execSQL("DROP TABLE IF EXISTS `alert_history`");
        db.execSQL("DROP TABLE IF EXISTS `portfolio_snapshots`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsAlertConditions = new HashMap<String, TableInfo.Column>(7);
        _columnsAlertConditions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertConditions.put("tradingSymbol", new TableInfo.Column("tradingSymbol", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertConditions.put("alertType", new TableInfo.Column("alertType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertConditions.put("targetPrice", new TableInfo.Column("targetPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertConditions.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertConditions.put("lastTriggeredTimestamp", new TableInfo.Column("lastTriggeredTimestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertConditions.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAlertConditions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAlertConditions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAlertConditions = new TableInfo("alert_conditions", _columnsAlertConditions, _foreignKeysAlertConditions, _indicesAlertConditions);
        final TableInfo _existingAlertConditions = TableInfo.read(db, "alert_conditions");
        if (!_infoAlertConditions.equals(_existingAlertConditions)) {
          return new RoomOpenHelper.ValidationResult(false, "alert_conditions(com.shaun.stocktracker.data.entity.AlertCondition).\n"
                  + " Expected:\n" + _infoAlertConditions + "\n"
                  + " Found:\n" + _existingAlertConditions);
        }
        final HashMap<String, TableInfo.Column> _columnsAlertHistory = new HashMap<String, TableInfo.Column>(6);
        _columnsAlertHistory.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertHistory.put("tradingSymbol", new TableInfo.Column("tradingSymbol", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertHistory.put("alertType", new TableInfo.Column("alertType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertHistory.put("targetPrice", new TableInfo.Column("targetPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertHistory.put("triggeredPrice", new TableInfo.Column("triggeredPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAlertHistory.put("triggeredAt", new TableInfo.Column("triggeredAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAlertHistory = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAlertHistory = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAlertHistory = new TableInfo("alert_history", _columnsAlertHistory, _foreignKeysAlertHistory, _indicesAlertHistory);
        final TableInfo _existingAlertHistory = TableInfo.read(db, "alert_history");
        if (!_infoAlertHistory.equals(_existingAlertHistory)) {
          return new RoomOpenHelper.ValidationResult(false, "alert_history(com.shaun.stocktracker.data.entity.AlertHistory).\n"
                  + " Expected:\n" + _infoAlertHistory + "\n"
                  + " Found:\n" + _existingAlertHistory);
        }
        final HashMap<String, TableInfo.Column> _columnsPortfolioSnapshots = new HashMap<String, TableInfo.Column>(8);
        _columnsPortfolioSnapshots.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPortfolioSnapshots.put("totalValue", new TableInfo.Column("totalValue", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPortfolioSnapshots.put("totalPnl", new TableInfo.Column("totalPnl", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPortfolioSnapshots.put("pnlPercentage", new TableInfo.Column("pnlPercentage", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPortfolioSnapshots.put("winners", new TableInfo.Column("winners", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPortfolioSnapshots.put("losers", new TableInfo.Column("losers", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPortfolioSnapshots.put("snapshotDate", new TableInfo.Column("snapshotDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPortfolioSnapshots.put("capturedAt", new TableInfo.Column("capturedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPortfolioSnapshots = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPortfolioSnapshots = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPortfolioSnapshots = new TableInfo("portfolio_snapshots", _columnsPortfolioSnapshots, _foreignKeysPortfolioSnapshots, _indicesPortfolioSnapshots);
        final TableInfo _existingPortfolioSnapshots = TableInfo.read(db, "portfolio_snapshots");
        if (!_infoPortfolioSnapshots.equals(_existingPortfolioSnapshots)) {
          return new RoomOpenHelper.ValidationResult(false, "portfolio_snapshots(com.shaun.stocktracker.data.entity.PortfolioSnapshot).\n"
                  + " Expected:\n" + _infoPortfolioSnapshots + "\n"
                  + " Found:\n" + _existingPortfolioSnapshots);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "6c916105ba7960ec9f44328c8c2f4e5a", "09c8ed2ba680c5707ef0091047c2b084");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "alert_conditions","alert_history","portfolio_snapshots");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `alert_conditions`");
      _db.execSQL("DELETE FROM `alert_history`");
      _db.execSQL("DELETE FROM `portfolio_snapshots`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(AlertDao.class, AlertDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AlertHistoryDao.class, AlertHistoryDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PortfolioSnapshotDao.class, PortfolioSnapshotDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public AlertDao alertDao() {
    if (_alertDao != null) {
      return _alertDao;
    } else {
      synchronized(this) {
        if(_alertDao == null) {
          _alertDao = new AlertDao_Impl(this);
        }
        return _alertDao;
      }
    }
  }

  @Override
  public AlertHistoryDao alertHistoryDao() {
    if (_alertHistoryDao != null) {
      return _alertHistoryDao;
    } else {
      synchronized(this) {
        if(_alertHistoryDao == null) {
          _alertHistoryDao = new AlertHistoryDao_Impl(this);
        }
        return _alertHistoryDao;
      }
    }
  }

  @Override
  public PortfolioSnapshotDao portfolioSnapshotDao() {
    if (_portfolioSnapshotDao != null) {
      return _portfolioSnapshotDao;
    } else {
      synchronized(this) {
        if(_portfolioSnapshotDao == null) {
          _portfolioSnapshotDao = new PortfolioSnapshotDao_Impl(this);
        }
        return _portfolioSnapshotDao;
      }
    }
  }
}
