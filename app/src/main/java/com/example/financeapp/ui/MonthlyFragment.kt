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
import com.example.financeapp.R
import com.example.financeapp.adapters.MonthlySummaryAdapter // This adapter will be created later
import com.example.financeapp.helpers.DatabaseHelper
import com.example.financeapp.models.MonthlySummary // This data class will be created later
import com.example.financeapp.viewmodels.SharedViewModel // Import SharedViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthlyFragment : Fragment() {

    private lateinit var recyclerViewMonthlySummary: RecyclerView
    private lateinit var textMonthName: TextView
    private lateinit var monthlySummaryAdapter: MonthlySummaryAdapter
    private var monthlySummaries: ArrayList<MonthlySummary> = ArrayList()
    private lateinit var dbHelper: DatabaseHelper // Revert to lateinit
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize dbHelper here, context should be available
        dbHelper = DatabaseHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monthly, container, false)

        textMonthName = view.findViewById(R.id.textMonthName)
        recyclerViewMonthlySummary = view.findViewById(R.id.recyclerViewMonthlySummary)
        recyclerViewMonthlySummary.layoutManager = LinearLayoutManager(context)

        monthlySummaryAdapter = MonthlySummaryAdapter(monthlySummaries)
        recyclerViewMonthlySummary.adapter = monthlySummaryAdapter

        // dbHelper is initialized in onCreate.
        // loadMonthlySummary() will be called in onViewCreated.

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setMonthName()

        // Initial load of summary
        loadMonthlySummaryData()

        // Observe transaction changes from SharedViewModel
        sharedViewModel.transactionChanged.observe(viewLifecycleOwner) { changed ->
            if (changed && isAdded) { // Ensure fragment is still added
                loadMonthlySummaryData()
            }
        }
    }

    private fun setMonthName() {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        textMonthName.text = monthFormat.format(calendar.time)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMonthlySummaryData() {
        // dbHelper is guaranteed to be initialized here if onCreate was called.
        // If not, the app has a more fundamental lifecycle issue or incorrect state.
        val summaries = dbHelper.getMonthlySummary() // This is the critical call (Line 68 in original trace)
        monthlySummaries.clear()
        monthlySummaries.addAll(summaries)
        if (::monthlySummaryAdapter.isInitialized) {
            monthlySummaryAdapter.notifyDataSetChanged()
        }

        if (monthlySummaries.isEmpty()) {
            // Optionally, show a message
        }
    }

    // The old refreshSummary() is no longer needed as ViewModel observation drives updates.
    // fun refreshSummary() {
    //     if (isAdded && ::dbHelper.isInitialized) {
    //         loadMonthlySummary()
    //     }
    // }
}
