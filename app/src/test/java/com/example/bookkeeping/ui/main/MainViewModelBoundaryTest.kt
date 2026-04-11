package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.local.entity.AccountEntity
import com.example.bookkeeping.data.local.entity.BudgetEntity
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.local.entity.RecurringRuleEntity
import com.example.bookkeeping.data.model.AccountType
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.RecurringRule
import com.example.bookkeeping.data.repository.RecordRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelBoundaryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun observeRecordsForDay_filtersByDateAndAccount() = runTest {
        val zoneId = ZoneId.systemDefault()
        val march31 = LocalDate.of(2026, 3, 31).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val april1 = LocalDate.of(2026, 4, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val repository = FakeRecordRepository(
            records = listOf(
                RecordEntity(id = 1, type = RecordType.EXPENSE, amount = 10.0, tag = "吃饭", note = null, timestamp = march31, accountId = 1),
                RecordEntity(id = 2, type = RecordType.EXPENSE, amount = 20.0, tag = "购物", note = null, timestamp = march31, accountId = 2),
                RecordEntity(id = 3, type = RecordType.INCOME, amount = 30.0, tag = "工资", note = null, timestamp = april1, accountId = 1)
            ),
            accounts = listOf(
                AccountEntity(id = 1, name = "现金", type = AccountType.CASH, sortOrder = 0),
                AccountEntity(id = 2, name = "银行卡", type = AccountType.BANK_CARD, sortOrder = 1)
            )
        )
        val viewModel = MainViewModel(repository)

        val allRecords = viewModel.observeRecordsForDay(march31, null).first()
        val accountRecords = viewModel.observeRecordsForDay(march31, 2L).first()

        assertEquals(2, allRecords.size)
        assertEquals(1, accountRecords.size)
        assertEquals(2L, accountRecords.first().accountId)
    }

    @Test
    fun recordListUiState_appliesKeywordTypeAndAccountFilters() = runTest {
        val zoneId = ZoneId.systemDefault()
        val now = LocalDate.of(2026, 3, 31).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val repository = FakeRecordRepository(
            records = listOf(
                RecordEntity(id = 1, type = RecordType.EXPENSE, amount = 10.0, tag = "吃饭", note = "早餐", timestamp = now, accountId = 1),
                RecordEntity(id = 2, type = RecordType.INCOME, amount = 88.0, tag = "工资", note = "工资卡", timestamp = now, accountId = 2)
            ),
            accounts = listOf(
                AccountEntity(id = 1, name = "现金", type = AccountType.CASH, sortOrder = 0),
                AccountEntity(id = 2, name = "银行卡", type = AccountType.BANK_CARD, sortOrder = 1)
            )
        )
        val viewModel = MainViewModel(repository)

        viewModel.updateFilterKeyword("工资")
        viewModel.updateFilterType(RecordType.INCOME)
        viewModel.updateFilterAccount(2L)
        advanceUntilIdle()

        val records = viewModel.recordListUiState.first { it.records.size == 1 }.records
        assertEquals(1, records.size)
        assertEquals(2L, records.first().accountId)
        assertEquals(RecordType.INCOME, records.first().type)
    }

    @Test
    fun jumpToMonth_defaultsCalendarSelectionToFirstDayForNonCurrentMonth() = runTest {
        val repository = FakeRecordRepository(
            accounts = listOf(
                AccountEntity(id = 1, name = "现金", type = AccountType.CASH, sortOrder = 0)
            )
        )
        val viewModel = MainViewModel(repository)
        val targetMonth = YearMonth.of(2030, 5)

        viewModel.jumpToMonth(targetMonth)
        advanceUntilIdle()

        val calendarUiState = viewModel.calendarUiState.first { it.yearMonth == targetMonth }
        assertEquals(targetMonth, calendarUiState.yearMonth)
        assertEquals(targetMonth.atDay(1), calendarUiState.selectedDate)
    }

    private class FakeRecordRepository(
        private val records: List<RecordEntity> = emptyList(),
        private val accounts: List<AccountEntity> = emptyList()
    ) : RecordRepository {
        override fun observeRecords(): Flow<List<RecordEntity>> = flowOf(records)
        override fun observeRecordsInRange(startMillis: Long, endMillis: Long): Flow<List<RecordEntity>> = flowOf(records)
        override fun observeDailySummary(): Flow<List<DailySummaryEntity>> = flowOf(emptyList())
        override fun observeDailySummaryInRange(startMillis: Long, endMillis: Long): Flow<List<DailySummaryEntity>> = flowOf(emptyList())
        override suspend fun insertRecord(record: Record) = Unit
        override suspend fun updateRecord(record: Record) = Unit
        override suspend fun deleteRecord(recordId: Long) = Unit
        override fun observeAccounts(): Flow<List<AccountEntity>> = flowOf(accounts)
        override suspend fun insertAccount(account: com.example.bookkeeping.data.model.Account): Long = 0L
        override suspend fun updateAccount(account: com.example.bookkeeping.data.model.Account) = Unit
        override suspend fun getAccount(accountId: Long): AccountEntity? = accounts.firstOrNull { it.id == accountId }
        override fun observeBudgets(): Flow<List<BudgetEntity>> = flowOf(emptyList())
        override suspend fun insertBudget(budget: Budget): Long = 0L
        override suspend fun updateBudget(budget: Budget) = Unit
        override suspend fun deleteBudget(budgetId: Long) = Unit
        override fun observeRecurringRules(): Flow<List<RecurringRuleEntity>> = flowOf(emptyList())
        override suspend fun insertRecurringRule(rule: RecurringRule): Long = 0L
        override suspend fun updateRecurringRule(rule: RecurringRule) = Unit
        override suspend fun deleteRecurringRule(ruleId: Long) = Unit
        override suspend fun getRecurringRule(ruleId: Long): RecurringRuleEntity? = null
        override suspend fun exportSnapshot() = throw UnsupportedOperationException()
        override suspend fun importSnapshot(snapshot: com.example.bookkeeping.data.backup.BackupSnapshot) = Unit
    }
}
