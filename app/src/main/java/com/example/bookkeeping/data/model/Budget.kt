package com.example.bookkeeping.data.model

data class Budget(
    val id: Long = 0,
    val yearMonth: String,
    val scope: BudgetScope,
    val tag: String? = null,
    val amountLimit: Double
)
