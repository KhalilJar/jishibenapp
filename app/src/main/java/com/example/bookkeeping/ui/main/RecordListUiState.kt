package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecurringRule

data class RecordListUiState(
    val records: List<Record>,
    val filters: RecordFilterUiState,
    val accounts: List<Account>,
    val availableTags: List<String>,
    val budgetStatuses: List<BudgetStatus>,
    val dueRecurringRules: List<RecurringRule>
)
