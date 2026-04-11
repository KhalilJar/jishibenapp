package com.example.bookkeeping.data.backup

import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecurringRule

data class BackupSnapshot(
    val version: Int = 1,
    val exportedAt: Long,
    val records: List<Record>,
    val accounts: List<Account>,
    val budgets: List<Budget>,
    val recurringRules: List<RecurringRule>
)
