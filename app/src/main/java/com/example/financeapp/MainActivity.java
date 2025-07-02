package com.example.financeapp;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	EditText editAmount, editNote;
	MaterialButton btnSave;
	TextView textTotal;
	RecyclerView recyclerView;

	ArrayList<Transaction> transactionList;
	TransactionAdapter adapter;
	DatabaseHelper dbHelper;

	java.text.NumberFormat format = java.text.NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

	double totalExpense = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dbHelper = new DatabaseHelper(this);

		// Inisialisasi view
		editAmount = findViewById(R.id.editAmount);
		editNote = findViewById(R.id.editNote);
		btnSave = findViewById(R.id.btnSave);
		textTotal = findViewById(R.id.textTotal);
		recyclerView = findViewById(R.id.recyclerViewTransactions);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		// Inisialisasi list dan adapter
		transactionList = new ArrayList<>();
		adapter = new TransactionAdapter(
				transactionList,
				this::removeTransaction,
				DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		recyclerView.setAdapter(adapter);

		// Tombol simpan
		btnSave.setOnClickListener(v -> addTransaction());

		// Load existing transactions
		loadTransactions();
	}

	private void loadTransactions() {
		Cursor cursor = dbHelper.getAllTransactions();
		if (cursor != null) {
			while (cursor.moveToNext()) {
				long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
				double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AMOUNT));
				String note = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTE));
				String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIMESTAMP));

				Transaction transaction = new Transaction(id, amount, note, timestamp);
				transactionList.add(transaction);
				totalExpense += amount;
			}
			cursor.close();
			adapter.notifyDataSetChanged(); // Notify adapter after loading all data
			updateTotalText(); // Update the total expense text view
		}
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

		LocalDateTime timestamp = LocalDateTime.now();

		// Update UI and data
		totalExpense += amount;
		Transaction newTransaction = new Transaction(
				newId,
				amount,
				note,
				timestamp.format(
						DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", new Locale("id", "ID"))));
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

	private void removeTransaction(long transactionId) {
		// Find the actual position in the list
		int positionToRemove = -1;
		double amountToRemove = 0;

		for (int i = 0; i < transactionList.size(); i++) {
			if (transactionList.get(i).getId() == transactionId) {
				positionToRemove = i;
				amountToRemove = transactionList.get(i).getAmount();
				break;
			}
		}

		if (positionToRemove == -1) {
			return; // Transaction not found
		}

		if (dbHelper.deleteRecord(transactionId)) {
			transactionList.remove(positionToRemove);
			totalExpense -= amountToRemove;
			adapter.notifyItemRemoved(positionToRemove);
			updateTotalText();
		} else {
			Toast.makeText(this, "Gagal menghapus transaksi", Toast.LENGTH_SHORT).show();
		}
	}

	private void updateTotalText() {
		Resources res = getResources();
		String formattedTotal = format.format(totalExpense);
		textTotal.setText(String.format(res.getString(R.string.total_update), formattedTotal));
	}
}