package com.example.bookkeeping.data.repository

import com.example.bookkeeping.data.backup.BackupSnapshot
import com.example.bookkeeping.data.local.entity.AccountEntity
import com.example.bookkeeping.data.local.entity.BudgetEntity
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.local.entity.RecurringRuleEntity
import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecurringRule
import kotlinx.coroutines.flow.Flow

interface RecordRepository {
    fun observeRecords(): Flow<List<RecordEntity>>
    fun observeRecordsInRange(startMillis: Long, endMillis: Long): Flow<List<RecordEntity>>

    fun observeDailySummary(): Flow<List<DailySummaryEntity>>
    fun observeDailySummaryInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<DailySummaryEntity>>

    suspend fun insertRecord(record: Record)

    suspend fun updateRecord(record: Record)
    suspend fun deleteRecord(recordId: Long)

    fun observeAccounts(): Flow<List<AccountEntity>>
    suspend fun insertAccount(account: Account): Long
    suspend fun updateAccount(account: Account)
    suspend fun getAccount(accountId: Long): AccountEntity?

    fun observeBudgets(): Flow<List<BudgetEntity>>
    suspend fun insertBudget(budget: Budget): Long
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budgetId: Long)

    fun observeRecurringRules(): Flow<List<RecurringRuleEntity>>
    suspend fun insertRecurringRule(rule: RecurringRule): Long
    suspend fun updateRecurringRule(rule: RecurringRule)
    suspend fun deleteRecurringRule(ruleId: Long)
    suspend fun getRecurringRule(ruleId: Long): RecurringRuleEntity?

    suspend fun exportSnapshot(): BackupSnapshot
    suspend fun importSnapshot(snapshot: BackupSnapshot)
}
