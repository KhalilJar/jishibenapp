package com.example.bookkeeping.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.RecurringRule
import com.example.bookkeeping.ui.main.RecordListUiState
import com.example.bookkeeping.ui.util.formatAmount
import com.example.bookkeeping.ui.util.formatDateTime
import com.example.bookkeeping.ui.util.formatDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    state: RecordListUiState,
    contentPadding: PaddingValues,
    onRecordClick: (Record) -> Unit,
    onKeywordChanged: (String) -> Unit,
    onTypeChanged: (RecordType?) -> Unit,
    onTagChanged: (String?) -> Unit,
    onAccountChanged: (Long?) -> Unit,
    onStartDateChanged: (Long?) -> Unit,
    onEndDateChanged: (Long?) -> Unit,
    onMinAmountChanged: (String) -> Unit,
    onMaxAmountChanged: (String) -> Unit,
    onClearFilters: () -> Unit,
    onCreateFromRecurring: (RecurringRule) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilters by remember { mutableStateOf(false) }
    var tagExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val accountNameMap = remember(state.accounts) { state.accounts.associateBy({ it.id }, { it.name }) }

    if (showStartDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.filters.startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onStartDateChanged(pickerState.selectedDateMillis)
                    showStartDatePicker = false
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("取消") }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showEndDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.filters.endDateMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onEndDateChanged(pickerState.selectedDateMillis)
                    showEndDatePicker = false
                }) { Text("确认") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("取消") }
            }
        ) { DatePicker(state = pickerState) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("筛选器", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { showFilters = !showFilters }) {
                            Text(if (showFilters) "收起" else "展开")
                        }
                    }

                    if (showFilters) {
                        OutlinedTextField(
                            value = state.filters.keyword,
                            onValueChange = onKeywordChanged,
                            label = { Text("备注关键字") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = state.filters.type == null,
                                onClick = { onTypeChanged(null) },
                                label = { Text("全部") }
                            )
                            FilterChip(
                                selected = state.filters.type == RecordType.EXPENSE,
                                onClick = { onTypeChanged(RecordType.EXPENSE) },
                                label = { Text("支出") }
                            )
                            FilterChip(
                                selected = state.filters.type == RecordType.INCOME,
                                onClick = { onTypeChanged(RecordType.INCOME) },
                                label = { Text("收入") }
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = tagExpanded,
                            onExpandedChange = { tagExpanded = !tagExpanded }
                        ) {
                            OutlinedTextField(
                                value = state.filters.tag ?: "全部标签",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("标签") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = tagExpanded,
                                onDismissRequest = { tagExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("全部标签") },
                                    onClick = {
                                        onTagChanged(null)
                                        tagExpanded = false
                                    }
                                )
                                state.availableTags.forEach { tag ->
                                    DropdownMenuItem(
                                        text = { Text(tag) },
                                        onClick = {
                                            onTagChanged(tag)
                                            tagExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = accountExpanded,
                            onExpandedChange = { accountExpanded = !accountExpanded }
                        ) {
                            OutlinedTextField(
                                value = state.accounts.firstOrNull { it.id == state.filters.accountId }?.name ?: "全部账户",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("账户") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = accountExpanded,
                                onDismissRequest = { accountExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("全部账户") },
                                    onClick = {
                                        onAccountChanged(null)
                                        accountExpanded = false
                                    }
                                )
                                state.accounts.forEach { account ->
                                    DropdownMenuItem(
                                        text = { Text(account.name) },
                                        onClick = {
                                            onAccountChanged(account.id)
                                            accountExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showStartDatePicker = true }) {
                                Text(state.filters.startDateMillis?.let { "开始：${formatDay(it)}" } ?: "开始日期")
                            }
                            TextButton(onClick = { showEndDatePicker = true }) {
                                Text(state.filters.endDateMillis?.let { "结束：${formatDay(it)}" } ?: "结束日期")
                            }
                        }

                        OutlinedTextField(
                            value = state.filters.minAmountInput,
                            onValueChange = onMinAmountChanged,
                            label = { Text("最小金额") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = state.filters.maxAmountInput,
                            onValueChange = onMaxAmountChanged,
                            label = { Text("最大金额") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        TextButton(onClick = onClearFilters) {
                            Text("清空筛选")
                        }
                    }
                }
            }
        }

        if (state.budgetStatuses.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("本月预算提醒", style = MaterialTheme.typography.titleMedium)
                        state.budgetStatuses.forEach { status ->
                            val label = if (status.budget.scope.name == "TOTAL") {
                                "本月总支出预算"
                            } else {
                                "${status.budget.tag} 预算"
                            }
                            Text(
                                text = "$label：已用 ${formatAmount(status.spent)} / ${formatAmount(status.budget.amountLimit)}${if (status.isOverBudget) "（已超额）" else ""}",
                                color = if (status.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (state.dueRecurringRules.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("待提醒固定收支", style = MaterialTheme.typography.titleMedium)
                        state.dueRecurringRules.forEach { rule ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(rule.title)
                                    Text(
                                        text = "${rule.frequency.displayName} · ${rule.nextDueDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { onCreateFromRecurring(rule) }) {
                                    Text("记一笔")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.records.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("当前条件下还没有记录。", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else {
            items(state.records, key = { it.id }) { record ->
                val amountColor = if (record.type == RecordType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)
                val amountPrefix = if (record.type == RecordType.INCOME) "+" else "-"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecordClick(record) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "${record.type.displayName} · ${record.tag}")
                            Text(text = "$amountPrefix${formatAmount(record.amount)}", color = amountColor)
                        }
                        Text(text = "账户：${accountNameMap[record.accountId] ?: "未知账户"}")
                        Text(text = "时间：${formatDateTime(record.timestamp)}")
                        if (!record.note.isNullOrBlank()) {
                            Text(text = "备注：${record.note}")
                        }
                    }
                }
            }
        }
    }
}
