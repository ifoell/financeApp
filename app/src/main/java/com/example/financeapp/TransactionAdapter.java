package com.example.financeapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private ArrayList<Transaction> transactions;
    private OnDeleteClickListener listener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public TransactionAdapter(ArrayList<Transaction> transactions, OnDeleteClickListener listener) {
        this.transactions = transactions;
        this.listener = listener;
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
        holder.textTransaction.setText(transaction.toString());
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(position));
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