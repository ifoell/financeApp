package com.example.financeapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Companion object for static members
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "FinanceManager.db"

        const val TABLE_TRANSACTIONS = "transactions" // Made public for potential external use or kept internal

        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_NOTE = "note"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableSQL = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_NOTE TEXT,
                $COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        db?.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < newVersion) {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
            onCreate(db)
        }
    }

    fun addNewRecord(amount: Double, note: String): Long {
        // Using 'use' block for ensuring db is closed automatically
        return writableDatabase.use { db ->
            val values = ContentValues().apply {
                put(COLUMN_AMOUNT, amount)
                put(COLUMN_NOTE, note)
            }
            db.insert(TABLE_TRANSACTIONS, null, values)
        }
        // Note: db.close() is handled by 'use'. Original Java code closed db, which is good.
        // insert() returns the row ID of the newly inserted row, or -1 if an error occurred.
    }

    fun deleteRecord(id: Long): Boolean {
        return writableDatabase.use { db ->
            val selection = "$COLUMN_ID = ?"
            val selectionArgs = arrayOf(id.toString())
            val rowsAffected = db.delete(TABLE_TRANSACTIONS, selection, selectionArgs)
            rowsAffected > 0
        }
    }

    fun getAllTransactions(): Cursor? {
        // The caller of this method is responsible for closing the cursor.
        val db = this.readableDatabase
        return db.query(
            TABLE_TRANSACTIONS,
            null, // columns - null selects all columns
            null, // selection
            null, // selectionArgs
            null, // groupBy
            null, // having
            "$COLUMN_TIMESTAMP DESC" // orderBy
        )
    }

    fun getTotalExpenses(): Double {
        // Using 'use' block for cursor ensuring it's closed
        readableDatabase.rawQuery("SELECT SUM($COLUMN_AMOUNT) FROM $TABLE_TRANSACTIONS", null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0)
            }
        }
        return 0.0
    }
}
