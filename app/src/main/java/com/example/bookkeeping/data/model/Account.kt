package com.example.bookkeeping.data.model

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0
) {
    val isSystem: Boolean
        get() = type != AccountType.CUSTOM
}
