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
    private final String note;

    public Transaction(long id, double amount, String note) {
        this.id = id;
        this.amount = amount;
        this.note = note;
    }

    public double getAmount() {
        return amount;
    }

    public String getNote() {
        return note;
    }

    @Override
    public @NotNull String toString() {
        return "Rp. " + amount + " - " + note;
    }

}