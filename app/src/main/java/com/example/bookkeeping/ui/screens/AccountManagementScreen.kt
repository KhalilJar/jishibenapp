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
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.bookkeeping.data.model.Account

@Composable
fun AccountManagementScreen(
    accounts: List<Account>,
    contentPadding: PaddingValues,
    onAddAccount: (String) -> Unit,
    onRenameAccount: (Account, String) -> Unit,
    onToggleArchive: (Account) -> Unit,
    onMoveAccount: (Long, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var nameInput by remember { mutableStateOf("") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新增账户") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("账户名称") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onAddAccount(nameInput)
                    nameInput = ""
                    showAddDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = {
                    nameInput = ""
                    showAddDialog = false
                }) { Text("取消") }
            }
        )
    }

    if (editingAccount != null) {
        AlertDialog(
            onDismissRequest = { editingAccount = null },
            title = { Text("重命名账户") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("账户名称") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editingAccount?.let { onRenameAccount(it, nameInput) }
                    editingAccount = null
                    nameInput = ""
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = {
                    editingAccount = null
                    nameInput = ""
                }) { Text("取消") }
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
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("账户管理", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "默认账户不可删除，自定义账户可以归档。上下箭头用于调整显示顺序。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { showAddDialog = true }) {
                        Text("新增自定义账户")
                    }
                }
            }
        }

        items(accounts, key = { it.id }) { account ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(account.name)
                        Text(
                            text = buildString {
                                append(account.type.displayName)
                                if (account.isArchived) append(" · 已归档")
                                if (account.isSystem) append(" · 默认账户")
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onMoveAccount(account.id, -1) }) {
                            Icon(Icons.Outlined.ArrowUpward, contentDescription = "上移")
                        }
                        IconButton(onClick = { onMoveAccount(account.id, 1) }) {
                            Icon(Icons.Outlined.ArrowDownward, contentDescription = "下移")
                        }
                        TextButton(onClick = {
                            editingAccount = account
                            nameInput = account.name
                        }) {
                            Text("重命名")
                        }
                        if (!account.isSystem) {
                            TextButton(onClick = { onToggleArchive(account) }) {
                                Text(if (account.isArchived) "恢复" else "归档")
                            }
                        }
                    }
                }
            }
        }
    }
}
