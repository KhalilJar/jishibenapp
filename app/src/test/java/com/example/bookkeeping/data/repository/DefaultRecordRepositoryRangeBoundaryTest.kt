package com.example.bookkeeping.data.repository

import com.example.bookkeeping.data.local.AccountDao
import com.example.bookkeeping.data.local.BudgetDao
import com.example.bookkeeping.data.local.RecordDao
import com.example.bookkeeping.data.local.RecurringRuleDao
import com.example.bookkeeping.data.local.entity.AccountEntity
import com.example.bookkeeping.data.local.entity.BudgetEntity
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.local.entity.RecurringRuleEntity
import com.example.bookkeeping.data.model.AccountType
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultRecordRepositoryRangeBoundaryTest {

    @Test
    fun observeRecordsInRange_passesCrossMonthBoundaryToDao() = runTest {
        val recordDao = FakeRecordDao()
        val repository = DefaultRecordRepository(
            recordDao = recordDao,
            accountDao = FakeAccountDao(),
            budgetDao = FakeBudgetDao(),
            recurringRuleDao = FakeRecurringRuleDao()
        )
        val zoneId = ZoneId.systemDefault()

        val start = LocalDate.of(2026, 3, 31).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 4, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        repository.observeRecordsInRange(start, end).first()

        assertEquals(start to end, recordDao.lastObserveRecordsInRange)
    }

    @Test
    fun observeDailySummaryInRange_passesCrossMonthBoundaryToDao() = runTest {
        val recordDao = FakeRecordDao()
        val repository = DefaultRecordRepository(
            recordDao = recordDao,
            accountDao = FakeAccountDao(),
            budgetDao = FakeBudgetDao(),
            recurringRuleDao = FakeRecurringRuleDao()
        )
        val zoneId = ZoneId.systemDefault()

        val start = LocalDate.of(2026, 3, 31).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 4, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        repository.observeDailySummaryInRange(start, end).first()

        assertEquals(start to end, recordDao.lastObserveDailySummaryInRange)
    }

    private class FakeRecordDao : RecordDao {
        var lastObserveRecordsInRange: Pair<Long, Long>? = null
        var lastObserveDailySummaryInRange: Pair<Long, Long>? = null

        override suspend fun insertRecord(record: RecordEntity): Long = 0L
        override suspend fun insertRecords(records: List<RecordEntity>) = Unit
        override suspend fun updateRecord(record: RecordEntity) = Unit
        override suspend fun deleteRecord(record: RecordEntity) = Unit
        override suspend fun getRecordById(id: Long): RecordEntity? = null
        override fun observeAllRecords(): Flow<List<RecordEntity>> = flowOf(emptyList())
        override suspend fun getAllRecords(): List<RecordEntity> = emptyList()
        override fun observeRecordsInRange(startMillis: Long, endMillis: Long): Flow<List<RecordEntity>> {
            lastObserveRecordsInRange = startMillis to endMillis
            return flowOf(emptyList())
        }
        override fun observeDailySummary(): Flow<List<DailySummaryEntity>> = flowOf(emptyList())
        override fun observeDailySummaryInRange(startMillis: Long, endMillis: Long): Flow<List<DailySummaryEntity>> {
            lastObserveDailySummaryInRange = startMillis to endMillis
            return flowOf(emptyList())
        }
        override suspend fun clearRecords() = Unit
    }

    private class FakeAccountDao : AccountDao {
        override fun observeAccounts(): Flow<List<AccountEntity>> = flowOf(
            listOf(AccountEntity(id = 1, name = "现金", type = AccountType.CASH, sortOrder = 0))
        )
        override suspend fun getAllAccounts(): List<AccountEntity> = emptyList()
        override suspend fun insertAccount(account: AccountEntity): Long = 0L
        override suspend fun insertAccounts(accounts: List<AccountEntity>) = Unit
        override suspend fun updateAccount(account: AccountEntity) = Unit
        override suspend fun getAccountById(id: Long): AccountEntity? = null
        override suspend fun clearAccounts() = Unit
    }

    private class FakeBudgetDao : BudgetDao {
        override fun observeBudgets(): Flow<List<BudgetEntity>> = flowOf(emptyList())
        override suspend fun getAllBudgets(): List<BudgetEntity> = emptyList()
        override suspend fun insertBudget(budget: BudgetEntity): Long = 0L
        override suspend fun insertBudgets(budgets: List<BudgetEntity>) = Unit
        override suspend fun updateBudget(budget: BudgetEntity) = Unit
        override suspend fun deleteBudget(id: Long) = Unit
        override suspend fun clearBudgets() = Unit
    }

    private class FakeRecurringRuleDao : RecurringRuleDao {
        override fun observeRecurringRules(): Flow<List<RecurringRuleEntity>> = flowOf(emptyList())
        override suspend fun getAllRecurringRules(): List<RecurringRuleEntity> = emptyList()
        override suspend fun insertRecurringRule(rule: RecurringRuleEntity): Long = 0L
        override suspend fun insertRecurringRules(rules: List<RecurringRuleEntity>) = Unit
        override suspend fun updateRecurringRule(rule: RecurringRuleEntity) = Unit
        override suspend fun getRecurringRuleById(id: Long): RecurringRuleEntity? = null
        override suspend fun deleteRecurringRule(id: Long) = Unit
        override suspend fun clearRecurringRules() = Unit
    }
}
