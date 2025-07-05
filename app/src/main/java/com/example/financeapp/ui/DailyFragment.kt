package com.example.financeapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.adapters.TransactionAdapter
import com.example.financeapp.helpers.DatabaseHelper
import com.example.financeapp.models.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.time.format.DateTimeFormatter

class DailyFragment : Fragment(), TransactionAdapter.OnDeleteClickListener {

    private lateinit var recyclerViewDailyTransactions: RecyclerView
    private lateinit var textDayName: TextView
    private lateinit var transactionAdapter: TransactionAdapter
    private var dailyTransactions: ArrayList<Transaction> = ArrayList()
    private lateinit var dbHelper: DatabaseHelper

    // Define a listener interface
    interface OnTransactionDeletedListener {
        fun onTransactionDeleted()
    }
    private var listener: OnTransactionDeletedListener? = null

    fun setOnTransactionDeletedListener(listener: OnTransactionDeletedListener) {
        this.listener = listener
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_daily, container, false)
        dbHelper = DatabaseHelper(requireContext())

        textDayName = view.findViewById(R.id.textDayName)
        recyclerViewDailyTransactions = view.findViewById(R.id.recyclerViewDailyTransactions)
        recyclerViewDailyTransactions.layoutManager = LinearLayoutManager(context)

        // Initialize adapter
        // The DateTimeFormatter needs to be appropriate for how timestamps are stored and displayed.
        // Using a common pattern here, adjust if necessary based on actual timestamp format from DB.
        transactionAdapter = TransactionAdapter(
            dailyTransactions,
            this, // OnDeleteClickListener
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss") // Example pattern
        )
        recyclerViewDailyTransactions.adapter = transactionAdapter

        loadDailyTransactions()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDayName()
        loadDailyTransactions()
    }

    private fun setDayName() {
        // Example: Set to current day name, you might want to make this selectable
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        textDayName.text = dayFormat.format(calendar.time)
    }

    fun loadDailyTransactions() {
        // TODO: Implement actual data loading logic from DatabaseHelper for a specific day
        // For now, this is a placeholder. You'll need to modify DatabaseHelper
        // and call the appropriate method here.
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDateStr = dateFormat.format(calendar.time)

        // Assuming DatabaseHelper will have a method like getTransactionsForDate(dateStr)
        // This is a conceptual call, actual implementation will depend on DatabaseHelper changes
        val cursor = dbHelper.getTransactionsForDate(todayDateStr) // This method needs to be created in DatabaseHelper

        dailyTransactions.clear()
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val amount = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT))
                val note = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE))
                val timestamp = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP))
                dailyTransactions.add(Transaction(id, amount, note, timestamp))
            }
        }
        transactionAdapter.notifyDataSetChanged()

        if (dailyTransactions.isEmpty()) {
            // Optionally, show a message if there are no transactions for the day
        }
    }

    override fun onDeleteClick(transactionId: Long) {
        // Find the transaction to get its amount before deleting
        val transactionToRemove = dailyTransactions.find { it.id == transactionId }
        val amountToRemove = transactionToRemove?.amount ?: 0.0

        if (dbHelper.deleteRecord(transactionId)) {
            val position = dailyTransactions.indexOfFirst { it.id == transactionId }
            if (position != -1) {
                dailyTransactions.removeAt(position)
                transactionAdapter.notifyItemRemoved(position)
                // Notify MainActivity to update total
                (activity as? MainActivity)?.updateTotalAfterDeletion(amountToRemove)
                listener?.onTransactionDeleted()

            }
        }
    }

    // Call this method from MainActivity after a new transaction is added
    fun refreshTransactions() {
        loadDailyTransactions()
    }
}
