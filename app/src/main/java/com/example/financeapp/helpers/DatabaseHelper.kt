package com.example.financeapp.helpers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.financeapp.models.MonthlySummary
import java.util.ArrayList
import java.util.HashMap

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2 // Incremented due to potential schema change/clarification
        private const val DATABASE_NAME = "FinanceManager.db"
        private const val TABLE_TRANSACTIONS = "transactions"

        const val COLUMN_ID = "id"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_NOTE = "note"
        // Timestamps are stored as DATETIME, which SQLite stores as TEXT in ISO8601 format (YYYY-MM-DD HH:MM:SS) by default for CURRENT_TIMESTAMP
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
        if (oldVersion < newVersion) {
            // Simple upgrade policy: drop and recreate. For production, implement proper migration.
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
            onCreate(db)
        }
    }

    fun addNewRecord(amount: Double, note: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AMOUNT, amount)
            put(COLUMN_NOTE, note)
            // Timestamp is handled by DEFAULT CURRENT_TIMESTAMP
        }
        val id = db.insert(TABLE_TRANSACTIONS, null, values)
        // db.close() is managed by SQLiteOpenHelper
        return id
    }

    fun deleteRecord(id: Long): Boolean {
        val db = this.writableDatabase
        val rowsDeleted = db.delete(
            TABLE_TRANSACTIONS,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
        return rowsDeleted > 0
    }

    fun getAllTransactions(): Cursor {
        val db = this.readableDatabase
        return db.query(
            TABLE_TRANSACTIONS,
            null, null, null, null, null,
            "$COLUMN_TIMESTAMP DESC"
        )
    }

    /**
     * Fetches transactions for a specific date.
     * Assumes timestamp is stored in a format where date part can be queried using LIKE.
     * SQLite's date functions are powerful here. Example: date(timestamp) = 'YYYY-MM-DD'
     * @param dateStr The date in "YYYY-MM-DD" format.
     */
    fun getTransactionsForDate(dateStr: String): Cursor {
        val db = this.readableDatabase
        // Uses SQLite's date() function to compare only the date part of the timestamp
        val selection = "date($COLUMN_TIMESTAMP) = ?"
        val selectionArgs = arrayOf(dateStr)
        return db.query(
            TABLE_TRANSACTIONS,
            null, // All columns
            selection,
            selectionArgs,
            null, // groupBy
            null, // having
            "$COLUMN_TIMESTAMP DESC" // orderBy
        )
    }

    /**
     * Fetches monthly summary of transactions.
     * Groups by month and year, summing amounts.
     * Uses SQLite's strftime function to extract month and year from timestamp.
     */
    @SuppressLint("Range")
    fun getMonthlySummary(): List<MonthlySummary> {
        val summaryList = ArrayList<MonthlySummary>()
        val db = this.readableDatabase
        // query to group by month and year, and sum amounts
        // strftime('%Y-%m', timestamp) extracts 'YYYY-MM' from the timestamp
        val query = """
            SELECT
                strftime('%Y-%m', $COLUMN_TIMESTAMP) as monthYear,
                SUM($COLUMN_AMOUNT) as totalAmount
            FROM $TABLE_TRANSACTIONS
            GROUP BY monthYear
            ORDER BY monthYear DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor?.use {
            while (it.moveToNext()) {
                val monthYear = it.getString(it.getColumnIndex("monthYear"))
                val totalAmount = it.getDouble(it.getColumnIndex("totalAmount"))
                // Assuming MonthlySummary data class exists with (monthYear: String, totalAmount: Double)
                summaryList.add(MonthlySummary(monthYear, totalAmount))
            }
        }
        return summaryList
    }
}
