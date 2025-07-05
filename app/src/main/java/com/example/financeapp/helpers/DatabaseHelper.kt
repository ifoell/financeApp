package com.example.financeapp.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Database version should increment when schema changes
        private const val DATABASE_VERSION = 1

        // Database name
        private const val DATABASE_NAME = "FinanceManager.db"

        // Table name
        private const val TABLE_TRANSACTIONS = "transactions"

        // Column names
        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_NOTE = "note"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_TABLE = "CREATE TABLE $TABLE_TRANSACTIONS(" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_AMOUNT REAL NOT NULL," +
                "$COLUMN_NOTE TEXT," +
                "$COLUMN_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP)"
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Better migration handling for future versions
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
            onCreate(db)
        }
    }

    fun addNewRecord(amount: Double, note: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_NOTE, note)
        }

        // Insert and get the ID of the new record
        val id = db.insert(TABLE_TRANSACTIONS, null, values)

        println("id neeh$id") // Consider using Android Log class instead of System.out.println
        // db.close() // Closing the database here can cause issues if other operations need it. Managed by SQLiteOpenHelper.
        return id
    }

    fun deleteRecord(id: Long): Boolean {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_TRANSACTIONS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        // db.close() // Managed by SQLiteOpenHelper.
        return rowsDeleted > 0
    }

    // Additional useful methods
    fun getAllTransactions(): Cursor {
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
        // db.close() // Cursor should be closed by the caller, and DB by SQLiteOpenHelper.
    }
}
