package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.model.RecordType

data class RecordFilterUiState(
    val keyword: String = "",
    val type: RecordType? = null,
    val tag: String? = null,
    val accountId: Long? = null,
    val startDateMillis: Long? = null,
    val endDateMillis: Long? = null,
    val minAmountInput: String = "",
    val maxAmountInput: String = ""
)
