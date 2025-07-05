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
import com.example.financeapp.adapters.MonthlySummaryAdapter // This adapter will be created later
import com.example.financeapp.helpers.DatabaseHelper
import com.example.financeapp.models.MonthlySummary // This data class will be created later
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthlyFragment : Fragment() {

    private lateinit var recyclerViewMonthlySummary: RecyclerView
    private lateinit var textMonthName: TextView
    private lateinit var monthlySummaryAdapter: MonthlySummaryAdapter
    private var monthlySummaries: ArrayList<MonthlySummary> = ArrayList()
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monthly, container, false)
        dbHelper = DatabaseHelper(requireContext())

        textMonthName = view.findViewById(R.id.textMonthName)
        recyclerViewMonthlySummary = view.findViewById(R.id.recyclerViewMonthlySummary)
        recyclerViewMonthlySummary.layoutManager = LinearLayoutManager(context)

        // Initialize adapter (MonthlySummaryAdapter will be created in a later step)
        monthlySummaryAdapter = MonthlySummaryAdapter(monthlySummaries)
        recyclerViewMonthlySummary.adapter = monthlySummaryAdapter

        loadMonthlySummary()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMonthName() // Example: Set to current month
        loadMonthlySummary()
    }

    private fun setMonthName() {
        // Example: Set to current month name, you might want to make this selectable
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        textMonthName.text = monthFormat.format(calendar.time)
    }

    fun loadMonthlySummary() {
        // TODO: Implement actual data loading logic from DatabaseHelper for monthly summaries
        // This is a placeholder. You'll need to modify DatabaseHelper
        // and call the appropriate method here.
        // Example: dbHelper.getMonthlySummaries() which returns List<MonthlySummary>

        val summaries = dbHelper.getMonthlySummary() // This method needs to be created in DatabaseHelper
        monthlySummaries.clear()
        monthlySummaries.addAll(summaries)
        monthlySummaryAdapter.notifyDataSetChanged()

        if (monthlySummaries.isEmpty()) {
            // Optionally, show a message if there is no summary data
        }
    }

    // Call this method from MainActivity after a new transaction is added or deleted
    fun refreshSummary() {
        loadMonthlySummary()
    }
}
