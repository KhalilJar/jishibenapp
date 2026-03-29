package com.example.bookkeeping.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookkeeping.data.TagDataSource
import com.example.bookkeeping.data.mapper.toDomain
import com.example.bookkeeping.data.model.DailySummary
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.repository.RecordRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: RecordRepository
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    val tags: List<String> = TagDataSource.defaultTags

    val records = repository.observeRecords()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val dailySummary: StateFlow<List<DailySummary>> = repository.observeDailySummary()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _currentMonth = MutableStateFlow(YearMonth.now(zoneId))
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    val calendarDailySummary: StateFlow<List<DailySummary>> = _currentMonth
        .flatMapLatest { yearMonth ->
            repository.observeDailySummaryInRange(
                startMillis = monthStartMillis(yearMonth),
                endMillis = monthEndMillisExclusive(yearMonth)
            )
        }
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _addRecordState = MutableStateFlow(AddRecordUiState())
    val addRecordState: StateFlow<AddRecordUiState> = _addRecordState.asStateFlow()

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun dayStartMillis(date: LocalDate): Long {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun observeRecordsForDay(selectedDayMillis: Long): Flow<List<Record>> {
        val localDate = Instant.ofEpochMilli(selectedDayMillis)
            .atZone(zoneId)
            .toLocalDate()
        val startMillis = dayStartMillis(localDate)
        val endMillis = dayStartMillis(localDate.plusDays(1))

        return repository.observeRecordsInRange(startMillis, endMillis)
            .map { entities -> entities.map { it.toDomain() } }
    }

    fun onTypeSelected(type: RecordType) {
        _addRecordState.value = _addRecordState.value.copy(type = type, errorMessage = null)
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

    fun saveRecord() {
        val state = _addRecordState.value
        val amount = state.amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _addRecordState.value = state.copy(errorMessage = "Please input a valid amount greater than 0")
            return
        }
        if (state.selectedTag.isBlank()) {
            _addRecordState.value = state.copy(errorMessage = "Please select a tag")
            return
        }

        viewModelScope.launch {
            repository.insertRecord(
                Record(
                    type = state.type,
                    amount = amount,
                    tag = state.selectedTag,
                    note = state.noteInput.trim().ifBlank { null },
                    timestamp = state.selectedDateMillis
                )
            )
            _addRecordState.value = AddRecordUiState(
                selectedTag = tags.first(),
                selectedDateMillis = System.currentTimeMillis()
            )
        }
    }

    private fun monthStartMillis(yearMonth: YearMonth): Long {
        return yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    private fun monthEndMillisExclusive(yearMonth: YearMonth): Long {
        return yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    }
}
