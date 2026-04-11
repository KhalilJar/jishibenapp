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
import com.example.bookkeeping.data.model.Account
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.data.model.RecurringFrequency
import com.example.bookkeeping.data.model.RecurringRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringManagementScreen(
    rules: List<RecurringRule>,
    dueRules: List<RecurringRule>,
    accounts: List<Account>,
    todayDate: String,
    contentPadding: PaddingValues,
    onSaveRule: (RecurringRule) -> Unit,
    onDeleteRule: (Long) -> Unit,
    onToggleRuleActive: (RecurringRule) -> Unit,
    onCreateFromRule: (RecurringRule) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<RecurringRule?>(null) }
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(RecordType.EXPENSE) }
    var amountInput by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf(TagDataSource.expenseTags.first()) }
    var note by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
    var intervalInput by remember { mutableStateOf("1") }
    var accountExpanded by remember { mutableStateOf(false) }
    var tagExpanded by remember { mutableStateOf(false) }

    fun openDialog(rule: RecurringRule?) {
        editingRule = rule
        title = rule?.title.orEmpty()
        type = rule?.type ?: RecordType.EXPENSE
        amountInput = rule?.amount?.toString().orEmpty()
        tag = rule?.tag ?: TagDataSource.tagsFor(type).first()
        note = rule?.note.orEmpty()
        accountId = rule?.accountId ?: accounts.firstOrNull()?.id ?: 1L
        frequency = rule?.frequency ?: RecurringFrequency.MONTHLY
        intervalInput = rule?.intervalCount?.toString() ?: "1"
        showDialog = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingRule == null) "新增固定收支" else "编辑固定收支") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("名称") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = type == RecordType.EXPENSE, onClick = {
                            type = RecordType.EXPENSE
                            tag = TagDataSource.expenseTags.first()
                        }, label = { Text("支出") })
                        FilterChip(selected = type == RecordType.INCOME, onClick = {
                            type = RecordType.INCOME
                            tag = TagDataSource.incomeTags.first()
                        }, label = { Text("收入") })
                    }
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(expanded = tagExpanded, onExpandedChange = { tagExpanded = !tagExpanded }) {
                        OutlinedTextField(
                            value = tag,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("标签") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        DropdownMenu(expanded = tagExpanded, onDismissRequest = { tagExpanded = false }) {
                            TagDataSource.tagsFor(type).forEach { item ->
                                DropdownMenuItem(text = { Text(item) }, onClick = {
                                    tag = item
                                    tagExpanded = false
                                })
                            }
                        }
                    }
                    ExposedDropdownMenuBox(expanded = accountExpanded, onExpandedChange = { accountExpanded = !accountExpanded }) {
                        OutlinedTextField(
                            value = accounts.firstOrNull { it.id == accountId }?.name.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("账户") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        DropdownMenu(expanded = accountExpanded, onDismissRequest = { accountExpanded = false }) {
                            accounts.forEach { account ->
                                DropdownMenuItem(text = { Text(account.name) }, onClick = {
                                    accountId = account.id
                                    accountExpanded = false
                                })
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RecurringFrequency.entries.forEach { item ->
                            FilterChip(selected = frequency == item, onClick = { frequency = item }, label = { Text(item.displayName) })
                        }
                    }
                    OutlinedTextField(
                        value = intervalInput,
                        onValueChange = { intervalInput = it },
                        label = { Text("间隔数量") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注（可选）") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    val intervalCount = intervalInput.toIntOrNull() ?: 1
                    onSaveRule(
                        RecurringRule(
                            id = editingRule?.id ?: 0L,
                            title = title,
                            type = type,
                            amount = amount,
                            tag = tag,
                            note = note.ifBlank { null },
                            accountId = accountId,
                            frequency = frequency,
                            intervalCount = intervalCount,
                            startDate = editingRule?.startDate ?: todayDate,
                            nextDueDate = editingRule?.nextDueDate ?: todayDate,
                            isActive = editingRule?.isActive ?: true
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("固定收支与提醒", style = MaterialTheme.typography.titleMedium)
                    Text("第一版只做提醒，不会静默自动入账。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { openDialog(null) }) { Text("新增规则") }
                }
            }
        }

        if (dueRules.isNotEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("待处理提醒", style = MaterialTheme.typography.titleMedium)
                        dueRules.forEach { rule ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(rule.title)
                                    Text("到期日：${rule.nextDueDate}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { onCreateFromRule(rule) }) { Text("记一笔") }
                            }
                        }
                    }
                }
            }
        }

        items(rules, key = { it.id }) { rule ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(rule.title)
                    Text("${rule.frequency.displayName} · 下次提醒 ${rule.nextDueDate}")
                    Text("金额：${rule.amount} · 标签：${rule.tag}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { openDialog(rule) }) { Text("编辑") }
                        TextButton(onClick = { onToggleRuleActive(rule) }) { Text(if (rule.isActive) "停用" else "启用") }
                        TextButton(onClick = { onDeleteRule(rule.id) }) { Text("删除") }
                    }
                }
            }
        }
    }
}
