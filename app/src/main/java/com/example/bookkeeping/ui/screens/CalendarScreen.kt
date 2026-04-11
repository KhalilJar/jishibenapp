package com.example.bookkeeping.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.R
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.SummaryTotals
import com.example.bookkeeping.ui.components.CalendarMonthGrid
import com.example.bookkeeping.ui.components.MonthYearPickerDialog
import com.example.bookkeeping.ui.main.CalendarUiState
import com.example.bookkeeping.ui.util.formatAmount
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")
private val selectedDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

@Composable
fun CalendarScreen(
    state: CalendarUiState,
    contentPadding: PaddingValues,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToMonth: (java.time.YearMonth) -> Unit,
    onAccountSelected: (Long?) -> Unit,
    onDayClick: (Long) -> Unit,
    onViewDayDetails: (Long) -> Unit,
    onRecordClick: (Record) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)
    var showMonthPicker by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    val weekdays = listOf(
        stringResource(R.string.weekday_mon),
        stringResource(R.string.weekday_tue),
        stringResource(R.string.weekday_wed),
        stringResource(R.string.weekday_thu),
        stringResource(R.string.weekday_fri),
        stringResource(R.string.weekday_sat),
        stringResource(R.string.weekday_sun)
    )
    val summaryByDate = remember(state.dailySummary) {
        state.dailySummary.mapNotNull { item ->
            runCatching { LocalDate.parse(item.day) to item }.getOrNull()
        }.toMap()
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialYearMonth = state.yearMonth,
            availableYears = state.availableYears,
            onDismiss = { showMonthPicker = false },
            onConfirm = {
                onJumpToMonth(it)
                showMonthPicker = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = contentPadding.calculateTopPadding() + 10.dp,
            end = 12.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = stringResource(R.string.calendar_prev_month))
                }
                TextButton(onClick = { showMonthPicker = true }) {
                    Text(text = state.yearMonth.format(monthFormatter), style = MaterialTheme.typography.titleMedium)
                }
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = stringResource(R.string.calendar_next_month))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.calendar_account_label,
                        state.accounts.firstOrNull { it.id == state.selectedAccountId }?.name
                            ?: stringResource(R.string.calendar_all_accounts)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(onClick = { accountExpanded = true }) {
                    Text(stringResource(R.string.calendar_switch_account))
                }
                DropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.calendar_all_accounts)) }, onClick = {
                        onAccountSelected(null)
                        accountExpanded = false
                    })
                    state.accounts.forEach { account ->
                        DropdownMenuItem(text = { Text(account.name) }, onClick = {
                            onAccountSelected(account.id)
                            accountExpanded = false
                        })
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(R.string.calendar_jump_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            CalendarMonthGrid(
                yearMonth = state.yearMonth,
                weekdays = weekdays,
                summaryByDate = summaryByDate,
                selectedDate = state.selectedDate,
                today = today,
                onDayClick = { date ->
                    onDayClick(date.atStartOfDay(zoneId).toInstant().toEpochMilli())
                }
            )
        }

        item {
            AnimatedContent(
                targetState = state.selectedDate,
                modifier = Modifier.fillMaxWidth(),
                label = "calendar-detail-panel"
            ) {
                CalendarDetailPanel(
                    selectedDate = state.selectedDate,
                    totals = state.selectedDayTotals,
                    records = state.selectedDayRecords,
                    onViewDayDetails = {
                        onViewDayDetails(state.selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli())
                    },
                    onRecordClick = onRecordClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CalendarDetailPanel(
    selectedDate: LocalDate,
    totals: SummaryTotals,
    records: List<Record>,
    onViewDayDetails: () -> Unit,
    onRecordClick: (Record) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.calendar_detail_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = selectedDate.format(selectedDayFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onViewDayDetails) {
                    Text(stringResource(R.string.calendar_view_day_details))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailAmountCard(
                    title = stringResource(R.string.calendar_income_total),
                    amountText = "+${formatAmount(totals.income)}",
                    amountColor = Color(0xFF2E7D32),
                    containerColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                )
                DetailAmountCard(
                    title = stringResource(R.string.calendar_expense_total),
                    amountText = "-${formatAmount(totals.expense)}",
                    amountColor = Color(0xFFC62828),
                    containerColor = Color(0xFFFFEBEE),
                    modifier = Modifier.weight(1f)
                )
                DetailAmountCard(
                    title = stringResource(R.string.calendar_net_total),
                    amountText = "${if (totals.net >= 0) "+" else "-"}${formatAmount(kotlin.math.abs(totals.net))}",
                    amountColor = if (totals.net >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                    containerColor = if (totals.net >= 0) Color(0xFFE3F2FD) else Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = stringResource(R.string.calendar_day_records_title),
                style = MaterialTheme.typography.titleSmall
            )

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.calendar_day_records_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    records.forEach { record ->
                        val amountColor = if (record.type == RecordType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)
                        val amountPrefix = if (record.type == RecordType.INCOME) "+" else "-"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRecordClick(record) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = record.tag,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$amountPrefix${formatAmount(record.amount)}",
                                color = amountColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailAmountCard(
    title: String,
    amountText: String,
    amountColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Text(
                text = amountText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = amountColor,
                maxLines = 1
            )
        }
    }
}
