package com.example.bookkeeping.data.repository

import com.example.bookkeeping.data.local.RecordDao
import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
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
        val dao = FakeRecordDao()
        val repository = DefaultRecordRepository(dao)
        val zoneId = ZoneId.systemDefault()

        val start = LocalDate.of(2026, 3, 31).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 4, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        repository.observeRecordsInRange(start, end).first()

        assertEquals(start to end, dao.lastObserveRecordsInRange)
    }

    @Test
    fun observeDailySummaryInRange_passesCrossMonthBoundaryToDao() = runTest {
        val dao = FakeRecordDao()
        val repository = DefaultRecordRepository(dao)
        val zoneId = ZoneId.systemDefault()

        val start = LocalDate.of(2026, 3, 31).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 4, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        repository.observeDailySummaryInRange(start, end).first()

        assertEquals(start to end, dao.lastObserveDailySummaryInRange)
    }

    private class FakeRecordDao : RecordDao {
        var lastObserveRecordsInRange: Pair<Long, Long>? = null
        var lastObserveDailySummaryInRange: Pair<Long, Long>? = null

        override suspend fun insertRecord(record: RecordEntity): Long = 0L

        override suspend fun updateRecord(record: RecordEntity) = Unit

        override suspend fun deleteRecord(record: RecordEntity) = Unit

        override suspend fun getRecordById(id: Long): RecordEntity? = null

        override fun observeAllRecords(): Flow<List<RecordEntity>> = flowOf(emptyList())

        override fun observeRecordsInRange(startMillis: Long, endMillis: Long): Flow<List<RecordEntity>> {
            lastObserveRecordsInRange = startMillis to endMillis
            return flowOf(emptyList())
        }

        override fun observeDailySummary(): Flow<List<DailySummaryEntity>> = flowOf(emptyList())

        override fun observeDailySummaryInRange(
            startMillis: Long,
            endMillis: Long
        ): Flow<List<DailySummaryEntity>> {
            lastObserveDailySummaryInRange = startMillis to endMillis
            return flowOf(emptyList())
        }
    }
}
