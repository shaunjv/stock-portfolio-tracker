package com.shaun.stocktracker.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shaun.stocktracker.data.entity.PortfolioSnapshot;
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
public final class PortfolioSnapshotDao_Impl implements PortfolioSnapshotDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PortfolioSnapshot> __insertionAdapterOfPortfolioSnapshot;

  private final SharedSQLiteStatement __preparedStmtOfDeleteOlderThan;

  public PortfolioSnapshotDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPortfolioSnapshot = new EntityInsertionAdapter<PortfolioSnapshot>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `portfolio_snapshots` (`id`,`totalValue`,`totalPnl`,`pnlPercentage`,`winners`,`losers`,`snapshotDate`,`capturedAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PortfolioSnapshot entity) {
        statement.bindLong(1, entity.getId());
        statement.bindDouble(2, entity.getTotalValue());
        statement.bindDouble(3, entity.getTotalPnl());
        statement.bindDouble(4, entity.getPnlPercentage());
        statement.bindString(5, entity.getWinners());
        statement.bindString(6, entity.getLosers());
        statement.bindString(7, entity.getSnapshotDate());
        statement.bindLong(8, entity.getCapturedAt());
      }
    };
    this.__preparedStmtOfDeleteOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM portfolio_snapshots WHERE capturedAt < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final PortfolioSnapshot snapshot,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfPortfolioSnapshot.insertAndReturnId(snapshot);
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
  public Object getAll(final Continuation<? super List<PortfolioSnapshot>> $completion) {
    final String _sql = "SELECT * FROM portfolio_snapshots ORDER BY capturedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PortfolioSnapshot>>() {
      @Override
      @NonNull
      public List<PortfolioSnapshot> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTotalValue = CursorUtil.getColumnIndexOrThrow(_cursor, "totalValue");
          final int _cursorIndexOfTotalPnl = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPnl");
          final int _cursorIndexOfPnlPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "pnlPercentage");
          final int _cursorIndexOfWinners = CursorUtil.getColumnIndexOrThrow(_cursor, "winners");
          final int _cursorIndexOfLosers = CursorUtil.getColumnIndexOrThrow(_cursor, "losers");
          final int _cursorIndexOfSnapshotDate = CursorUtil.getColumnIndexOrThrow(_cursor, "snapshotDate");
          final int _cursorIndexOfCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "capturedAt");
          final List<PortfolioSnapshot> _result = new ArrayList<PortfolioSnapshot>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PortfolioSnapshot _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpTotalValue;
            _tmpTotalValue = _cursor.getDouble(_cursorIndexOfTotalValue);
            final double _tmpTotalPnl;
            _tmpTotalPnl = _cursor.getDouble(_cursorIndexOfTotalPnl);
            final double _tmpPnlPercentage;
            _tmpPnlPercentage = _cursor.getDouble(_cursorIndexOfPnlPercentage);
            final String _tmpWinners;
            _tmpWinners = _cursor.getString(_cursorIndexOfWinners);
            final String _tmpLosers;
            _tmpLosers = _cursor.getString(_cursorIndexOfLosers);
            final String _tmpSnapshotDate;
            _tmpSnapshotDate = _cursor.getString(_cursorIndexOfSnapshotDate);
            final long _tmpCapturedAt;
            _tmpCapturedAt = _cursor.getLong(_cursorIndexOfCapturedAt);
            _item = new PortfolioSnapshot(_tmpId,_tmpTotalValue,_tmpTotalPnl,_tmpPnlPercentage,_tmpWinners,_tmpLosers,_tmpSnapshotDate,_tmpCapturedAt);
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
  public Object getByDate(final String date,
      final Continuation<? super PortfolioSnapshot> $completion) {
    final String _sql = "SELECT * FROM portfolio_snapshots WHERE snapshotDate = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, date);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PortfolioSnapshot>() {
      @Override
      @Nullable
      public PortfolioSnapshot call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTotalValue = CursorUtil.getColumnIndexOrThrow(_cursor, "totalValue");
          final int _cursorIndexOfTotalPnl = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPnl");
          final int _cursorIndexOfPnlPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "pnlPercentage");
          final int _cursorIndexOfWinners = CursorUtil.getColumnIndexOrThrow(_cursor, "winners");
          final int _cursorIndexOfLosers = CursorUtil.getColumnIndexOrThrow(_cursor, "losers");
          final int _cursorIndexOfSnapshotDate = CursorUtil.getColumnIndexOrThrow(_cursor, "snapshotDate");
          final int _cursorIndexOfCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "capturedAt");
          final PortfolioSnapshot _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpTotalValue;
            _tmpTotalValue = _cursor.getDouble(_cursorIndexOfTotalValue);
            final double _tmpTotalPnl;
            _tmpTotalPnl = _cursor.getDouble(_cursorIndexOfTotalPnl);
            final double _tmpPnlPercentage;
            _tmpPnlPercentage = _cursor.getDouble(_cursorIndexOfPnlPercentage);
            final String _tmpWinners;
            _tmpWinners = _cursor.getString(_cursorIndexOfWinners);
            final String _tmpLosers;
            _tmpLosers = _cursor.getString(_cursorIndexOfLosers);
            final String _tmpSnapshotDate;
            _tmpSnapshotDate = _cursor.getString(_cursorIndexOfSnapshotDate);
            final long _tmpCapturedAt;
            _tmpCapturedAt = _cursor.getLong(_cursorIndexOfCapturedAt);
            _result = new PortfolioSnapshot(_tmpId,_tmpTotalValue,_tmpTotalPnl,_tmpPnlPercentage,_tmpWinners,_tmpLosers,_tmpSnapshotDate,_tmpCapturedAt);
          } else {
            _result = null;
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
  public Object getLatest(final Continuation<? super PortfolioSnapshot> $completion) {
    final String _sql = "SELECT * FROM portfolio_snapshots ORDER BY capturedAt DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PortfolioSnapshot>() {
      @Override
      @Nullable
      public PortfolioSnapshot call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTotalValue = CursorUtil.getColumnIndexOrThrow(_cursor, "totalValue");
          final int _cursorIndexOfTotalPnl = CursorUtil.getColumnIndexOrThrow(_cursor, "totalPnl");
          final int _cursorIndexOfPnlPercentage = CursorUtil.getColumnIndexOrThrow(_cursor, "pnlPercentage");
          final int _cursorIndexOfWinners = CursorUtil.getColumnIndexOrThrow(_cursor, "winners");
          final int _cursorIndexOfLosers = CursorUtil.getColumnIndexOrThrow(_cursor, "losers");
          final int _cursorIndexOfSnapshotDate = CursorUtil.getColumnIndexOrThrow(_cursor, "snapshotDate");
          final int _cursorIndexOfCapturedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "capturedAt");
          final PortfolioSnapshot _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final double _tmpTotalValue;
            _tmpTotalValue = _cursor.getDouble(_cursorIndexOfTotalValue);
            final double _tmpTotalPnl;
            _tmpTotalPnl = _cursor.getDouble(_cursorIndexOfTotalPnl);
            final double _tmpPnlPercentage;
            _tmpPnlPercentage = _cursor.getDouble(_cursorIndexOfPnlPercentage);
            final String _tmpWinners;
            _tmpWinners = _cursor.getString(_cursorIndexOfWinners);
            final String _tmpLosers;
            _tmpLosers = _cursor.getString(_cursorIndexOfLosers);
            final String _tmpSnapshotDate;
            _tmpSnapshotDate = _cursor.getString(_cursorIndexOfSnapshotDate);
            final long _tmpCapturedAt;
            _tmpCapturedAt = _cursor.getLong(_cursorIndexOfCapturedAt);
            _result = new PortfolioSnapshot(_tmpId,_tmpTotalValue,_tmpTotalPnl,_tmpPnlPercentage,_tmpWinners,_tmpLosers,_tmpSnapshotDate,_tmpCapturedAt);
          } else {
            _result = null;
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
