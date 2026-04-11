package com.example.bookkeeping

import android.app.Application
import androidx.room.Room
import com.example.bookkeeping.data.local.AppDatabase
import com.example.bookkeeping.data.backup.BackupManager
import com.example.bookkeeping.data.repository.DefaultRecordRepository
import com.example.bookkeeping.data.repository.RecordRepository

class BookkeepingApplication : Application() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    val recordRepository: RecordRepository by lazy {
        DefaultRecordRepository(
            appDatabase = database,
            recordDao = database.recordDao(),
            accountDao = database.accountDao(),
            budgetDao = database.budgetDao(),
            recurringRuleDao = database.recurringRuleDao()
        )
    }

    val backupManager: BackupManager by lazy {
        BackupManager(applicationContext)
    }
}
