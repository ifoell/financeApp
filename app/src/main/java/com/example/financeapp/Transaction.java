package com.example.financeapp;

import org.jetbrains.annotations.NotNull;

public class Transaction {
    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    private long id;
    private final double amount;
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTimestamp() {
        return timestamp;
    }

    private final String timestamp;

    public Transaction(long id, double amount, String note, String timestamp) {
        this.id = id;
        this.amount = amount;
        this.note = note;
	    this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public @NotNull String toString() {
        note = this.note.isEmpty()? "":" - " + note;
	    return "Rp. " + amount + note + " \n       " + timestamp;
    }

}