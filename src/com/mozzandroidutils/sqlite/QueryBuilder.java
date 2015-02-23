package com.mozzandroidutils.sqlite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mozzandroidutils.file.ObjectByte;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class QueryBuilder {
	private final String DEBUG_TAG = this.getClass().getSimpleName();

	QueryBuilder(String tableName, SQLiteDatabase sqliteDatabase,
			Map<String, ColumnType> columnTypes) {
		mColumns = columnTypes;
		mTableName = tableName;
		mDatabase = sqliteDatabase;

		mWhereBuilder = new StringBuilder();
	}

	void closeBuild() {
		mCanBuildQuery = false;
	}

	void changeTableName(String newTableName) {
		mTableName = newTableName;
	}

	void buildSelect(String select) {
		if (mCanBuildQuery) {
			mSelect = select;
			mIsQueryCosumed = false;
		}
	}

	void buildWhere(String where) {
		if (mCanBuildQuery) {
			mWhereBuilder.append(where);
			mIsQueryCosumed = false;
		}
	}

	void buildOrderBy(String orderBy) {
		if (mCanBuildQuery) {
			mOrderBy = orderBy;
			mIsQueryCosumed = false;
		}
	}

	void buildGroupBy(String groupBy) {
		if (mCanBuildQuery) {
			mGroupBy = groupBy;
			mIsQueryCosumed = false;
		}
	}

	private void clear() {
		mSelect = null;
		mGroupBy = null;
		mOrderBy = null;
		mWhereBuilder.setLength(0);

	}

	public List<Model> get() throws IllegalAccessException {
		Cursor cursor = build();
		List<Model> result = new ArrayList<Model>();
		if (cursor == null) {
			throw new IllegalAccessException(
					"result has been cosumed, please do a query again.");
		}

		if (cursor.moveToFirst()) {
			while (cursor.moveToNext()) {
				Model model = new Model();
				int length = cursor.getColumnCount();

				for (int i = 0; i < length; i++) {
					String columnName = cursor.getColumnName(i);

					ColumnType type = mColumns.get(columnName);
					switch (type) {
					case TYPE_INTEGER:
						model.set(columnName, cursor.getInt(i));
						break;
					case TYPE_BLOB:
						model.set(columnName,
								ObjectByte.toObject(cursor.getBlob(i)));
						break;
					case TYPE_TEXT:
						model.set(columnName, cursor.getString(i));
						break;
					case TYPE_REAL:
						model.set(columnName, cursor.getDouble(i));
						break;
					}

				}
				result.add(model);
			}
		}

		close(cursor);

		return result;
	}

	public Cursor cursor() throws IllegalAccessException {
		Cursor cursor = build();

		if (cursor == null)
			throw new IllegalAccessException(
					"result has been cosumed, please do a query again.");

		return cursor;
	}

	public Model first() throws IllegalAccessException {
		Cursor cursor = build();

		if (cursor == null) {
			throw new IllegalAccessException(
					"result has been cosumed, please do a query again.");
		}

		if (cursor.moveToFirst()) {
			Model model = new Model();
			int length = cursor.getColumnCount();

			for (int i = 0; i < length; i++) {
				String columnName = cursor.getColumnName(i);

				ColumnType type = mColumns.get(columnName);
				switch (type) {
				case TYPE_INTEGER:
					model.set(columnName, cursor.getInt(i));
					break;
				case TYPE_BLOB:
					model.set(columnName,
							ObjectByte.toObject(cursor.getBlob(i)));
					break;
				case TYPE_TEXT:
					model.set(columnName, cursor.getString(i));
					break;
				case TYPE_REAL:
					model.set(columnName, cursor.getDouble(i));
					break;
				}
			}
			close(cursor);
			return model;
		} else {
			close(cursor);
			return null;
		}

	}

	private Cursor build() {
		if (!mIsQueryCosumed) {
			if (mQueryBuilder == null) {
				mQueryBuilder = new StringBuilder();
			} else {
				mQueryBuilder.setLength(0);
			}

			if (mSelect != null) {
				mQueryBuilder.append("SELECT " + mSelect + " FROM "
						+ mTableName);
			} else {
				mQueryBuilder.append("SELECT * " + " FROM " + mTableName);
			}

			if (mWhereBuilder.length() > 0) {
				mQueryBuilder.append(" WHERE " + mWhereBuilder.toString());
			}

			if (mGroupBy != null)
				mQueryBuilder.append(mGroupBy);

			if (mOrderBy != null)
				mQueryBuilder.append(mOrderBy);

			if (mDatabase != null && mDatabase.isOpen()) {
				debug(mQueryBuilder.toString());
				clear();
				mIsQueryCosumed = true;
				mCanBuildQuery = true;
				return mDatabase.rawQuery(mQueryBuilder.toString(), null);
			}

			clear();
			mIsQueryCosumed = true;
			mCanBuildQuery = true;
		}
		return null;
	}

	private void close(Cursor cursor) {
		if (cursor != null)
			cursor.close();
		cursor = null;
	}

	private void debug(String debugInfo) {
		if (mOpenDebug)
			Log.d(DEBUG_TAG, debugInfo);
	}

	void setDebugMode(boolean openDebug) {
		mOpenDebug = openDebug;
	}

	private Map<String, ColumnType> mColumns;

	private String mSelect;
	private String mGroupBy;
	private String mOrderBy;
	private String mTableName;
	private StringBuilder mWhereBuilder;
	private StringBuilder mQueryBuilder;
	private SQLiteDatabase mDatabase;

	private boolean mIsQueryCosumed = true;
	private boolean mCanBuildQuery = true;

	private boolean mOpenDebug = false;
}