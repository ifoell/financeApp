package com.example.financeapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final ArrayList<Transaction> transactions;
    private final OnDeleteClickListener listener;
    private final DateTimeFormatter dateFormat;

    @Override
    public long getItemId(int position) {
        return transactions.get(position).getId();
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(long transactionId);
    }

    public TransactionAdapter(ArrayList<Transaction> transactions, OnDeleteClickListener listener, DateTimeFormatter dateFormat) {
        this.transactions = transactions;
        this.listener = listener;
        this.dateFormat = dateFormat;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        String formattedTransaction = formatTransaction(transaction);
        holder.textTransaction.setText(formattedTransaction);

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(transaction.getId()));
    }

    private String formatTransaction(Transaction transaction) {
        String formattedDate = transaction.getTimestamp();

        // Format amount with currency (assuming IDR)
        String formattedAmount = String.format(Locale.getDefault(), "Rp %,.0f", transaction.getAmount());

        String formattedNote = transaction.getNote().isEmpty() ? "" : " - "+transaction.getNote();

        // Build the final string
        return String.format(Locale.getDefault(),
                "%s%s\n      %s",
                formattedAmount,
                formattedNote,
                formattedDate);
    }


    @Override
    public int getItemCount() {
        return transactions.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textTransaction;
        ImageButton btnDelete;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textTransaction = itemView.findViewById(R.id.textTransaction);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}