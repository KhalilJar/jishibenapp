package com.example.bookkeeping.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.ui.main.DataManagementUiState

@Composable
fun DataManagementScreen(
    state: DataManagementUiState,
    contentPadding: PaddingValues,
    onExportBackup: (Uri, String) -> Unit,
    onImportBackup: (Uri, String) -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var password by remember { mutableStateOf("") }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) onExportBackup(uri, password)
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onImportBackup(uri, password)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("数据管理", style = MaterialTheme.typography.titleMedium)
                Text("当前版本先支持本地加密导出/导入。等这套稳定后，再接你的 VPS 做 SFTP 备份。")
            }
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("备份密码") },
            modifier = Modifier.fillMaxWidth()
        )

        TextButton(onClick = { exportLauncher.launch("bookkeeping-backup.json") }, modifier = Modifier.fillMaxWidth()) {
            Text("导出加密备份")
        }
        TextButton(onClick = { importLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("导入加密备份")
        }

        if (state.statusMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.statusMessage)
                    TextButton(onClick = onClearMessage) { Text("知道了") }
                }
            }
        }

        if (state.errorMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = onClearMessage) { Text("知道了") }
                }
            }
        }
    }
}
