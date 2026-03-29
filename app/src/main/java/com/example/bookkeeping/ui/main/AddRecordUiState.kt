package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.TagDataSource
import com.example.bookkeeping.data.model.RecordType

data class AddRecordUiState(
    val type: RecordType = RecordType.EXPENSE,
    val amountInput: String = "",
    val selectedTag: String = TagDataSource.defaultTags.first(),
    val noteInput: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)
