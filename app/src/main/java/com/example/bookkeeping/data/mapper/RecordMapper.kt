package com.example.bookkeeping.data.mapper

import com.example.bookkeeping.data.local.entity.AccountEntity
import com.example.bookkeeping.data.local.entity.BudgetEntity
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.local.entity.RecurringRuleEntity
import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.DailySummary
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecurringRule

fun RecordEntity.toDomain(): Record {
    return Record(
        id = id,
        type = type,
        amount = amount,
        tag = tag,
        note = note,
        timestamp = timestamp,
        accountId = accountId
    )
}

fun Record.toEntity(): RecordEntity {
    return RecordEntity(
        id = id,
        type = type,
        amount = amount,
        tag = tag,
        note = note,
        timestamp = timestamp,
        accountId = accountId
    )
}

fun DailySummaryEntity.toDomain(): DailySummary {
    return DailySummary(
        day = day,
        totalIncome = totalIncome,
        totalExpense = totalExpense
    )
}

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        type = type,
        isArchived = isArchived,
        sortOrder = sortOrder
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        type = type,
        isArchived = isArchived,
        sortOrder = sortOrder
    )
}

fun BudgetEntity.toDomain(): Budget {
    return Budget(
        id = id,
        yearMonth = yearMonth,
        scope = scope,
        tag = tag,
        amountLimit = amountLimit
    )
}

fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        yearMonth = yearMonth,
        scope = scope,
        tag = tag,
        amountLimit = amountLimit
    )
}

fun RecurringRuleEntity.toDomain(): RecurringRule {
    return RecurringRule(
        id = id,
        title = title,
        type = type,
        amount = amount,
        tag = tag,
        note = note,
        accountId = accountId,
        frequency = frequency,
        intervalCount = intervalCount,
        startDate = startDate,
        nextDueDate = nextDueDate,
        isActive = isActive
    )
}

fun RecurringRule.toEntity(): RecurringRuleEntity {
    return RecurringRuleEntity(
        id = id,
        title = title,
        type = type,
        amount = amount,
        tag = tag,
        note = note,
        accountId = accountId,
        frequency = frequency,
        intervalCount = intervalCount,
        startDate = startDate,
        nextDueDate = nextDueDate,
        isActive = isActive
    )
}
