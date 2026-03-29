package com.example.bookkeeping.data.repository

import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.model.Record
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

    // Reserved for edit/delete entry workflows.
    suspend fun updateRecord(record: Record)
    suspend fun deleteRecord(recordId: Long)
}
