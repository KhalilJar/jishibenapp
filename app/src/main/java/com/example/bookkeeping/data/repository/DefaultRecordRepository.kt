package com.example.bookkeeping.data.repository

import androidx.room.withTransaction
import com.example.bookkeeping.data.backup.BackupSnapshot
import com.example.bookkeeping.data.local.AccountDao
import com.example.bookkeeping.data.local.AppDatabase
import com.example.bookkeeping.data.local.BudgetDao
import com.example.bookkeeping.data.local.RecordDao
import com.example.bookkeeping.data.local.RecurringRuleDao
import com.example.bookkeeping.data.local.entity.AccountEntity
import com.example.bookkeeping.data.local.entity.BudgetEntity
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.local.entity.RecurringRuleEntity
import com.example.bookkeeping.data.mapper.toDomain
import com.example.bookkeeping.data.mapper.toEntity
import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecurringRule
import kotlinx.coroutines.flow.Flow

class DefaultRecordRepository(
    private val appDatabase: AppDatabase? = null,
    private val recordDao: RecordDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao,
    private val recurringRuleDao: RecurringRuleDao
) : RecordRepository {

    override fun observeRecords(): Flow<List<RecordEntity>> = recordDao.observeAllRecords()

    override fun observeRecordsInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<RecordEntity>> = recordDao.observeRecordsInRange(startMillis, endMillis)

    override fun observeDailySummary(): Flow<List<DailySummaryEntity>> = recordDao.observeDailySummary()

    override fun observeDailySummaryInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<DailySummaryEntity>> = recordDao.observeDailySummaryInRange(startMillis, endMillis)

    override suspend fun insertRecord(record: Record) {
        recordDao.insertRecord(record.toEntity())
    }

    override suspend fun updateRecord(record: Record) {
        recordDao.updateRecord(record.toEntity())
    }

    override suspend fun deleteRecord(recordId: Long) {
        val target = recordDao.getRecordById(recordId) ?: return
        recordDao.deleteRecord(target)
    }

    override fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAccounts()

    override suspend fun insertAccount(account: Account): Long {
        return accountDao.insertAccount(account.toEntity())
    }

    override suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account.toEntity())
    }

    override suspend fun getAccount(accountId: Long): AccountEntity? {
        return accountDao.getAccountById(accountId)
    }

    override fun observeBudgets(): Flow<List<BudgetEntity>> = budgetDao.observeBudgets()

    override suspend fun insertBudget(budget: Budget): Long {
        return budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(budgetId: Long) {
        budgetDao.deleteBudget(budgetId)
    }

    override fun observeRecurringRules(): Flow<List<RecurringRuleEntity>> = recurringRuleDao.observeRecurringRules()

    override suspend fun insertRecurringRule(rule: RecurringRule): Long {
        return recurringRuleDao.insertRecurringRule(rule.toEntity())
    }

    override suspend fun updateRecurringRule(rule: RecurringRule) {
        recurringRuleDao.updateRecurringRule(rule.toEntity())
    }

    override suspend fun deleteRecurringRule(ruleId: Long) {
        recurringRuleDao.deleteRecurringRule(ruleId)
    }

    override suspend fun getRecurringRule(ruleId: Long): RecurringRuleEntity? {
        return recurringRuleDao.getRecurringRuleById(ruleId)
    }

    override suspend fun exportSnapshot(): BackupSnapshot {
        return BackupSnapshot(
            exportedAt = System.currentTimeMillis(),
            records = recordDao.getAllRecords().map { it.toDomain() },
            accounts = accountDao.getAllAccounts().map { it.toDomain() },
            budgets = budgetDao.getAllBudgets().map { it.toDomain() },
            recurringRules = recurringRuleDao.getAllRecurringRules().map { it.toDomain() }
        )
    }

    override suspend fun importSnapshot(snapshot: BackupSnapshot) {
        suspend fun replaceAllData() {
            recurringRuleDao.clearRecurringRules()
            budgetDao.clearBudgets()
            recordDao.clearRecords()
            accountDao.clearAccounts()

            accountDao.insertAccounts(snapshot.accounts.map { it.toEntity() })
            budgetDao.insertBudgets(snapshot.budgets.map { it.toEntity() })
            recurringRuleDao.insertRecurringRules(snapshot.recurringRules.map { it.toEntity() })
            recordDao.insertRecords(snapshot.records.map { it.toEntity() })
        }

        val database = appDatabase
        if (database != null) {
            database.withTransaction {
                replaceAllData()
            }
        } else {
            replaceAllData()
        }
    }
}
