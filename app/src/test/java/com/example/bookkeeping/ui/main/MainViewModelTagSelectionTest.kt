package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.local.entity.AccountEntity
import com.example.bookkeeping.data.local.entity.BudgetEntity
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.local.entity.RecurringRuleEntity
import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.AccountType
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.RecurringRule
import com.example.bookkeeping.data.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MainViewModelTagSelectionTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun onTypeSelected_replacesInvalidTagWithDefaultIncomeTag() = runTest {
        val viewModel = MainViewModel(FakeRecordRepository())
        advanceUntilIdle()

        viewModel.onTagSelected("吃饭")
        viewModel.onTypeSelected(RecordType.INCOME)

        assertEquals("工资", viewModel.addRecordState.value.selectedTag)
        assertEquals(RecordType.INCOME, viewModel.addRecordState.value.type)
    }

    @Test
    fun saveRecord_rejectsTagThatDoesNotMatchCurrentType() = runTest {
        val viewModel = MainViewModel(FakeRecordRepository())
        advanceUntilIdle()

        viewModel.onAmountChanged("88")
        viewModel.onTagSelected("奖金")
        viewModel.saveRecord()

        assertEquals("当前收支类型与标签不匹配", viewModel.addRecordState.value.errorMessage)
    }

    @Test
    fun onTypeSelected_keepsCurrentTagWhenItIsStillValid() = runTest {
        val viewModel = MainViewModel(FakeRecordRepository())
        advanceUntilIdle()

        viewModel.onTypeSelected(RecordType.INCOME)
        viewModel.onTagSelected("奖金")
        viewModel.onTypeSelected(RecordType.INCOME)

        assertEquals("奖金", viewModel.addRecordState.value.selectedTag)
        assertNull(viewModel.addRecordState.value.errorMessage)
    }

    private class FakeRecordRepository : RecordRepository {
        override fun observeRecords(): Flow<List<RecordEntity>> = flowOf(emptyList())
        override fun observeRecordsInRange(startMillis: Long, endMillis: Long): Flow<List<RecordEntity>> = flowOf(emptyList())
        override fun observeDailySummary(): Flow<List<DailySummaryEntity>> = flowOf(emptyList())
        override fun observeDailySummaryInRange(startMillis: Long, endMillis: Long): Flow<List<DailySummaryEntity>> = flowOf(emptyList())
        override suspend fun insertRecord(record: Record) = Unit
        override suspend fun updateRecord(record: Record) = Unit
        override suspend fun deleteRecord(recordId: Long) = Unit
        override fun observeAccounts(): Flow<List<AccountEntity>> = flowOf(
            listOf(AccountEntity(id = 1, name = "现金", type = AccountType.CASH, sortOrder = 0))
        )
        override suspend fun insertAccount(account: Account): Long = 0L
        override suspend fun updateAccount(account: Account) = Unit
        override suspend fun getAccount(accountId: Long): AccountEntity? = null
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
