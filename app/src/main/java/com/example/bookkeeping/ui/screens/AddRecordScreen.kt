package com.example.bookkeeping.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.ui.main.AddRecordUiState
import com.example.bookkeeping.ui.util.formatDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    state: AddRecordUiState,
    tags: List<String>,
    accounts: List<Account>,
    contentPadding: PaddingValues,
    onTypeSelected: (RecordType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onDateChanged: (Long) -> Unit,
    onAccountSelected: (Long) -> Unit,
    onSaveClicked: () -> Unit,
    onDeleteClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var tagExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDateDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateChanged(datePickerState.selectedDateMillis ?: state.selectedDateMillis)
                        showDateDialog = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateDialog = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirm && state.isEditing && onDeleteClicked != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClicked()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            },
            title = { Text("删除这笔账？") },
            text = { Text("删除后将无法恢复，确定继续吗？") }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (state.isEditing) "编辑记录" else "新增记录",
            style = MaterialTheme.typography.titleLarge
        )

        Text(text = "收支类型", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.type == RecordType.EXPENSE,
                onClick = { onTypeSelected(RecordType.EXPENSE) },
                label = { Text("支出") }
            )
            FilterChip(
                selected = state.type == RecordType.INCOME,
                onClick = { onTypeSelected(RecordType.INCOME) },
                label = { Text("收入") }
            )
        }

        OutlinedTextField(
            value = state.amountInput,
            onValueChange = onAmountChanged,
            label = { Text("金额") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        ExposedDropdownMenuBox(
            expanded = accountExpanded,
            onExpandedChange = { accountExpanded = !accountExpanded }
        ) {
            OutlinedTextField(
                value = accounts.firstOrNull { it.id == state.selectedAccountId }?.name.orEmpty(),
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
                accounts.forEach { account ->
                    DropdownMenuItem(
                        text = { Text(account.name) },
                        onClick = {
                            onAccountSelected(account.id)
                            accountExpanded = false
                        }
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = tagExpanded,
            onExpandedChange = { tagExpanded = !tagExpanded }
        ) {
            OutlinedTextField(
                value = state.selectedTag,
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
                tags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag) },
                        onClick = {
                            onTagSelected(tag)
                            tagExpanded = false
                        }
                    )
                }
            }
        }

        Text(
            text = if (state.type == RecordType.EXPENSE) {
                "当前只显示支出标签，避免把“奖金”选到支出里。"
            } else {
                "当前只显示收入标签，避免把“吃饭/购物”选到收入里。"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.noteInput,
            onValueChange = onNoteChanged,
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "日期：${formatDay(state.selectedDateMillis)}")
            TextButton(onClick = { showDateDialog = true }) {
                Text("选择日期")
            }
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onSaveClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isEditing) "保存修改" else "保存记录")
        }

        if (state.isEditing && onDeleteClicked != null) {
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("删除这笔账", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
