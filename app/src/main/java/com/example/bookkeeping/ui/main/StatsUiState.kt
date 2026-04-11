package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.CategoryBreakdown
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.StatsPeriod
import com.example.bookkeeping.data.model.SummaryTotals
import java.time.LocalDate
import java.time.YearMonth

data class StatsUiState(
    val period: StatsPeriod,
    val recordType: RecordType,
    val selectedDay: LocalDate,
    val selectedMonth: YearMonth,
    val selectedYear: Int,
    val totals: SummaryTotals,
    val breakdown: List<CategoryBreakdown>,
    val availableYears: List<Int>,
    val accounts: List<Account>,
    val selectedAccountId: Long?
)
