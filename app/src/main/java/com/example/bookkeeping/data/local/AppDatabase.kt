package com.example.bookkeeping.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bookkeeping.data.local.entity.RecordEntity

@Database(
    entities = [RecordEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        const val DB_NAME = "bookkeeping.db"
    }
}
