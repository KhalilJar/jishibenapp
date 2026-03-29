package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.local.entity.DailySummaryEntity
import com.example.bookkeeping.data.local.entity.RecordEntity
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.repository.RecordRepository
import java.time.LocalDate
import java.time.ZoneId
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelBoundaryTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun calendarDailySummary_usesMonthRange_whenMonthChanges() = runTest {
        val repository = FakeRecordRepository()
        val viewModel = MainViewModel(repository)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.calendarDailySummary.collect { }
        }
        advanceUntilIdle()

        assertLatestMonthRangeMatches(repository, viewModel.currentMonth.value)

        viewModel.previousMonth()
        advanceUntilIdle()
        assertLatestMonthRangeMatches(repository, viewModel.currentMonth.value)

        viewModel.nextMonth()
        advanceUntilIdle()
        assertLatestMonthRangeMatches(repository, viewModel.currentMonth.value)

        collectJob.cancel()
    }

    @Test
    fun observeRecordsForDay_usesClosedOpenDayRange_acrossMonthBoundary() = runTest {
        val repository = FakeRecordRepository()
        val viewModel = MainViewModel(repository)
        val zoneId = ZoneId.systemDefault()

        val march31Start = LocalDate.of(2026, 3, 31).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val april1Start = LocalDate.of(2026, 4, 1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val april2Start = LocalDate.of(2026, 4, 2).atStartOfDay(zoneId).toInstant().toEpochMilli()

        viewModel.observeRecordsForDay(march31Start).first()
        assertEquals(march31Start to april1Start, repository.recordRanges.last())

        viewModel.observeRecordsForDay(april1Start).first()
        assertEquals(april1Start to april2Start, repository.recordRanges.last())
    }

    private fun assertLatestMonthRangeMatches(
        repository: FakeRecordRepository,
        yearMonth: YearMonth
    ) {
        val zoneId = ZoneId.systemDefault()
        val expectedStart = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val expectedEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        assertEquals(expectedStart to expectedEnd, repository.summaryRanges.last())
    }

    private class FakeRecordRepository : RecordRepository {
        val recordRanges: MutableList<Pair<Long, Long>> = mutableListOf()
        val summaryRanges: MutableList<Pair<Long, Long>> = mutableListOf()

        override fun observeRecords(): Flow<List<RecordEntity>> = flowOf(emptyList())

        override fun observeRecordsInRange(startMillis: Long, endMillis: Long): Flow<List<RecordEntity>> {
            recordRanges.add(startMillis to endMillis)
            return flowOf(emptyList())
        }

        override fun observeDailySummary(): Flow<List<DailySummaryEntity>> = flowOf(emptyList())

        override fun observeDailySummaryInRange(
            startMillis: Long,
            endMillis: Long
        ): Flow<List<DailySummaryEntity>> {
            summaryRanges.add(startMillis to endMillis)
            return flowOf(emptyList())
        }

        override suspend fun insertRecord(record: Record) = Unit

        override suspend fun updateRecord(record: Record) = Unit

        override suspend fun deleteRecord(recordId: Long) = Unit
    }
}
