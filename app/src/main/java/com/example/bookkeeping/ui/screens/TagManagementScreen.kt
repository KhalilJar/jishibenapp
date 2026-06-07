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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.TagDataSource
import com.example.bookkeeping.data.model.RecordType

@Composable
fun TagManagementScreen(
    expenseTags: List<String>,
    incomeTags: List<String>,
    contentPadding: PaddingValues,
    onAddTag: (RecordType, String) -> Unit,
    onRemoveTag: (RecordType, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf<RecordType?>(null) }
    var newTagInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf<Pair<RecordType, String>?>(null) }

    // 添加标签对话框
    if (showAddDialog != null) {
        AlertDialog(
            onDismissRequest = { showAddDialog = null; newTagInput = "" },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newTagInput.trim()
                        if (trimmed.isNotBlank()) {
                            onAddTag(showAddDialog!!, trimmed)
                            newTagInput = ""
                            showAddDialog = null
                        }
                    }
                ) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = null; newTagInput = "" }) { Text("取消") }
            },
            title = { Text("添加标签") },
            text = {
                OutlinedTextField(
                    value = newTagInput,
                    onValueChange = { newTagInput = it },
                    label = { Text("标签名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    // 删除确认对话框
    if (showDeleteConfirm != null) {
        val (type, tag) = showDeleteConfirm!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveTag(type, tag)
                        showDeleteConfirm = null
                    }
                ) { Text("删除", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") }
            },
            title = { Text("删除标签") },
            text = { Text("确定删除「$tag」标签吗？已使用该标签的记录不会受影响。") }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 支出标签
        item {
            SectionHeader(
                title = "支出标签",
                count = expenseTags.size
            )
        }

        items(expenseTags) { tag ->
            TagRow(
                tag = tag,
                isDefault = TagDataSource.isDefaultTag(RecordType.EXPENSE, tag),
                onDelete = { showDeleteConfirm = RecordType.EXPENSE to tag }
            )
        }

        item {
            TextButton(
                onClick = { showAddDialog = RecordType.EXPENSE },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("添加支出标签")
            }
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // 收入标签
        item {
            SectionHeader(
                title = "收入标签",
                count = incomeTags.size
            )
        }

        items(incomeTags) { tag ->
            TagRow(
                tag = tag,
                isDefault = TagDataSource.isDefaultTag(RecordType.INCOME, tag),
                onDelete = { showDeleteConfirm = RecordType.INCOME to tag }
            )
        }

        item {
            TextButton(
                onClick = { showAddDialog = RecordType.INCOME },
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("添加收入标签")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            "$count 个",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TagRow(
    tag: String,
    isDefault: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(tag, style = MaterialTheme.typography.bodyLarge)
                if (isDefault) {
                    Text(
                        "默认标签",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!isDefault) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
