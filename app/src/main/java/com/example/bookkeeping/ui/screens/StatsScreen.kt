package com.example.bookkeeping.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.StatsPeriod
import com.example.bookkeeping.ui.components.CategoryPieChart
import com.example.bookkeeping.ui.components.MonthYearPickerDialog
import com.example.bookkeeping.ui.components.YearPickerDialog
import com.example.bookkeeping.ui.main.StatsUiState
import com.example.bookkeeping.ui.util.formatAmount
import com.example.bookkeeping.ui.util.formatDay
import java.time.ZoneId
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val statsMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    state: StatsUiState,
    contentPadding: PaddingValues,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onRecordTypeSelected: (RecordType) -> Unit,
    onDaySelected: (Long) -> Unit,
    onMonthSelected: (YearMonth) -> Unit,
    onYearSelected: (Int) -> Unit,
    onAccountSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDayDialog by remember { mutableStateOf(false) }
    var showMonthDialog by remember { mutableStateOf(false) }
    var showYearDialog by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }

    if (showDayDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDayDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDaySelected(datePickerState.selectedDateMillis ?: state.selectedDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    showDayDialog = false
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showDayDialog = false }) { Text("取消") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showMonthDialog) {
        MonthYearPickerDialog(
            initialYearMonth = state.selectedMonth,
            availableYears = state.availableYears,
            onDismiss = { showMonthDialog = false },
            onConfirm = {
                onMonthSelected(it)
                showMonthDialog = false
            }
        )
    }

    if (showYearDialog) {
        YearPickerDialog(
            initialYear = state.selectedYear,
            availableYears = state.availableYears,
            onDismiss = { showYearDialog = false },
            onConfirm = {
                onYearSelected(it)
                showYearDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = "统计维度", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatsPeriod.entries.forEach { period ->
                            FilterChip(
                                selected = state.period == period,
                                onClick = { onPeriodSelected(period) },
                                label = {
                                    Text(
                                        when (period) {
                                            StatsPeriod.DAY -> "按天"
                                            StatsPeriod.MONTH -> "按月"
                                            StatsPeriod.YEAR -> "按年"
                                        }
                                    )
                                }
                            )
                        }
                    }

                    Text(text = "收支类型", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.recordType == RecordType.EXPENSE,
                            onClick = { onRecordTypeSelected(RecordType.EXPENSE) },
                            label = { Text("支出") }
                        )
                        FilterChip(
                            selected = state.recordType == RecordType.INCOME,
                            onClick = { onRecordTypeSelected(RecordType.INCOME) },
                            label = { Text("收入") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = currentPeriodLabel(state), style = MaterialTheme.typography.bodyLarge)
                        TextButton(onClick = {
                            when (state.period) {
                                StatsPeriod.DAY -> showDayDialog = true
                                StatsPeriod.MONTH -> showMonthDialog = true
                                StatsPeriod.YEAR -> showYearDialog = true
                            }
                        }) {
                            Text(
                                when (state.period) {
                                    StatsPeriod.DAY -> "切换日期"
                                    StatsPeriod.MONTH -> "切换年月"
                                    StatsPeriod.YEAR -> "切换年份"
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "账户：${state.accounts.firstOrNull { it.id == state.selectedAccountId }?.name ?: "全部账户"}")
                        TextButton(onClick = { accountExpanded = true }) {
                            Text("切换账户")
                        }
                        DropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) {
                            DropdownMenuItem(text = { Text("全部账户") }, onClick = {
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
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "汇总", style = MaterialTheme.typography.titleMedium)
                    StatsAmountRow(label = "收入", amount = "+${formatAmount(state.totals.income)}", color = Color(0xFF2E7D32))
                    StatsAmountRow(label = "支出", amount = "-${formatAmount(state.totals.expense)}", color = Color(0xFFC62828))
                    StatsAmountRow(
                        label = "净额",
                        amount = "${if (state.totals.net >= 0) "+" else "-"}${formatAmount(kotlin.math.abs(state.totals.net))}",
                        color = if (state.totals.net >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${if (state.recordType == RecordType.EXPENSE) "支出" else "收入"}标签占比",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "扇形图展示当前维度下每个标签对应的金额占比。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                    CategoryPieChart(items = state.breakdown)
                }
            }
        }

        if (state.breakdown.isEmpty()) {
            item {
                Text(
                    text = "这个筛选条件下还没有记录，换一个日期或切换收入/支出试试看。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(state.breakdown, key = { it.tag }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = item.tag)
                        Text(text = "${formatAmount(item.amount)} / ${(item.percentage * 100).toInt()}%")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsAmountRow(label: String, amount: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = amount, color = color)
    }
}

private fun currentPeriodLabel(state: StatsUiState): String {
    return when (state.period) {
        StatsPeriod.DAY -> "当前日期：${formatDay(state.selectedDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())}"
        StatsPeriod.MONTH -> "当前月份：${state.selectedMonth.format(statsMonthFormatter)}"
        StatsPeriod.YEAR -> "当前年份：${state.selectedYear}年"
    }
}
