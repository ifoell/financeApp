package com.example.financeapp

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.ArrayList
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Views - using lateinit as they are initialized in onCreate and non-null after
    private lateinit var editAmount: EditText
    private lateinit var editNote: EditText
    private lateinit var btnSave: ImageButton
    private lateinit var textTotal: TextView
    private lateinit var recyclerView: RecyclerView

    // Using 'by lazy' for dbHelper initialization, ensures it's created when first accessed
    // and only once. Context is available at that point.
    private val dbHelper: DatabaseHelper by lazy { DatabaseHelper(this) }

    private lateinit var transactionList: ArrayList<Transaction>
    private lateinit var adapter: TransactionAdapter

    private var totalExpense: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        editAmount = findViewById(R.id.editAmount)
        editNote = findViewById(R.id.editNote)
        btnSave = findViewById(R.id.btnSave)
        textTotal = findViewById(R.id.textTotal)
        recyclerView = findViewById(R.id.recyclerViewTransactions)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize list and adapter
        transactionList = ArrayList()
        // Pass the lambda for delete click directly
        adapter = TransactionAdapter(transactionList) { position -> removeTransaction(position) }
        recyclerView.adapter = adapter

        // Button click listener
        btnSave.setOnClickListener { addTransaction() }

        // Load existing transactions
        loadTransactions()
    }

    private fun loadTransactions() {
        // Use 'use' extension function for automatic cursor closing
        dbHelper.getAllTransactions()?.use { cursor ->
            // Check if cursor is not empty and can be moved to first
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                    val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT))
                    val note = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE))
                    // Transaction.timestamp is String?, so direct assignment is fine.
                    val timestampString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP))

                    val transaction = Transaction(id, amount, note, timestampString)
                    transactionList.add(transaction)
                    totalExpense += amount
                } while (cursor.moveToNext())
            }
        } // Cursor is closed here automatically

        adapter.notifyDataSetChanged()
        updateTotalText()
    }

    private fun addTransaction() {
        val amountStr = editAmount.text.toString().trim()
        val noteStr = editNote.text.toString().trim()

        if (amountStr.isEmpty()) {
            editAmount.error = "Masukkan jumlah pengeluaran"
            editAmount.requestFocus()
            return
        }

        val amount: Double = try {
            val parsedAmount = amountStr.toDouble()
            if (parsedAmount <= 0) {
                editAmount.error = "Jumlah harus lebih dari 0"
                editAmount.requestFocus()
                return // Exit after setting error
            }
            parsedAmount
        } catch (e: NumberFormatException) {
            editAmount.error = "Format jumlah tidak valid"
            editAmount.requestFocus()
            return // Exit after setting error
        }

        val newId = dbHelper.addNewRecord(amount, noteStr)
        if (newId == -1L) {
            Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            return
        }

        totalExpense += amount
        // Assuming Transaction.timestamp is String? (nullable) as defined in Transaction.kt conversion
        // If it were non-nullable, we'd need a valid string or fetch the actual DB timestamp here.
        val newTransaction = Transaction(newId, amount, noteStr, null)

        transactionList.add(newTransaction)
        adapter.notifyItemInserted(transactionList.size - 1)
        updateTotalText()

        editAmount.text = null
        editNote.text = null
        editAmount.requestFocus()

        val localeId = Locale("id", "ID")
        val formattedAmount = NumberFormat.getCurrencyInstance(localeId).format(amount)
        Toast.makeText(this, "Transaksi berhasil: $formattedAmount", Toast.LENGTH_SHORT).show()
    }

    private fun removeTransaction(position: Int) {
        if (position < 0 || position >= transactionList.size) {
            // Optionally log an error or show a message if this happens
            return
        }

        val transactionToDelete = transactionList[position]
        if (dbHelper.deleteRecord(transactionToDelete.id)) {
            totalExpense -= transactionToDelete.amount
            transactionList.removeAt(position)
            adapter.notifyItemRemoved(position)
            // If you want to ensure subsequent items also update their positions visually immediately:
            // adapter.notifyItemRangeChanged(position, transactionList.size - position)
            updateTotalText()
        } else {
            // Optionally handle the case where deletion from DB fails
            Toast.makeText(this, "Gagal menghapus transaksi dari database", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun updateTotalText() {
        val localeIn = Locale("in", "ID") // "in" is the ISO 639-1 code for Indonesian
        val format = NumberFormat.getCurrencyInstance(localeIn)
        val formattedTotal = format.format(totalExpense)
        textTotal.text = getString(R.string.total_update, formattedTotal)
    }
}
