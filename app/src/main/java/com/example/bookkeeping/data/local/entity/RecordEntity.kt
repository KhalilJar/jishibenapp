package com.example.bookkeeping.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bookkeeping.data.model.RecordType

@Entity(
    tableName = "records",
    indices = [Index(value = ["timestamp"]), Index(value = ["type"]), Index(value = ["tag"])]
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: RecordType,
    val amount: Double,
    val tag: String,
    val note: String?,
    val timestamp: Long
)
