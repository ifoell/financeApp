package com.example.financeapp.models

data class Transaction(
    var id: Long,
    val amount: Double,
    var note: String,
    val timestamp: String
) {
    override fun toString(): String {
        val noteSuffix = if (note.isEmpty()) "" else " - $note"
        return "Rp. $amount$noteSuffix \n       $timestamp"
    }
}
