package com.example.bookkeeping.data.analytics

import com.example.bookkeeping.data.model.CategoryBreakdown
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.SummaryTotals
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

fun List<Record>.summaryTotals(): SummaryTotals {
    var income = 0.0
    var expense = 0.0

    for (record in this) {
        when (record.type) {
            RecordType.INCOME -> income += record.amount
            RecordType.EXPENSE -> expense += record.amount
        }
    }

    return SummaryTotals(
        income = income,
        expense = expense
    )
}

fun List<Record>.summaryTotalsForMonth(
    yearMonth: YearMonth,
    zoneId: ZoneId
): SummaryTotals {
    return filter { record ->
        record.localDate(zoneId).let { date ->
            date.year == yearMonth.year && date.monthValue == yearMonth.monthValue
        }
    }.summaryTotals()
}

fun List<Record>.summaryTotalsForYear(
    year: Int,
    zoneId: ZoneId
): SummaryTotals {
    return filter { record ->
        record.localDate(zoneId).year == year
    }.summaryTotals()
}

fun List<Record>.categoryBreakdownForDay(
    date: LocalDate,
    type: RecordType,
    zoneId: ZoneId
): List<CategoryBreakdown> {
    return categoryBreakdown(
        predicate = { it.localDate(zoneId) == date },
        type = type
    )
}

fun List<Record>.categoryBreakdownForMonth(
    yearMonth: YearMonth,
    type: RecordType,
    zoneId: ZoneId
): List<CategoryBreakdown> {
    return categoryBreakdown(
        predicate = {
            val date = it.localDate(zoneId)
            date.year == yearMonth.year && date.monthValue == yearMonth.monthValue
        },
        type = type
    )
}

fun List<Record>.categoryBreakdownForYear(
    year: Int,
    type: RecordType,
    zoneId: ZoneId
): List<CategoryBreakdown> {
    return categoryBreakdown(
        predicate = { it.localDate(zoneId).year == year },
        type = type
    )
}

fun List<Record>.availableYears(currentYear: Int, extraPastYears: Int = 2): List<Int> {
    if (isEmpty()) {
        return ((currentYear - extraPastYears)..currentYear).reversed().toList()
    }

    val years = map { record ->
        Instant.ofEpochMilli(record.timestamp)
            .atZone(ZoneId.systemDefault())
            .year
    }
    val minYear = minOf(years.min(), currentYear - extraPastYears)
    val maxYear = maxOf(years.max(), currentYear)
    return (minYear..maxYear).reversed().toList()
}

private fun List<Record>.categoryBreakdown(
    predicate: (Record) -> Boolean,
    type: RecordType
): List<CategoryBreakdown> {
    val grouped = filter { record ->
        record.type == type && predicate(record)
    }.groupBy { record ->
        record.tag
    }.mapValues { (_, records) ->
        records.sumOf { record -> record.amount }
    }.toList()
        .sortedByDescending { (_, amount) -> amount }

    val total = grouped.sumOf { (_, amount) -> amount }
    if (total <= 0.0) {
        return emptyList()
    }

    return grouped.map { (tag, amount) ->
        CategoryBreakdown(
            tag = tag,
            amount = amount,
            percentage = (amount / total).toFloat()
        )
    }
}

private fun Record.localDate(zoneId: ZoneId): LocalDate {
    return Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .toLocalDate()
}
