package com.example.financeapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.models.MonthlySummary
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class MonthlySummaryAdapter(
    private val monthlySummaries: List<MonthlySummary>
) : RecyclerView.Adapter<MonthlySummaryAdapter.MonthlySummaryViewHolder>() {

    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
    private val inputMonthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val outputMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlySummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_summary, parent, false) // Ensure this layout exists
        return MonthlySummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthlySummaryViewHolder, position: Int) {
        val summary = monthlySummaries[position]
        holder.bind(summary)
    }

    override fun getItemCount(): Int {
        return monthlySummaries.size
    }

    inner class MonthlySummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMonthYear: TextView = itemView.findViewById(R.id.textMonthYear)
        private val textTotalAmount: TextView = itemView.findViewById(R.id.textTotalAmount)

        fun bind(summary: MonthlySummary) {
            try {
                val date = inputMonthYearFormat.parse(summary.monthYear)
                textMonthYear.text = if (date != null) outputMonthYearFormat.format(date) else summary.monthYear
            } catch (e: ParseException) {
                textMonthYear.text = summary.monthYear // Fallback to raw string if parsing fails
            }
            textTotalAmount.text = currencyFormatter.format(summary.totalAmount)
        }
    }
}
