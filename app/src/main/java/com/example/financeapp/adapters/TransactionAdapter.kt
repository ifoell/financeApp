package com.example.financeapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.models.Transaction
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.financeapp.R

class TransactionAdapter(
    private val transactions: ArrayList<Transaction>,
    private val listener: OnDeleteClickListener,
    private val dateFormat: DateTimeFormatter // Assuming this is still needed, though not directly used in formatTransaction
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return transactions[position].id
    }

    interface OnDeleteClickListener {
        fun onDeleteClick(transactionId: Long)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction, listener)
    }

    private fun formatTransaction(transaction: Transaction): String {
        // The dateFormat parameter is not used here, was it intended for the timestamp?
        // Assuming transaction.timestamp is already formatted as desired.
        val formattedDate = transaction.timestamp

        // Format amount with currency (assuming IDR)
        val formattedAmount = String.format(Locale.getDefault(), "Rp %,.0f", transaction.amount)
        val formattedNote = if (transaction.note.isEmpty()) "" else " - ${transaction.note}"

        // Build the final string
        return String.format(
            Locale.getDefault(),
            "%s%s\n      %s",
            formattedAmount,
            formattedNote,
            formattedDate
        )
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTransaction: TextView = itemView.findViewById(R.id.textTransaction)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(transaction: Transaction, listener: OnDeleteClickListener) {
            textTransaction.text = formatTransaction(transaction) // Using the adapter's format method
            btnDelete.setOnClickListener { listener.onDeleteClick(transaction.id) }
        }
    }
}
