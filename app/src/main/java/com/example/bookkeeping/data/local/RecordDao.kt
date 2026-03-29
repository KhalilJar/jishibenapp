package com.example.bookkeeping.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): RecordEntity?

    @Query("SELECT * FROM records ORDER BY timestamp DESC")
    fun observeAllRecords(): Flow<List<RecordEntity>>

    @Query(
        """
        SELECT * FROM records
        WHERE timestamp >= :startMillis AND timestamp < :endMillis
        ORDER BY timestamp DESC
        """
    )
    fun observeRecordsInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<RecordEntity>>

    @Query(
        """
        SELECT
            date(timestamp / 1000, 'unixepoch', 'localtime') AS day,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS totalExpense
        FROM records
        GROUP BY day
        ORDER BY day DESC
        """
    )
    fun observeDailySummary(): Flow<List<DailySummaryEntity>>

    @Query(
        """
        SELECT
            date(timestamp / 1000, 'unixepoch', 'localtime') AS day,
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END), 0) AS totalIncome,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END), 0) AS totalExpense
        FROM records
        WHERE timestamp >= :startMillis AND timestamp < :endMillis
        GROUP BY day
        ORDER BY day ASC
        """
    )
    fun observeDailySummaryInRange(
        startMillis: Long,
        endMillis: Long
    ): Flow<List<DailySummaryEntity>>
}
