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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.TagDataSource
import com.example.bookkeeping.data.model.Budget
import com.example.bookkeeping.data.model.BudgetScope
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.ui.components.MonthYearPickerDialog
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val budgetMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagementScreen(
    budgets: List<Budget>,
    records: List<Record>,
    availableYears: List<Int>,
    defaultMonth: String,
    contentPadding: PaddingValues,
    onSaveBudget: (Budget) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.parse(defaultMonth, budgetMonthFormatter)) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf(BudgetScope.TOTAL) }
    var selectedTag by remember { mutableStateOf(TagDataSource.expenseTags.first()) }
    var tagExpanded by remember { mutableStateOf(false) }

    fun openDialog(budget: Budget?) {
        editingBudget = budget
        amountInput = budget?.amountLimit?.toString().orEmpty()
        selectedScope = budget?.scope ?: BudgetScope.TOTAL
        selectedTag = budget?.tag ?: TagDataSource.expenseTags.first()
        showDialog = true
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialYearMonth = selectedMonth,
            availableYears = availableYears,
            onDismiss = { showMonthPicker = false },
            onConfirm = {
                selectedMonth = it
                showMonthPicker = false
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingBudget == null) "新增预算" else "编辑预算") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = selectedScope == BudgetScope.TOTAL, onClick = { selectedScope = BudgetScope.TOTAL }, label = { Text("总支出") })
                        FilterChip(selected = selectedScope == BudgetScope.CATEGORY, onClick = { selectedScope = BudgetScope.CATEGORY }, label = { Text("分类") })
                    }
                    if (selectedScope == BudgetScope.CATEGORY) {
                        ExposedDropdownMenuBox(expanded = tagExpanded, onExpandedChange = { tagExpanded = !tagExpanded }) {
                            OutlinedTextField(
                                value = selectedTag,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("标签") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            DropdownMenu(expanded = tagExpanded, onDismissRequest = { tagExpanded = false }) {
                                TagDataSource.expenseTags.forEach { tag ->
                                    DropdownMenuItem(text = { Text(tag) }, onClick = {
                                        selectedTag = tag
                                        tagExpanded = false
                                    })
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("预算金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    onSaveBudget(
                        Budget(
                            id = editingBudget?.id ?: 0L,
                            yearMonth = selectedMonth.format(budgetMonthFormatter),
                            scope = selectedScope,
                            tag = if (selectedScope == BudgetScope.CATEGORY) selectedTag else null,
                            amountLimit = amount
                        )
                    )
                    showDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("取消") }
            }
        )
    }

    val selectedMonthKey = selectedMonth.format(budgetMonthFormatter)
    val filteredBudgets = budgets.filter { it.yearMonth == selectedMonthKey }
    val monthExpenses = records.filter { record ->
        record.type == RecordType.EXPENSE &&
            Instant.ofEpochMilli(record.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(budgetMonthFormatter) == selectedMonthKey
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("预算管理", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("当前月份：${selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月"))}")
                        TextButton(onClick = { showMonthPicker = true }) { Text("切换月份") }
                    }
                    TextButton(onClick = { openDialog(null) }) { Text("新增预算") }
                }
            }
        }

        items(filteredBudgets, key = { it.id }) { budget ->
            val spent = when (budget.scope) {
                BudgetScope.TOTAL -> monthExpenses.sumOf { it.amount }
                BudgetScope.CATEGORY -> monthExpenses.filter { it.tag == budget.tag }.sumOf { it.amount }
            }
            val remaining = budget.amountLimit - spent
            val isOverBudget = spent > budget.amountLimit
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (budget.scope == BudgetScope.TOTAL) "本月总支出预算" else "${budget.tag} 预算")
                    Text("预算：${budget.amountLimit}")
                    Text(
                        "已用：$spent，剩余：$remaining",
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { openDialog(budget) }) { Text("编辑") }
                        TextButton(onClick = { onDeleteBudget(budget.id) }) { Text("删除") }
                    }
                }
            }
        }
    }
}
