package com.example.financeapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	// Database version should increment when schema changes
	private static final int DATABASE_VERSION = 1;

	// Database name
	private static final String DATABASE_NAME = "FinanceManager.db";

	// Table name
	private static final String TABLE_TRANSACTIONS = "transactions";

	// Column names
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_AMOUNT = "amount";
	public static final String COLUMN_NOTE = "note";
	private static final String COLUMN_TIMESTAMP = "timestamp";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
				+ COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ COLUMN_AMOUNT + " REAL NOT NULL,"
				+ COLUMN_NOTE + " TEXT,"
				+ COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Better migration handling for future versions
		if (oldVersion < newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
			onCreate(db);
		}
	}

	public long addNewRecord(double amount, String note) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_AMOUNT, amount);
		values.put(COLUMN_NOTE, note);

		// Insert and get the ID of the new record
		long id = db.insert(TABLE_TRANSACTIONS, null, values);
		db.close();
		return id;
	}

	public boolean deleteRecord(long id) {
		SQLiteDatabase db = this.getWritableDatabase();
		int rowsAffected = db.delete(TABLE_TRANSACTIONS,
				COLUMN_ID + " = ?",
				new String[]{String.valueOf(id)});
		db.close();
		return rowsAffected > 0;
	}

	// Additional useful methods
	public Cursor getAllTransactions() {
		SQLiteDatabase db = this.getReadableDatabase();
		return db.query(TABLE_TRANSACTIONS,
				null, // columns - null selects all columns
				null, // selection
				null, // selectionArgs
				null, // groupBy
				null, // having
				COLUMN_TIMESTAMP + " DESC" // orderBy
		);
	}

	public double getTotalExpenses() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS, null);
		if (cursor.moveToFirst()) {
			return cursor.getDouble(0);
		}
		cursor.close();
		return 0;
	}
}