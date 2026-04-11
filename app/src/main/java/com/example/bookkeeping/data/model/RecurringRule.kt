package com.example.bookkeeping.data.model

data class RecurringRule(
    val id: Long = 0,
    val title: String,
    val type: RecordType,
    val amount: Double,
    val tag: String,
    val note: String? = null,
    val accountId: Long,
    val frequency: RecurringFrequency,
    val intervalCount: Int = 1,
    val startDate: String,
    val nextDueDate: String,
    val isActive: Boolean = true
)
