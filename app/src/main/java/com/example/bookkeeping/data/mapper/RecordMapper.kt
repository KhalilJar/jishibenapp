package com.example.bookkeeping.data.mapper

import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.model.DailySummary
import com.example.bookkeeping.data.model.Record

fun RecordEntity.toDomain(): Record {
    return Record(
        id = id,
        type = type,
        amount = amount,
        tag = tag,
        note = note,
        timestamp = timestamp
    )
}

fun Record.toEntity(): RecordEntity {
    return RecordEntity(
        id = id,
        type = type,
        amount = amount,
        tag = tag,
        note = note,
        timestamp = timestamp
    )
}

fun DailySummaryEntity.toDomain(): DailySummary {
    return DailySummary(
        day = day,
        totalIncome = totalIncome,
        totalExpense = totalExpense
    )
}
