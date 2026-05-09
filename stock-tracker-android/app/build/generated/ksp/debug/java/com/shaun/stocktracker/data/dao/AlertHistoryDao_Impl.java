package com.shaun.stocktracker.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shaun.stocktracker.data.converter.Converters;
import com.shaun.stocktracker.data.entity.AlertHistory;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AlertHistoryDao_Impl implements AlertHistoryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AlertHistory> __insertionAdapterOfAlertHistory;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteOlderThan;

  public AlertHistoryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAlertHistory = new EntityInsertionAdapter<AlertHistory>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `alert_history` (`id`,`tradingSymbol`,`alertType`,`targetPrice`,`triggeredPrice`,`triggeredAt`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AlertHistory entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTradingSymbol());
        final String _tmp = __converters.fromAlertType(entity.getAlertType());
        statement.bindString(3, _tmp);
        statement.bindDouble(4, entity.getTargetPrice());
        statement.bindDouble(5, entity.getTriggeredPrice());
        statement.bindLong(6, entity.getTriggeredAt());
      }
    };
    this.__preparedStmtOfDeleteOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM alert_history WHERE triggeredAt < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final AlertHistory history, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfAlertHistory.insertAndReturnId(history);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteOlderThan(final long timestamp,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, timestamp);
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
          __preparedStmtOfDeleteOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<AlertHistory>> $completion) {
    final String _sql = "SELECT * FROM alert_history ORDER BY triggeredAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertHistory>>() {
      @Override
      @NonNull
      public List<AlertHistory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfTriggeredPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "triggeredPrice");
          final int _cursorIndexOfTriggeredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "triggeredAt");
          final List<AlertHistory> _result = new ArrayList<AlertHistory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertHistory _item;
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
            final double _tmpTriggeredPrice;
            _tmpTriggeredPrice = _cursor.getDouble(_cursorIndexOfTriggeredPrice);
            final long _tmpTriggeredAt;
            _tmpTriggeredAt = _cursor.getLong(_cursorIndexOfTriggeredAt);
            _item = new AlertHistory(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpTriggeredPrice,_tmpTriggeredAt);
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
  public Object getBySymbol(final String symbol,
      final Continuation<? super List<AlertHistory>> $completion) {
    final String _sql = "SELECT * FROM alert_history WHERE tradingSymbol = ? ORDER BY triggeredAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, symbol);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertHistory>>() {
      @Override
      @NonNull
      public List<AlertHistory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfTriggeredPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "triggeredPrice");
          final int _cursorIndexOfTriggeredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "triggeredAt");
          final List<AlertHistory> _result = new ArrayList<AlertHistory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertHistory _item;
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
            final double _tmpTriggeredPrice;
            _tmpTriggeredPrice = _cursor.getDouble(_cursorIndexOfTriggeredPrice);
            final long _tmpTriggeredAt;
            _tmpTriggeredAt = _cursor.getLong(_cursorIndexOfTriggeredAt);
            _item = new AlertHistory(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpTriggeredPrice,_tmpTriggeredAt);
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
  public Object getByDateRange(final long startTime, final long endTime,
      final Continuation<? super List<AlertHistory>> $completion) {
    final String _sql = "SELECT * FROM alert_history WHERE triggeredAt BETWEEN ? AND ? ORDER BY triggeredAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AlertHistory>>() {
      @Override
      @NonNull
      public List<AlertHistory> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTradingSymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "tradingSymbol");
          final int _cursorIndexOfAlertType = CursorUtil.getColumnIndexOrThrow(_cursor, "alertType");
          final int _cursorIndexOfTargetPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "targetPrice");
          final int _cursorIndexOfTriggeredPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "triggeredPrice");
          final int _cursorIndexOfTriggeredAt = CursorUtil.getColumnIndexOrThrow(_cursor, "triggeredAt");
          final List<AlertHistory> _result = new ArrayList<AlertHistory>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AlertHistory _item;
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
            final double _tmpTriggeredPrice;
            _tmpTriggeredPrice = _cursor.getDouble(_cursorIndexOfTriggeredPrice);
            final long _tmpTriggeredAt;
            _tmpTriggeredAt = _cursor.getLong(_cursorIndexOfTriggeredAt);
            _item = new AlertHistory(_tmpId,_tmpTradingSymbol,_tmpAlertType,_tmpTargetPrice,_tmpTriggeredPrice,_tmpTriggeredAt);
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
