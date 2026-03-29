package com.example.bookkeeping.data.repository

import com.example.bookkeeping.data.local.RecordDao
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.mapper.toEntity
import com.example.bookkeeping.data.model.Record
import kotlinx.coroutines.flow.Flow

class DefaultRecordRepository(
    private val recordDao: RecordDao
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
}
