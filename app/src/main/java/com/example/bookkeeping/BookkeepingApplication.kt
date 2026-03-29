package com.example.bookkeeping

import android.app.Application
import androidx.room.Room
import com.example.bookkeeping.data.local.AppDatabase
import com.example.bookkeeping.data.repository.DefaultRecordRepository
import com.example.bookkeeping.data.repository.RecordRepository

class BookkeepingApplication : Application() {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val recordRepository: RecordRepository by lazy {
        DefaultRecordRepository(database.recordDao())
    }
}
