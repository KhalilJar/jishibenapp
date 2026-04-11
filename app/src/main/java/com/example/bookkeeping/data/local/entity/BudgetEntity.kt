package com.example.bookkeeping.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bookkeeping.data.model.BudgetScope

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["yearMonth"]),
        Index(value = ["yearMonth", "scope", "tag"], unique = true)
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val yearMonth: String,
    val scope: BudgetScope,
    val tag: String? = null,
    val amountLimit: Double
)
