package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.model.Budget

data class BudgetStatus(
    val budget: Budget,
    val spent: Double,
    val remaining: Double,
    val isOverBudget: Boolean
)
