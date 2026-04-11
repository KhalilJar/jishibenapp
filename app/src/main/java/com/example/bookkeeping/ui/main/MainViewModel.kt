package com.example.bookkeeping.ui.main

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookkeeping.data.TagDataSource
import com.example.bookkeeping.data.analytics.availableYears
import com.example.bookkeeping.data.analytics.categoryBreakdownForDay
import com.example.bookkeeping.data.analytics.categoryBreakdownForMonth
import com.example.bookkeeping.data.analytics.categoryBreakdownForYear
import com.example.bookkeeping.data.analytics.summaryTotals
import com.example.bookkeeping.data.backup.BackupManager
import com.example.bookkeeping.data.mapper.toDomain
import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.AccountType
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.BudgetScope
import com.example.bookkeeping.data.model.DailySummary
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.RecurringFrequency
import com.example.bookkeeping.data.model.RecurringRule
import com.example.bookkeeping.data.model.StatsPeriod
import com.example.bookkeeping.data.model.SummaryTotals
import com.example.bookkeeping.data.repository.RecordRepository
import java.time.Instant
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val backupMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
private val backupDayFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

class MainViewModel(
    private val repository: RecordRepository,
    private val backupManager: BackupManager? = null
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val today: LocalDate = LocalDate.now(zoneId)
    private val initialMonth: YearMonth = YearMonth.now(zoneId)
    private val initialYear = Year.now(zoneId).value

    val records: StateFlow<List<Record>> = repository.observeRecords()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val accounts: StateFlow<List<Account>> = repository.observeAccounts()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val activeAccounts: StateFlow<List<Account>> = accounts
        .map { list -> list.filter { !it.isArchived }.sortedBy { it.sortOrder } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val budgets: StateFlow<List<Budget>> = repository.observeBudgets()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val recurringRules: StateFlow<List<RecurringRule>> = repository.observeRecurringRules()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val availableYears: StateFlow<List<Int>> = records
        .map { items -> items.availableYears(currentYear = initialYear) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf(initialYear)
        )

    private val _currentMonth = MutableStateFlow(initialMonth)
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _calendarAccountId = MutableStateFlow<Long?>(null)
    private val _selectedCalendarDate = MutableStateFlow(today)
    private val _statsAccountId = MutableStateFlow<Long?>(null)
    private val _recordFilters = MutableStateFlow(RecordFilterUiState())

    private val _addRecordState = MutableStateFlow(AddRecordUiState())
    val addRecordState: StateFlow<AddRecordUiState> = _addRecordState.asStateFlow()

    val addRecordTags: StateFlow<List<String>> = _addRecordState
        .map { state -> TagDataSource.tagsFor(state.type) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TagDataSource.tagsFor(RecordType.EXPENSE)
        )

    private val _statsPeriod = MutableStateFlow(StatsPeriod.MONTH)
    private val _statsRecordType = MutableStateFlow(RecordType.EXPENSE)
    private val _statsSelectedDay = MutableStateFlow(today)
    private val _statsSelectedMonth = MutableStateFlow(initialMonth)
    private val _statsSelectedYear = MutableStateFlow(initialYear)

    private val _dataManagementUiState = MutableStateFlow(DataManagementUiState())
    val dataManagementUiState: StateFlow<DataManagementUiState> = _dataManagementUiState.asStateFlow()

    private val filteredRecords: StateFlow<List<Record>> = combine(
        records,
        _recordFilters
    ) { recordItems, filters ->
        recordItems.filter { record ->
            val noteMatches = filters.keyword.isBlank() ||
                record.note.orEmpty().contains(filters.keyword, ignoreCase = true)
            val typeMatches = filters.type == null || record.type == filters.type
            val tagMatches = filters.tag.isNullOrBlank() || record.tag == filters.tag
            val accountMatches = filters.accountId == null || record.accountId == filters.accountId
            val startMatches = filters.startDateMillis == null || record.timestamp >= filters.startDateMillis
            val endMatches = filters.endDateMillis == null || record.timestamp < dayEndExclusive(filters.endDateMillis)
            val minAmount = filters.minAmountInput.toDoubleOrNull()
            val maxAmount = filters.maxAmountInput.toDoubleOrNull()
            val minMatches = minAmount == null || record.amount >= minAmount
            val maxMatches = maxAmount == null || record.amount <= maxAmount

            noteMatches && typeMatches && tagMatches && accountMatches &&
                startMatches && endMatches && minMatches && maxMatches
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private data class RecordListInputs(
        val filteredRecords: List<Record>,
        val filters: RecordFilterUiState,
        val accounts: List<Account>,
        val budgets: List<Budget>,
        val rules: List<RecurringRule>
    )

    val recordListUiState: StateFlow<RecordListUiState> = combine(
        combine(
            filteredRecords,
            _recordFilters,
            activeAccounts,
            budgets,
            recurringRules
        ) { listRecords, filters, accountItems, budgetItems, rules ->
            RecordListInputs(
                filteredRecords = listRecords,
                filters = filters,
                accounts = accountItems,
                budgets = budgetItems,
                rules = rules
            )
        },
        records
    ) { inputs, allRecords ->
        RecordListUiState(
            records = inputs.filteredRecords,
            filters = inputs.filters,
            accounts = inputs.accounts,
            availableTags = availableTagsForFilters(inputs.filters.type),
            budgetStatuses = calculateBudgetStatuses(inputs.budgets, allRecords),
            dueRecurringRules = inputs.rules.filter {
                it.isActive && it.nextDueDate <= today.format(backupDayFormatter)
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordListUiState(
            records = emptyList(),
            filters = RecordFilterUiState(),
            accounts = emptyList(),
            availableTags = availableTagsForFilters(null),
            budgetStatuses = emptyList(),
            dueRecurringRules = emptyList()
        )
    )

    private data class CalendarInputs(
        val yearMonth: YearMonth,
        val records: List<Record>,
        val accounts: List<Account>,
        val availableYears: List<Int>,
        val selectedAccountId: Long?
    )

    val calendarUiState: StateFlow<CalendarUiState> = combine(
        combine(
            _currentMonth,
            records,
            activeAccounts,
            availableYears,
            _calendarAccountId
        ) { yearMonth, recordItems, accountItems, years, selectedAccountId ->
            CalendarInputs(
                yearMonth = yearMonth,
                records = recordItems,
                accounts = accountItems,
                availableYears = years,
                selectedAccountId = selectedAccountId
            )
        },
        _selectedCalendarDate
    ) { inputs, requestedDate ->
        val scopedRecords = inputs.records.filterByAccount(inputs.selectedAccountId)
        val selectedDate = requestedDate
            .takeIf { YearMonth.from(it) == inputs.yearMonth }
            ?: defaultSelectedDateForMonth(inputs.yearMonth)
        val selectedDayRecords = scopedRecords
            .filter { record -> record.localDate(zoneId) == selectedDate }
            .sortedByDescending { it.timestamp }

        CalendarUiState(
            yearMonth = inputs.yearMonth,
            dailySummary = scopedRecords.toDailySummaryForMonth(inputs.yearMonth, zoneId),
            selectedDate = selectedDate,
            selectedDayTotals = selectedDayRecords.summaryTotals(),
            selectedDayRecords = selectedDayRecords,
            availableYears = inputs.availableYears,
            accounts = inputs.accounts,
            selectedAccountId = inputs.selectedAccountId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState(
            yearMonth = initialMonth,
            dailySummary = emptyList(),
            selectedDate = today,
            selectedDayTotals = SummaryTotals(),
            selectedDayRecords = emptyList(),
            availableYears = listOf(initialYear),
            accounts = emptyList(),
            selectedAccountId = null
        )
    )

    private data class StatsInputs(
        val records: List<Record>,
        val accounts: List<Account>,
        val period: StatsPeriod,
        val recordType: RecordType,
        val selectedDay: LocalDate,
        val selectedMonth: YearMonth,
        val selectedYear: Int,
        val availableYears: List<Int>,
        val selectedAccountId: Long?
    )

    val statsUiState: StateFlow<StatsUiState> = combine(
        combine(
            records,
            activeAccounts,
            _statsPeriod,
            _statsRecordType,
            _statsSelectedDay
        ) { recordItems, accountItems, period, recordType, selectedDay ->
            arrayOf(recordItems, accountItems, period, recordType, selectedDay)
        },
        combine(
            _statsSelectedMonth,
            _statsSelectedYear,
            availableYears,
            _statsAccountId
        ) { selectedMonth, selectedYear, years, selectedAccountId ->
            arrayOf(selectedMonth, selectedYear, years, selectedAccountId)
        }
    ) { first, second ->
        val inputs = StatsInputs(
            records = first[0] as List<Record>,
            accounts = first[1] as List<Account>,
            period = first[2] as StatsPeriod,
            recordType = first[3] as RecordType,
            selectedDay = first[4] as LocalDate,
            selectedMonth = second[0] as YearMonth,
            selectedYear = second[1] as Int,
            availableYears = second[2] as List<Int>,
            selectedAccountId = second[3] as Long?
        )

        val scopedRecords = inputs.records.filterByAccount(inputs.selectedAccountId)
        val filteredForPeriod = when (inputs.period) {
            StatsPeriod.DAY -> scopedRecords.filter { record ->
                record.localDate(zoneId) == inputs.selectedDay
            }
            StatsPeriod.MONTH -> scopedRecords.filter { record ->
                val date = record.localDate(zoneId)
                date.year == inputs.selectedMonth.year &&
                    date.monthValue == inputs.selectedMonth.monthValue
            }
            StatsPeriod.YEAR -> scopedRecords.filter { record ->
                record.localDate(zoneId).year == inputs.selectedYear
            }
        }

        val breakdown = when (inputs.period) {
            StatsPeriod.DAY -> scopedRecords.categoryBreakdownForDay(
                inputs.selectedDay,
                inputs.recordType,
                zoneId
            )
            StatsPeriod.MONTH -> scopedRecords.categoryBreakdownForMonth(
                inputs.selectedMonth,
                inputs.recordType,
                zoneId
            )
            StatsPeriod.YEAR -> scopedRecords.categoryBreakdownForYear(
                inputs.selectedYear,
                inputs.recordType,
                zoneId
            )
        }

        StatsUiState(
            period = inputs.period,
            recordType = inputs.recordType,
            selectedDay = inputs.selectedDay,
            selectedMonth = inputs.selectedMonth,
            selectedYear = inputs.selectedYear,
            totals = filteredForPeriod.summaryTotals(),
            breakdown = breakdown,
            availableYears = inputs.availableYears,
            accounts = inputs.accounts,
            selectedAccountId = inputs.selectedAccountId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(
            period = StatsPeriod.MONTH,
            recordType = RecordType.EXPENSE,
            selectedDay = today,
            selectedMonth = initialMonth,
            selectedYear = initialYear,
            totals = SummaryTotals(),
            breakdown = emptyList(),
            availableYears = listOf(initialYear),
            accounts = emptyList(),
            selectedAccountId = null
        )
    )

    init {
        if (_addRecordState.value.selectedAccountId == 1L && activeAccounts.value.isNotEmpty()) {
            startCreateRecord()
        }
    }

    fun startCreateRecord() {
        _addRecordState.value = AddRecordUiState(
            selectedTag = TagDataSource.defaultTagFor(RecordType.EXPENSE),
            selectedDateMillis = System.currentTimeMillis(),
            selectedAccountId = activeAccounts.value.firstOrNull()?.id ?: 1L
        )
    }

    fun startEditRecord(record: Record) {
        _addRecordState.value = AddRecordUiState(
            editingRecordId = record.id,
            type = record.type,
            amountInput = if (record.amount == 0.0) "" else record.amount.toString(),
            selectedTag = record.tag,
            noteInput = record.note.orEmpty(),
            selectedDateMillis = record.timestamp,
            selectedAccountId = record.accountId
        )
    }

    fun startRecurringReminderEntry(rule: RecurringRule) {
        _addRecordState.value = AddRecordUiState(
            sourceRecurringRuleId = rule.id,
            type = rule.type,
            amountInput = rule.amount.toString(),
            selectedTag = if (TagDataSource.isValidTag(rule.type, rule.tag)) rule.tag else TagDataSource.defaultTagFor(rule.type),
            noteInput = rule.note.orEmpty(),
            selectedDateMillis = System.currentTimeMillis(),
            selectedAccountId = rule.accountId
        )
    }

    fun previousMonth() {
        setCalendarMonth(_currentMonth.value.minusMonths(1))
    }

    fun nextMonth() {
        setCalendarMonth(_currentMonth.value.plusMonths(1))
    }

    fun jumpToMonth(yearMonth: YearMonth) {
        setCalendarMonth(yearMonth)
    }

    fun onCalendarDateSelected(dateMillis: Long) {
        val selectedDate = Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate()
        if (YearMonth.from(selectedDate) == _currentMonth.value) {
            _selectedCalendarDate.value = selectedDate
        }
    }

    fun observeRecordsForDay(selectedDayMillis: Long, accountId: Long?): Flow<List<Record>> {
        val selectedDate = Instant.ofEpochMilli(selectedDayMillis).atZone(zoneId).toLocalDate()
        return records.map { items ->
            items.filter { record ->
                record.localDate(zoneId) == selectedDate &&
                    (accountId == null || record.accountId == accountId)
            }
        }
    }

    fun onTypeSelected(type: RecordType) {
        val currentTag = _addRecordState.value.selectedTag
        val nextTag = if (TagDataSource.isValidTag(type, currentTag)) currentTag else TagDataSource.defaultTagFor(type)
        _addRecordState.value = _addRecordState.value.copy(
            type = type,
            selectedTag = nextTag,
            errorMessage = null
        )
    }

    fun onAmountChanged(amount: String) {
        _addRecordState.value = _addRecordState.value.copy(amountInput = amount, errorMessage = null)
    }

    fun onTagSelected(tag: String) {
        _addRecordState.value = _addRecordState.value.copy(selectedTag = tag, errorMessage = null)
    }

    fun onNoteChanged(note: String) {
        _addRecordState.value = _addRecordState.value.copy(noteInput = note, errorMessage = null)
    }

    fun onDateChanged(dateMillis: Long) {
        _addRecordState.value = _addRecordState.value.copy(selectedDateMillis = dateMillis, errorMessage = null)
    }

    fun onRecordAccountSelected(accountId: Long) {
        _addRecordState.value = _addRecordState.value.copy(selectedAccountId = accountId, errorMessage = null)
    }

    fun saveRecord(onSuccess: () -> Unit = {}) {
        val state = _addRecordState.value
        val amount = state.amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _addRecordState.value = state.copy(errorMessage = "请输入大于 0 的有效金额")
            return
        }
        if (state.selectedTag.isBlank()) {
            _addRecordState.value = state.copy(errorMessage = "请选择标签")
            return
        }
        if (!TagDataSource.isValidTag(state.type, state.selectedTag)) {
            _addRecordState.value = state.copy(errorMessage = "当前收支类型与标签不匹配")
            return
        }
        if (activeAccounts.value.none { it.id == state.selectedAccountId }) {
            _addRecordState.value = state.copy(errorMessage = "请选择有效账户")
            return
        }

        viewModelScope.launch {
            val record = Record(
                id = state.editingRecordId ?: 0L,
                type = state.type,
                amount = amount,
                tag = state.selectedTag,
                note = state.noteInput.trim().ifBlank { null },
                timestamp = state.selectedDateMillis,
                accountId = state.selectedAccountId
            )
            if (state.isEditing) {
                repository.updateRecord(record)
            } else {
                repository.insertRecord(record)
            }
            advanceRecurringRuleIfNeeded(state.sourceRecurringRuleId, state.selectedDateMillis)
            startCreateRecord()
            onSuccess()
        }
    }

    fun deleteCurrentRecord(onSuccess: () -> Unit = {}) {
        val recordId = _addRecordState.value.editingRecordId ?: return
        viewModelScope.launch {
            repository.deleteRecord(recordId)
            startCreateRecord()
            onSuccess()
        }
    }

    fun updateFilterKeyword(value: String) {
        _recordFilters.value = _recordFilters.value.copy(keyword = value)
    }

    fun updateFilterType(value: RecordType?) {
        val nextTag = _recordFilters.value.tag.takeIf { currentTag ->
            currentTag != null && value != null && TagDataSource.isValidTag(value, currentTag)
        }
        _recordFilters.value = _recordFilters.value.copy(type = value, tag = nextTag)
    }

    fun updateFilterTag(value: String?) {
        _recordFilters.value = _recordFilters.value.copy(tag = value)
    }

    fun updateFilterAccount(value: Long?) {
        _recordFilters.value = _recordFilters.value.copy(accountId = value)
    }

    fun updateFilterStartDate(value: Long?) {
        _recordFilters.value = _recordFilters.value.copy(startDateMillis = value)
    }

    fun updateFilterEndDate(value: Long?) {
        _recordFilters.value = _recordFilters.value.copy(endDateMillis = value)
    }

    fun updateFilterMinAmount(value: String) {
        _recordFilters.value = _recordFilters.value.copy(minAmountInput = value)
    }

    fun updateFilterMaxAmount(value: String) {
        _recordFilters.value = _recordFilters.value.copy(maxAmountInput = value)
    }

    fun clearFilters() {
        _recordFilters.value = RecordFilterUiState()
    }

    fun onCalendarAccountSelected(accountId: Long?) {
        _calendarAccountId.value = accountId
    }

    fun onStatsAccountSelected(accountId: Long?) {
        _statsAccountId.value = accountId
    }

    fun onStatsPeriodSelected(period: StatsPeriod) {
        _statsPeriod.value = period
    }

    fun onStatsRecordTypeSelected(recordType: RecordType) {
        _statsRecordType.value = recordType
    }

    fun onStatsDaySelected(dateMillis: Long) {
        _statsSelectedDay.value = Instant.ofEpochMilli(dateMillis).atZone(zoneId).toLocalDate()
    }

    fun onStatsMonthSelected(yearMonth: YearMonth) {
        _statsSelectedMonth.value = yearMonth
    }

    fun onStatsYearSelected(year: Int) {
        _statsSelectedYear.value = year
    }

    fun addAccount(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val nextSortOrder = (accounts.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repository.insertAccount(
                Account(
                    name = trimmed,
                    type = AccountType.CUSTOM,
                    sortOrder = nextSortOrder
                )
            )
        }
    }

    fun renameAccount(account: Account, name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            repository.updateAccount(account.copy(name = trimmed))
        }
    }

    fun toggleAccountArchived(account: Account) {
        if (account.isSystem) return
        viewModelScope.launch {
            repository.updateAccount(account.copy(isArchived = !account.isArchived))
        }
    }

    fun moveAccount(accountId: Long, direction: Int) {
        val list = accounts.value.sortedBy { it.sortOrder }.toMutableList()
        val index = list.indexOfFirst { it.id == accountId }
        if (index == -1) return
        val targetIndex = index + direction
        if (targetIndex !in list.indices) return
        val current = list[index]
        val target = list[targetIndex]
        viewModelScope.launch {
            repository.updateAccount(current.copy(sortOrder = target.sortOrder))
            repository.updateAccount(target.copy(sortOrder = current.sortOrder))
        }
    }

    fun saveBudget(budget: Budget) {
        if (budget.amountLimit <= 0) return
        viewModelScope.launch {
            if (budget.id == 0L) {
                repository.insertBudget(budget)
            } else {
                repository.updateBudget(budget)
            }
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            repository.deleteBudget(budgetId)
        }
    }

    fun saveRecurringRule(rule: RecurringRule) {
        if (rule.amount <= 0 || rule.title.isBlank()) return
        viewModelScope.launch {
            if (rule.id == 0L) {
                repository.insertRecurringRule(rule)
            } else {
                repository.updateRecurringRule(rule)
            }
        }
    }

    fun deleteRecurringRule(ruleId: Long) {
        viewModelScope.launch {
            repository.deleteRecurringRule(ruleId)
        }
    }

    fun toggleRecurringRuleActive(rule: RecurringRule) {
        viewModelScope.launch {
            repository.updateRecurringRule(rule.copy(isActive = !rule.isActive))
        }
    }

    fun exportBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            runCatching {
                val snapshot = repository.exportSnapshot()
                requireNotNull(backupManager) { "备份管理器未初始化" }
                    .exportEncrypted(uri, password, snapshot)
            }.onSuccess {
                _dataManagementUiState.value = DataManagementUiState(statusMessage = "备份导出成功")
            }.onFailure { error ->
                _dataManagementUiState.value = DataManagementUiState(errorMessage = error.message ?: "备份导出失败")
            }
        }
    }

    fun importBackup(uri: Uri, password: String) {
        viewModelScope.launch {
            runCatching {
                val snapshot = requireNotNull(backupManager) { "备份管理器未初始化" }
                    .importEncrypted(uri, password)
                repository.importSnapshot(snapshot)
            }.onSuccess {
                startCreateRecord()
                clearFilters()
                _dataManagementUiState.value = DataManagementUiState(statusMessage = "备份导入成功")
            }.onFailure { error ->
                _dataManagementUiState.value = DataManagementUiState(errorMessage = error.message ?: "备份导入失败")
            }
        }
    }

    fun clearDataManagementMessage() {
        _dataManagementUiState.value = DataManagementUiState()
    }

    fun defaultBudgetMonth(): String = YearMonth.now(zoneId).format(backupMonthFormatter)

    fun todayDateString(): String = today.format(backupDayFormatter)

    private suspend fun advanceRecurringRuleIfNeeded(ruleId: Long?, savedMillis: Long) {
        if (ruleId == null) return
        val current = recurringRules.value.firstOrNull { it.id == ruleId } ?: return
        val savedDate = Instant.ofEpochMilli(savedMillis).atZone(zoneId).toLocalDate()
        var nextDate = LocalDate.parse(current.nextDueDate)
        while (!nextDate.isAfter(savedDate)) {
            nextDate = when (current.frequency) {
                RecurringFrequency.WEEKLY -> nextDate.plusWeeks(current.intervalCount.toLong())
                RecurringFrequency.MONTHLY -> nextDate.plusMonths(current.intervalCount.toLong())
                RecurringFrequency.YEARLY -> nextDate.plusYears(current.intervalCount.toLong())
            }
        }
        repository.updateRecurringRule(current.copy(nextDueDate = nextDate.format(backupDayFormatter)))
    }

    private fun calculateBudgetStatuses(budgetItems: List<Budget>, recordItems: List<Record>): List<BudgetStatus> {
        val currentMonthKey = YearMonth.now(zoneId).format(backupMonthFormatter)
        val currentMonthExpenses = recordItems.filter { record ->
            record.type == RecordType.EXPENSE &&
                record.localDate(zoneId).format(backupMonthFormatter) == currentMonthKey
        }
        return budgetItems
            .filter { it.yearMonth == currentMonthKey }
            .sortedWith(compareBy<Budget> { it.scope.name }.thenBy { it.tag.orEmpty() })
            .map { budget ->
                val spent = when (budget.scope) {
                    BudgetScope.TOTAL -> currentMonthExpenses.sumOf { it.amount }
                    BudgetScope.CATEGORY -> currentMonthExpenses
                        .filter { it.tag == budget.tag }
                        .sumOf { it.amount }
                }
                BudgetStatus(
                    budget = budget,
                    spent = spent,
                    remaining = budget.amountLimit - spent,
                    isOverBudget = spent > budget.amountLimit
                )
            }
    }

    private fun availableTagsForFilters(type: RecordType?): List<String> {
        return when (type) {
            RecordType.EXPENSE -> TagDataSource.expenseTags
            RecordType.INCOME -> TagDataSource.incomeTags
            null -> (TagDataSource.expenseTags + TagDataSource.incomeTags).distinct()
        }
    }

    private fun setCalendarMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
        _selectedCalendarDate.value = defaultSelectedDateForMonth(yearMonth)
    }

    private fun defaultSelectedDateForMonth(yearMonth: YearMonth): LocalDate {
        return if (yearMonth == YearMonth.from(today)) today else yearMonth.atDay(1)
    }

    private fun dayEndExclusive(dayStartMillis: Long): Long {
        return Instant.ofEpochMilli(dayStartMillis)
            .atZone(zoneId)
            .toLocalDate()
            .plusDays(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
    }

    private fun List<Record>.filterByAccount(accountId: Long?): List<Record> {
        return if (accountId == null) this else filter { it.accountId == accountId }
    }

    private fun List<Record>.toDailySummaryForMonth(yearMonth: YearMonth, zoneId: ZoneId): List<DailySummary> {
        return filter { record ->
            val date = record.localDate(zoneId)
            date.year == yearMonth.year && date.monthValue == yearMonth.monthValue
        }.groupBy { record ->
            record.localDate(zoneId)
        }.map { (date, dayRecords) ->
            DailySummary(
                day = date.format(backupDayFormatter),
                totalIncome = dayRecords.filter { it.type == RecordType.INCOME }.sumOf { it.amount },
                totalExpense = dayRecords.filter { it.type == RecordType.EXPENSE }.sumOf { it.amount }
            )
        }.sortedBy { it.day }
    }

    private fun Record.localDate(zoneId: ZoneId): LocalDate {
        return Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
    }
}
