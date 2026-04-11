package com.example.bookkeeping.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.RecurringFrequency

@Entity(
    tableName = "recurring_rules",
    indices = [Index(value = ["nextDueDate"]), Index(value = ["accountId"])]
)
data class RecurringRuleEntity(
    @PrimaryKey(autoGenerate = true)
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
