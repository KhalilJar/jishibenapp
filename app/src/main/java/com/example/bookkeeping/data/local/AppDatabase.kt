package com.example.bookkeeping.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookkeeping.data.local.entity.AccountEntity
import com.example.bookkeeping.data.local.entity.BudgetEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.local.entity.RecurringRuleEntity

@Database(
    entities = [RecordEntity::class, AccountEntity::class, BudgetEntity::class, RecurringRuleEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringRuleDao(): RecurringRuleDao

    companion object {
        const val DB_NAME = "bookkeeping.db"

        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    ALTER TABLE records ADD COLUMN accountId INTEGER NOT NULL DEFAULT 1
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_records_accountId ON records(accountId)")
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS accounts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        isArchived INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_accounts_sortOrder ON accounts(sortOrder)")
                database.execSQL(
                    """
                    INSERT INTO accounts (id, name, type, isArchived, sortOrder) VALUES
                    (1, '现金', 'CASH', 0, 0),
                    (2, '银行卡', 'BANK_CARD', 0, 1),
                    (3, '支付宝', 'ALIPAY', 0, 2),
                    (4, '微信', 'WECHAT', 0, 3)
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS budgets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        yearMonth TEXT NOT NULL,
                        scope TEXT NOT NULL,
                        tag TEXT,
                        amountLimit REAL NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_budgets_yearMonth ON budgets(yearMonth)")
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_budgets_yearMonth_scope_tag ON budgets(yearMonth, scope, tag)"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS recurring_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        tag TEXT NOT NULL,
                        note TEXT,
                        accountId INTEGER NOT NULL,
                        frequency TEXT NOT NULL,
                        intervalCount INTEGER NOT NULL,
                        startDate TEXT NOT NULL,
                        nextDueDate TEXT NOT NULL,
                        isActive INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_rules_nextDueDate ON recurring_rules(nextDueDate)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_recurring_rules_accountId ON recurring_rules(accountId)")
            }
        }
    }
}
