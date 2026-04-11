package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.TagDataSource
import com.example.bookkeeping.data.model.RecordType

data class AddRecordUiState(
    val editingRecordId: Long? = null,
    val sourceRecurringRuleId: Long? = null,
    val type: RecordType = RecordType.EXPENSE,
    val amountInput: String = "",
    val selectedTag: String = TagDataSource.defaultTagFor(RecordType.EXPENSE),
    val noteInput: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val selectedAccountId: Long = 1L,
    val errorMessage: String? = null
) {
    val isEditing: Boolean
        get() = editingRecordId != null
}
