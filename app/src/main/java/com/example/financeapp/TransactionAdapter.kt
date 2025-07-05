package com.example.financeapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val transactions: ArrayList<Transaction>, // Changed to val, list can still be mutated internally
    private val listener: OnDeleteClickListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    // Functional interface (SAM conversion)
    fun interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position] // Direct indexing
        holder.bind(transaction, listener)
    }

    override fun getItemCount(): Int = transactions.size // Expression body

    // Inner class to access adapter's listener or pass it explicitly
    // Made it an inner class to potentially access adapter members if needed,
    // though direct listener passing to bind method is cleaner.
    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Using private val for views, lateinit if not nullable and initialized in constructor/init
        private val textTransaction: TextView = itemView.findViewById(R.id.textTransaction)
        private val textTimestamp: TextView = itemView.findViewById(R.id.textTimestamp)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(transaction: Transaction, listener: OnDeleteClickListener) {
            textTransaction.text = transaction.toString() // Assumes Transaction.kt has the custom toString()

            // Format and display timestamp
            val rawTimestamp = transaction.timestamp // Accessing Kotlin property
            if (!rawTimestamp.isNullOrEmpty()) { // Kotlin utility function
                try {
                    // Input format from SQLite CURRENT_TIMESTAMP
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    // Output format
                    val outputFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
                    val date = inputFormat.parse(rawTimestamp)
                    if (date != null) {
                        textTimestamp.text = outputFormat.format(date)
                    } else {
                        textTimestamp.text = "Invalid date" // Fallback
                    }
                } catch (e: ParseException) {
                    textTimestamp.text = rawTimestamp // Fallback to raw timestamp if parsing fails
                    // Log error: e.printStackTrace() // Consider proper logging
                }
            } else {
                textTimestamp.text = "" // Clear if no timestamp
            }

            btnDelete.setOnClickListener {
                // Safe call for adapterPosition, though click listeners on items
                // being removed can sometimes be tricky. Using getAdapterPosition directly.
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(adapterPosition)
                }
            }
        }
    }
}
