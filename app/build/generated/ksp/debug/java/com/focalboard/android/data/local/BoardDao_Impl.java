package com.focalboard.android.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class BoardDao_Impl implements BoardDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BoardEntity> __insertionAdapterOfBoardEntity;

  private final EntityDeletionOrUpdateAdapter<BoardEntity> __deletionAdapterOfBoardEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteBoardsByServer;

  public BoardDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBoardEntity = new EntityInsertionAdapter<BoardEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `boards` (`id`,`name`,`description`,`workspaceId`,`serverUrl`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BoardEntity value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
        if (value.getName() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getName());
        }
        if (value.getDescription() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getDescription());
        }
        if (value.getWorkspaceId() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getWorkspaceId());
        }
        if (value.getServerUrl() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.getServerUrl());
        }
        stmt.bindLong(6, value.getCreatedAt());
        stmt.bindLong(7, value.getUpdatedAt());
      }
    };
    this.__deletionAdapterOfBoardEntity = new EntityDeletionOrUpdateAdapter<BoardEntity>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `boards` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BoardEntity value) {
        if (value.getId() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getId());
        }
      }
    };
    this.__preparedStmtOfDeleteBoardsByServer = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM boards WHERE serverUrl = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBoard(final BoardEntity board,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBoardEntity.insert(board);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object insertBoards(final List<BoardEntity> boards,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfBoardEntity.insert(boards);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteBoard(final BoardEntity board,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBoardEntity.handle(board);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteBoardsByServer(final String serverUrl,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteBoardsByServer.acquire();
        int _argIndex = 1;
        if (serverUrl == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, serverUrl);
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfDeleteBoardsByServer.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<BoardEntity>> getBoardsByServer(final String serverUrl) {
    final String _sql = "SELECT * FROM boards WHERE serverUrl = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (serverUrl == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, serverUrl);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[]{"boards"}, new Callable<List<BoardEntity>>() {
      @Override
      public List<BoardEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfWorkspaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "workspaceId");
          final int _cursorIndexOfServerUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "serverUrl");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<BoardEntity> _result = new ArrayList<BoardEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final BoardEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpWorkspaceId;
            if (_cursor.isNull(_cursorIndexOfWorkspaceId)) {
              _tmpWorkspaceId = null;
            } else {
              _tmpWorkspaceId = _cursor.getString(_cursorIndexOfWorkspaceId);
            }
            final String _tmpServerUrl;
            if (_cursor.isNull(_cursorIndexOfServerUrl)) {
              _tmpServerUrl = null;
            } else {
              _tmpServerUrl = _cursor.getString(_cursorIndexOfServerUrl);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new BoardEntity(_tmpId,_tmpName,_tmpDescription,_tmpWorkspaceId,_tmpServerUrl,_tmpCreatedAt,_tmpUpdatedAt);
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
  public Object getBoardsByServerSync(final String serverUrl,
      final Continuation<? super List<BoardEntity>> continuation) {
    final String _sql = "SELECT * FROM boards WHERE serverUrl = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (serverUrl == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, serverUrl);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BoardEntity>>() {
      @Override
      public List<BoardEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfWorkspaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "workspaceId");
          final int _cursorIndexOfServerUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "serverUrl");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<BoardEntity> _result = new ArrayList<BoardEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final BoardEntity _item;
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpWorkspaceId;
            if (_cursor.isNull(_cursorIndexOfWorkspaceId)) {
              _tmpWorkspaceId = null;
            } else {
              _tmpWorkspaceId = _cursor.getString(_cursorIndexOfWorkspaceId);
            }
            final String _tmpServerUrl;
            if (_cursor.isNull(_cursorIndexOfServerUrl)) {
              _tmpServerUrl = null;
            } else {
              _tmpServerUrl = _cursor.getString(_cursorIndexOfServerUrl);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new BoardEntity(_tmpId,_tmpName,_tmpDescription,_tmpWorkspaceId,_tmpServerUrl,_tmpCreatedAt,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getBoardById(final String boardId,
      final Continuation<? super BoardEntity> continuation) {
    final String _sql = "SELECT * FROM boards WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (boardId == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, boardId);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<BoardEntity>() {
      @Override
      public BoardEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfWorkspaceId = CursorUtil.getColumnIndexOrThrow(_cursor, "workspaceId");
          final int _cursorIndexOfServerUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "serverUrl");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final BoardEntity _result;
          if(_cursor.moveToFirst()) {
            final String _tmpId;
            if (_cursor.isNull(_cursorIndexOfId)) {
              _tmpId = null;
            } else {
              _tmpId = _cursor.getString(_cursorIndexOfId);
            }
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpWorkspaceId;
            if (_cursor.isNull(_cursorIndexOfWorkspaceId)) {
              _tmpWorkspaceId = null;
            } else {
              _tmpWorkspaceId = _cursor.getString(_cursorIndexOfWorkspaceId);
            }
            final String _tmpServerUrl;
            if (_cursor.isNull(_cursorIndexOfServerUrl)) {
              _tmpServerUrl = null;
            } else {
              _tmpServerUrl = _cursor.getString(_cursorIndexOfServerUrl);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _result = new BoardEntity(_tmpId,_tmpName,_tmpDescription,_tmpWorkspaceId,_tmpServerUrl,_tmpCreatedAt,_tmpUpdatedAt);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @Override
  public Object getBoardCount(final Continuation<? super Integer> continuation) {
    final String _sql = "SELECT COUNT(*) FROM boards";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if(_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
