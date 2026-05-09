package com.shaun.stocktracker.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shaun.stocktracker.data.converter.Converters;
import com.shaun.stocktracker.data.entity.AlertCondition;
import com.shaun.stocktracker.data.entity.AlertType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AlertDao_Impl implements AlertDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AlertCondition> __insertionAdapterOfAlertCondition;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<AlertCondition> __deletionAdapterOfAlertCondition;

  private final EntityDeletionOrUpdateAdapter<AlertCondition> __updateAdapterOfAlertCondition;

  private final SharedSQLiteStatement __preparedStmtOfEnableAlert;

  private final SharedSQLiteStatement __preparedStmtOfDisableAlert;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTriggerTimestamp;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public AlertDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAlertCondition = new EntityInsertionAdapter<AlertCondition>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `alert_conditions` (`id`,`tradingSymbol`,`alertType`,`targetPrice`,`isEnabled`,`lastTriggeredTimestamp`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlertCondition entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTradingSymbol());
        final String _tmp = __converters.fromAlertType(entity.getAlertType());
        statement.bindString(3, _tmp);
        statement.bindDouble(4, entity.getTargetPrice());
        final int _tmp_1 = entity.isEnabled() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        if (entity.getLastTriggeredTimestamp() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getLastTriggeredTimestamp());
        }
        statement.bindLong(7, entity.getCreatedAt());
      }
    };
    this.__deletionAdapterOfAlertCondition = new EntityDeletionOrUpdateAdapter<AlertCondition>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `alert_conditions` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlertCondition entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfAlertCondition = new EntityDeletionOrUpdateAdapter<AlertCondition>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `alert_conditions` SET `id` = ?,`tradingSymbol` = ?,`alertType` = ?,`targetPrice` = ?,`isEnabled` = ?,`lastTriggeredTimestamp` = ?,`createdAt` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlertCondition entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTradingSymbol());
        final String _tmp = __converters.fromAlertType(entity.getAlertType());
        statement.bindString(3, _tmp);
        statement.bindDouble(4, entity.getTargetPrice());
        final int _tmp_1 = entity.isEnabled() ? 1 : 0;
        statement.bindLong(5, _tmp_1);
        if (entity.getLastTriggeredTimestamp() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getLastTriggeredTimestamp());
        }
        statement.bindLong(7, entity.getCreatedAt());
        statement.bindLong(8, entity.getId());
      }
    };
    this.__preparedStmtOfEnableAlert = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE alert_conditions SET isEnabled = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDisableAlert = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE alert_conditions SET isEnabled = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTriggerTimestamp = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE alert_conditions SET lastTriggeredTimestamp = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM alert_conditions WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AlertCondition alert, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAlertCondition.insertAndReturnId(alert);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final AlertCondition alert, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAlertCondition.handle(alert);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final AlertCondition alert, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAlertCondition.handle(alert);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object enableAlert(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfEnableAlert.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfEnableAlert.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object disableAlert(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDisableAlert.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDisableAlert.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTriggerTimestamp(final long id, final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTriggerTimestamp.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateTriggerTimestamp.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<AlertCondition>> $completion) {
    final String _sql = "SELECT * FROM alert_conditions ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertCondition>>() {
      @Override
      @NonNull
      public List<AlertCondition> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastTriggeredTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTriggeredTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlertCondition> _result = new ArrayList<AlertCondition>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertCondition _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTradingSymbol;
            _tmpTradingSymbol = _cursor.getString(_cursorIndexOfTradingSymbol);
            final AlertType _tmpAlertType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAlertType);
            _tmpAlertType = __converters.toAlertType(_tmp);
            final double _tmpTargetPrice;
            _tmpTargetPrice = _cursor.getDouble(_cursorIndexOfTargetPrice);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final Long _tmpLastTriggeredTimestamp;
            if (_cursor.isNull(_cursorIndexOfLastTriggeredTimestamp)) {
              _tmpLastTriggeredTimestamp = null;
            } else {
              _tmpLastTriggeredTimestamp = _cursor.getLong(_cursorIndexOfLastTriggeredTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlertCondition(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpIsEnabled,_tmpLastTriggeredTimestamp,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AlertCondition>> observeAll() {
    final String _sql = "SELECT * FROM alert_conditions ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"alert_conditions"}, new Callable<List<AlertCondition>>() {
      @Override
      @NonNull
      public List<AlertCondition> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastTriggeredTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTriggeredTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlertCondition> _result = new ArrayList<AlertCondition>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertCondition _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTradingSymbol;
            _tmpTradingSymbol = _cursor.getString(_cursorIndexOfTradingSymbol);
            final AlertType _tmpAlertType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAlertType);
            _tmpAlertType = __converters.toAlertType(_tmp);
            final double _tmpTargetPrice;
            _tmpTargetPrice = _cursor.getDouble(_cursorIndexOfTargetPrice);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final Long _tmpLastTriggeredTimestamp;
            if (_cursor.isNull(_cursorIndexOfLastTriggeredTimestamp)) {
              _tmpLastTriggeredTimestamp = null;
            } else {
              _tmpLastTriggeredTimestamp = _cursor.getLong(_cursorIndexOfLastTriggeredTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlertCondition(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpIsEnabled,_tmpLastTriggeredTimestamp,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getActiveAlerts(final Continuation<? super List<AlertCondition>> $completion) {
    final String _sql = "SELECT * FROM alert_conditions WHERE isEnabled = 1 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertCondition>>() {
      @Override
      @NonNull
      public List<AlertCondition> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastTriggeredTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTriggeredTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlertCondition> _result = new ArrayList<AlertCondition>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertCondition _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTradingSymbol;
            _tmpTradingSymbol = _cursor.getString(_cursorIndexOfTradingSymbol);
            final AlertType _tmpAlertType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAlertType);
            _tmpAlertType = __converters.toAlertType(_tmp);
            final double _tmpTargetPrice;
            _tmpTargetPrice = _cursor.getDouble(_cursorIndexOfTargetPrice);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final Long _tmpLastTriggeredTimestamp;
            if (_cursor.isNull(_cursorIndexOfLastTriggeredTimestamp)) {
              _tmpLastTriggeredTimestamp = null;
            } else {
              _tmpLastTriggeredTimestamp = _cursor.getLong(_cursorIndexOfLastTriggeredTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlertCondition(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpIsEnabled,_tmpLastTriggeredTimestamp,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AlertCondition>> observeActiveAlerts() {
    final String _sql = "SELECT * FROM alert_conditions WHERE isEnabled = 1 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"alert_conditions"}, new Callable<List<AlertCondition>>() {
      @Override
      @NonNull
      public List<AlertCondition> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastTriggeredTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTriggeredTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlertCondition> _result = new ArrayList<AlertCondition>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertCondition _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTradingSymbol;
            _tmpTradingSymbol = _cursor.getString(_cursorIndexOfTradingSymbol);
            final AlertType _tmpAlertType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAlertType);
            _tmpAlertType = __converters.toAlertType(_tmp);
            final double _tmpTargetPrice;
            _tmpTargetPrice = _cursor.getDouble(_cursorIndexOfTargetPrice);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final Long _tmpLastTriggeredTimestamp;
            if (_cursor.isNull(_cursorIndexOfLastTriggeredTimestamp)) {
              _tmpLastTriggeredTimestamp = null;
            } else {
              _tmpLastTriggeredTimestamp = _cursor.getLong(_cursorIndexOfLastTriggeredTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlertCondition(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpIsEnabled,_tmpLastTriggeredTimestamp,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAlertsBySymbol(final String symbol,
      final Continuation<? super List<AlertCondition>> $completion) {
    final String _sql = "SELECT * FROM alert_conditions WHERE tradingSymbol = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, symbol);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertCondition>>() {
      @Override
      @NonNull
      public List<AlertCondition> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastTriggeredTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTriggeredTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlertCondition> _result = new ArrayList<AlertCondition>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertCondition _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTradingSymbol;
            _tmpTradingSymbol = _cursor.getString(_cursorIndexOfTradingSymbol);
            final AlertType _tmpAlertType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAlertType);
            _tmpAlertType = __converters.toAlertType(_tmp);
            final double _tmpTargetPrice;
            _tmpTargetPrice = _cursor.getDouble(_cursorIndexOfTargetPrice);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final Long _tmpLastTriggeredTimestamp;
            if (_cursor.isNull(_cursorIndexOfLastTriggeredTimestamp)) {
              _tmpLastTriggeredTimestamp = null;
            } else {
              _tmpLastTriggeredTimestamp = _cursor.getLong(_cursorIndexOfLastTriggeredTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlertCondition(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpIsEnabled,_tmpLastTriggeredTimestamp,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getActiveAlertsBySymbol(final String symbol,
      final Continuation<? super List<AlertCondition>> $completion) {
    final String _sql = "SELECT * FROM alert_conditions WHERE tradingSymbol = ? AND isEnabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, symbol);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertCondition>>() {
      @Override
      @NonNull
      public List<AlertCondition> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastTriggeredTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTriggeredTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlertCondition> _result = new ArrayList<AlertCondition>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertCondition _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTradingSymbol;
            _tmpTradingSymbol = _cursor.getString(_cursorIndexOfTradingSymbol);
            final AlertType _tmpAlertType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAlertType);
            _tmpAlertType = __converters.toAlertType(_tmp);
            final double _tmpTargetPrice;
            _tmpTargetPrice = _cursor.getDouble(_cursorIndexOfTargetPrice);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final Long _tmpLastTriggeredTimestamp;
            if (_cursor.isNull(_cursorIndexOfLastTriggeredTimestamp)) {
              _tmpLastTriggeredTimestamp = null;
            } else {
              _tmpLastTriggeredTimestamp = _cursor.getLong(_cursorIndexOfLastTriggeredTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlertCondition(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpIsEnabled,_tmpLastTriggeredTimestamp,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTriggeredAlerts(final Continuation<? super List<AlertCondition>> $completion) {
    final String _sql = "SELECT * FROM alert_conditions WHERE lastTriggeredTimestamp IS NOT NULL ORDER BY lastTriggeredTimestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertCondition>>() {
      @Override
      @NonNull
      public List<AlertCondition> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastTriggeredTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTriggeredTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlertCondition> _result = new ArrayList<AlertCondition>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertCondition _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTradingSymbol;
            _tmpTradingSymbol = _cursor.getString(_cursorIndexOfTradingSymbol);
            final AlertType _tmpAlertType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAlertType);
            _tmpAlertType = __converters.toAlertType(_tmp);
            final double _tmpTargetPrice;
            _tmpTargetPrice = _cursor.getDouble(_cursorIndexOfTargetPrice);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final Long _tmpLastTriggeredTimestamp;
            if (_cursor.isNull(_cursorIndexOfLastTriggeredTimestamp)) {
              _tmpLastTriggeredTimestamp = null;
            } else {
              _tmpLastTriggeredTimestamp = _cursor.getLong(_cursorIndexOfLastTriggeredTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlertCondition(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpIsEnabled,_tmpLastTriggeredTimestamp,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getDisabledAlerts(final Continuation<? super List<AlertCondition>> $completion) {
    final String _sql = "SELECT * FROM alert_conditions WHERE isEnabled = 0 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertCondition>>() {
      @Override
      @NonNull
      public List<AlertCondition> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final int _cursorIndexOfLastTriggeredTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "lastTriggeredTimestamp");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<AlertCondition> _result = new ArrayList<AlertCondition>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertCondition _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTradingSymbol;
            _tmpTradingSymbol = _cursor.getString(_cursorIndexOfTradingSymbol);
            final AlertType _tmpAlertType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfAlertType);
            _tmpAlertType = __converters.toAlertType(_tmp);
            final double _tmpTargetPrice;
            _tmpTargetPrice = _cursor.getDouble(_cursorIndexOfTargetPrice);
            final boolean _tmpIsEnabled;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp_1 != 0;
            final Long _tmpLastTriggeredTimestamp;
            if (_cursor.isNull(_cursorIndexOfLastTriggeredTimestamp)) {
              _tmpLastTriggeredTimestamp = null;
            } else {
              _tmpLastTriggeredTimestamp = _cursor.getLong(_cursorIndexOfLastTriggeredTimestamp);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new AlertCondition(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpIsEnabled,_tmpLastTriggeredTimestamp,_tmpCreatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
