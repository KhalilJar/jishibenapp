package com.example.bookkeeping.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bookkeeping.data.model.AccountType

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["sortOrder"])]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0
)
