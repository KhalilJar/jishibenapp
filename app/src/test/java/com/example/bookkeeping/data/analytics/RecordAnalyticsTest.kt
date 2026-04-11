package com.example.bookkeeping.data.analytics

import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordAnalyticsTest {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    @Test
    fun summaryTotalsForMonth_andCategoryBreakdown_matchExpectedBuckets() {
        val records = listOf(
            record(date = LocalDate.of(2026, 3, 1), type = RecordType.EXPENSE, amount = 20.0, tag = "吃饭"),
            record(date = LocalDate.of(2026, 3, 2), type = RecordType.EXPENSE, amount = 10.0, tag = "吃饭"),
            record(date = LocalDate.of(2026, 3, 3), type = RecordType.EXPENSE, amount = 30.0, tag = "购物"),
            record(date = LocalDate.of(2026, 3, 4), type = RecordType.INCOME, amount = 100.0, tag = "工资"),
            record(date = LocalDate.of(2025, 3, 1), type = RecordType.EXPENSE, amount = 99.0, tag = "交通")
        )

        val totals = records.summaryTotalsForMonth(YearMonth.of(2026, 3), zoneId)
        assertEquals(100.0, totals.income, 0.001)
        assertEquals(60.0, totals.expense, 0.001)
        assertEquals(40.0, totals.net, 0.001)

        val breakdown = records.categoryBreakdownForMonth(
            yearMonth = YearMonth.of(2026, 3),
            type = RecordType.EXPENSE,
            zoneId = zoneId
        )

        assertEquals(2, breakdown.size)
        assertEquals("吃饭", breakdown[0].tag)
        assertEquals(30.0, breakdown[0].amount, 0.001)
        assertEquals(0.5f, breakdown[0].percentage, 0.001f)
        assertEquals("购物", breakdown[1].tag)
        assertEquals(30.0, breakdown[1].amount, 0.001)
        assertEquals(0.5f, breakdown[1].percentage, 0.001f)
    }

    @Test
    fun availableYears_spansExistingDataAndCurrentYearFloor() {
        val years = listOf(
            record(date = LocalDate.of(2024, 5, 1), type = RecordType.EXPENSE, amount = 20.0, tag = "吃饭"),
            record(date = LocalDate.of(2026, 1, 1), type = RecordType.INCOME, amount = 88.0, tag = "工资")
        ).availableYears(currentYear = 2026)

        assertEquals(listOf(2026, 2025, 2024), years)
        assertTrue(years.contains(2026))
    }

    private fun record(
        date: LocalDate,
        type: RecordType,
        amount: Double,
        tag: String
    ): Record {
        return Record(
            type = type,
            amount = amount,
            tag = tag,
            note = null,
            timestamp = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        )
    }
}
