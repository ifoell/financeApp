package com.example.financeapp

import android.annotation.SuppressLint
import android.content.res.Resources
import android.database.Cursor
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.financeapp.adapters.ViewPagerAdapter
import com.example.financeapp.helpers.DatabaseHelper
import com.example.financeapp.models.Transaction // Keep if needed for addTransaction, or remove if handled by fragments
import com.example.financeapp.ui.DailyFragment // Import DailyFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.NumberFormat
import java.util.Locale
// Removed unused imports like LocalDateTime, DateTimeFormatter if addTransaction changes

class MainActivity : AppCompatActivity(), DailyFragment.OnTransactionDeletedListener {

    private lateinit var editAmount: EditText
    private lateinit var editNote: EditText
    private lateinit var btnSave: MaterialButton
    private lateinit var textTotal: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var dbHelper: DatabaseHelper
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private var totalExpense: Double = 0.0 // This will now be loaded differently

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        // Initialize views
        editAmount = findViewById(R.id.editAmount)
        editNote = findViewById(R.id.editNote)
        btnSave = findViewById(R.id.btnSave)
        textTotal = findViewById(R.id.textTotal)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        // Setup ViewPager and TabLayout
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Daily"
                1 -> "Monthly"
                else -> null
            }
        }.attach()

        // Set listener for DailyFragment's delete operations
        viewPagerAdapter.getDailyFragment().setOnTransactionDeletedListener(this)


        // Save button click listener
        btnSave.setOnClickListener { addTransaction() }

        // Load initial total expense
        loadInitialTotalExpense()
    }

    // This method is called when a transaction is deleted in DailyFragment
    override fun onTransactionDeleted() {
        // Reload total expense and update fragments
        loadInitialTotalExpense() // Recalculates total from DB
        viewPagerAdapter.getDailyFragment().refreshTransactions() // Refresh daily view
        viewPagerAdapter.getMonthlyFragment().refreshSummary() // Refresh monthly view
    }


    // Call this method from DailyFragment when a transaction is deleted
    fun updateTotalAfterDeletion(amountDeleted: Double) {
        totalExpense -= amountDeleted
        updateTotalText()
        // Also refresh the monthly fragment as its summary might change
        viewPagerAdapter.getMonthlyFragment().refreshSummary()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun loadInitialTotalExpense() {
        val cursor: Cursor? = dbHelper.getAllTransactions()
        totalExpense = 0.0 // Reset total expense
        cursor?.use {
            while (it.moveToNext()) {
                val amount = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT))
                totalExpense += amount
            }
        }
        updateTotalText()
    }

    private fun addTransaction() {
        val amountStr = editAmount.text.toString().trim()
        val note = editNote.text.toString().trim()

        if (amountStr.isEmpty()) {
            editAmount.error = "Masukkan jumlah pengeluaran"
            editAmount.requestFocus()
            return
        }

        val amount: Double = try {
            amountStr.toDouble()
        } catch (e: NumberFormatException) {
            editAmount.error = "Format jumlah tidak valid"
            editAmount.requestFocus()
            // Consider logging e.message instead of println for production apps
            return
        }

        if (amount <= 0) {
            editAmount.error = "Jumlah harus lebih dari 0"
            editAmount.requestFocus()
            return
        }

        val newId = dbHelper.addNewRecord(amount, note)
        if (newId == -1L) {
            Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show()
            return
        }

        // Update total expense
        totalExpense += amount
        updateTotalText()

        // Clear input fields
        editAmount.text.clear()
        editNote.text.clear()
        editAmount.requestFocus()

        val formattedAmount = currencyFormatter.format(amount)
        Toast.makeText(this, "Transaksi berhasil: $formattedAmount", Toast.LENGTH_SHORT).show()

        // Refresh the fragments
        viewPagerAdapter.getDailyFragment().refreshTransactions()
        viewPagerAdapter.getMonthlyFragment().refreshSummary()
    }

    @SuppressLint("StringFormatMatches")
    private fun updateTotalText() {
        val res: Resources = resources
        val formattedTotal = currencyFormatter.format(totalExpense)
        // Ensure R.string.total_update exists and is "Total Pengeluaran: %s" or similar
        textTotal.text = getString(R.string.total_update, formattedTotal)
    }

    // The onDeleteClick from TransactionAdapter.OnDeleteClickListener is removed
    // as this is now handled by DailyFragment.
    // MainActivity now implements DailyFragment.OnTransactionDeletedListener
}
