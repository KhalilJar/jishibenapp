package com.example.bookkeeping.data.model

data class SummaryTotals(
    val income: Double = 0.0,
    val expense: Double = 0.0
) {
    val net: Double
        get() = income - expense
}
