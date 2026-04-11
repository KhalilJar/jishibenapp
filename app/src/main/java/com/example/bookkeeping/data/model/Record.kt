package com.example.bookkeeping.data.model

data class Record(
    val id: Long = 0,
    val type: RecordType,
    val amount: Double,
    val tag: String,
    val note: String?,
    val timestamp: Long,
    val accountId: Long = 1L
)
