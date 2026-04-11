package com.example.bookkeeping.ui.main

import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.DailySummary
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.SummaryTotals
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val yearMonth: YearMonth,
    val dailySummary: List<DailySummary>,
    val selectedDate: LocalDate,
    val selectedDayTotals: SummaryTotals,
    val selectedDayRecords: List<Record>,
    val availableYears: List<Int>,
    val accounts: List<Account>,
    val selectedAccountId: Long?
)
