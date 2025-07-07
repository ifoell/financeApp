package com.example.financeapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _transactionChanged = MutableLiveData<Boolean>()
    val transactionChanged: LiveData<Boolean>
        get() = _transactionChanged

    fun notifyTransactionChanged() {
        _transactionChanged.value = true
    }

    fun doneNotifyingTransactionChange() {
        _transactionChanged.value = false // Reset after observation
    }
}
