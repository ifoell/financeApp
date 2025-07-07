package com.example.financeapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Import for activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Remove MainActivity import if no longer directly needed for callbacks
import com.example.financeapp.R
import com.example.financeapp.adapters.TransactionAdapter
import com.example.financeapp.helpers.DatabaseHelper
import com.example.financeapp.viewmodels.SharedViewModel // Import SharedViewModel
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
    private val sharedViewModel: SharedViewModel by activityViewModels() // Get Activity-scoped ViewModel

    // Listener interface and its usage are removed.

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

        // Observe transaction changes from SharedViewModel to refresh transactions
        sharedViewModel.transactionChanged.observe(viewLifecycleOwner) { changed ->
            if (changed) {
                loadDailyTransactions()
                // No need to call sharedViewModel.doneNotifyingTransactionChange() here,
                // MainActivity does it, or MonthlyFragment if it also observes.
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDayName()
        // loadDailyTransactions() // Already called in onCreateView and observed
    }

    private fun setDayName() {
        // Example: Set to current day name, you might want to make this selectable
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        textDayName.text = dayFormat.format(calendar.time)
    }

    @SuppressLint("NotifyDataSetChanged")
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
        cursor.use {
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
                // Notify SharedViewModel that a transaction has changed
                sharedViewModel.notifyTransactionChanged()
            }
        }
    }

    // The refreshTransactions() method might no longer be needed if LiveData observation handles updates.
    // Or it can be kept if external direct refresh is still desired for some reason.
    // For now, let's assume LiveData handles it.
    // fun refreshTransactions() {
    //     loadDailyTransactions()
    // }
}
