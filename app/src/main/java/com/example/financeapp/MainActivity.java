package com.example.financeapp;

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	EditText editAmount, editNote;
	ImageButton btnSave;
	TextView textTotal;
	RecyclerView recyclerView;

	ArrayList<Transaction> transactionList;
	TransactionAdapter adapter;
	DatabaseHelper dbHelper;

	double totalExpense = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Inisialisasi view
		editAmount = findViewById(R.id.editAmount);
		editNote = findViewById(R.id.editNote);
		btnSave = findViewById(R.id.btnSave);
		textTotal = findViewById(R.id.textTotal);
		recyclerView = findViewById(R.id.recyclerViewTransactions);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		// Inisialisasi list dan adapter
		transactionList = new ArrayList<>();
		adapter = new TransactionAdapter(transactionList, this::removeTransaction);
		recyclerView.setAdapter(adapter);

		// Tombol simpan
		btnSave.setOnClickListener(v -> addTransaction());
	}

	private void addTransaction() {
		String amountStr = editAmount.getText().toString().trim();
		String note = editNote.getText().toString().trim(); // Added trim() for note too

		// Validate amount input
		if (amountStr.isEmpty()) {
			editAmount.setError("Masukkan jumlah pengeluaran");
			editAmount.requestFocus();
			return;
		}

		// Parse and validate amount
		double amount;
		try {
			amount = Double.parseDouble(amountStr);
			if (amount <= 0) { // Added validation for positive numbers
				editAmount.setError("Jumlah harus lebih dari 0");
				editAmount.requestFocus();
				return;
			}
		} catch (NumberFormatException e) {
			editAmount.setError("Format jumlah tidak valid");
			editAmount.requestFocus();
			return;
		}

		// Add to database
		long newId = dbHelper.addNewRecord(amount, note);
		if (newId == -1) { // Check if insertion was successful
			Toast.makeText(this, "Gagal menyimpan transaksi", Toast.LENGTH_SHORT).show();
			return;
		}

		// Update UI and data
		totalExpense += amount;
		Transaction newTransaction = new Transaction(newId, amount, note);
		newTransaction.setId(newId); // Assuming your Transaction class has setId()
		transactionList.add(newTransaction);
		adapter.notifyItemInserted(transactionList.size() - 1); // Fixed parameter
		updateTotalText();

		// Clear inputs
		editAmount.setText("");
		editNote.setText("");
		editAmount.requestFocus(); // Move focus back to amount field for next entry

		// Show success message with formatted amount
		String formattedAmount = NumberFormat.getCurrencyInstance(new Locale("id", "ID"))
				.format(amount);
		Toast.makeText(this, "Transaksi berhasil: " + formattedAmount,
				Toast.LENGTH_SHORT).show();
	}

	private void removeTransaction(int position) {
		if (dbHelper.deleteRecord(position)){
			double amount = transactionList.get(position).getAmount();
			totalExpense -= amount;
			transactionList.remove(position);
			adapter.notifyItemRemoved(position);
			updateTotalText();
		}
	}

	private void updateTotalText() {
		Resources res = getResources();
		java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
		String formattedTotal = format.format(totalExpense);
		textTotal.setText(String.format(res.getString(R.string.total_update), formattedTotal));
	}
}