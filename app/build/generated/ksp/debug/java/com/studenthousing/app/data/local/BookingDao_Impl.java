package com.studenthousing.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
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
public final class BookingDao_Impl implements BookingDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BookingEntity> __insertionAdapterOfBookingEntity;

  public BookingDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBookingEntity = new EntityInsertionAdapter<BookingEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `bookings` (`id`,`status`,`finalPrice`,`propertyTitle`,`lastSyncedAt`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BookingEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getStatus());
        if (entity.getFinalPrice() == null) {
          statement.bindNull(3);
        } else {
          statement.bindDouble(3, entity.getFinalPrice());
        }
        if (entity.getPropertyTitle() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getPropertyTitle());
        }
        statement.bindLong(5, entity.getLastSyncedAt());
      }
    };
  }

  @Override
  public Object insertAll(final List<BookingEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBookingEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAll(final Continuation<? super List<BookingEntity>> $completion) {
    final String _sql = "SELECT * FROM bookings ORDER BY lastSyncedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BookingEntity>>() {
      @Override
      @NonNull
      public List<BookingEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfFinalPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "finalPrice");
          final int _cursorIndexOfPropertyTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "propertyTitle");
          final int _cursorIndexOfLastSyncedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSyncedAt");
          final List<BookingEntity> _result = new ArrayList<BookingEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BookingEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final Double _tmpFinalPrice;
            if (_cursor.isNull(_cursorIndexOfFinalPrice)) {
              _tmpFinalPrice = null;
            } else {
              _tmpFinalPrice = _cursor.getDouble(_cursorIndexOfFinalPrice);
            }
            final String _tmpPropertyTitle;
            if (_cursor.isNull(_cursorIndexOfPropertyTitle)) {
              _tmpPropertyTitle = null;
            } else {
              _tmpPropertyTitle = _cursor.getString(_cursorIndexOfPropertyTitle);
            }
            final long _tmpLastSyncedAt;
            _tmpLastSyncedAt = _cursor.getLong(_cursorIndexOfLastSyncedAt);
            _item = new BookingEntity(_tmpId,_tmpStatus,_tmpFinalPrice,_tmpPropertyTitle,_tmpLastSyncedAt);
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
