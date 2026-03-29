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
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.ui.main.AddRecordUiState
import com.example.bookkeeping.ui.util.formatDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordScreen(
    state: AddRecordUiState,
    tags: List<String>,
    contentPadding: PaddingValues,
    onTypeSelected: (RecordType) -> Unit,
    onAmountChanged: (String) -> Unit,
    onTagSelected: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onDateChanged: (Long) -> Unit,
    onSaveClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tagExpanded by remember { mutableStateOf(false) }
    var showDateDialog by remember { mutableStateOf(false) }

    if (showDateDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis ?: state.selectedDateMillis
                        onDateChanged(selectedMillis)
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "收支类型（必选）")
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
            label = { Text("金额（必填）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        ExposedDropdownMenuBox(
            expanded = tagExpanded,
            onExpandedChange = { tagExpanded = !tagExpanded }
        ) {
            OutlinedTextField(
                value = state.selectedTag,
                onValueChange = {},
                readOnly = true,
                label = { Text("标签（必选）") },
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
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onSaveClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存记录")
        }
    }
}
