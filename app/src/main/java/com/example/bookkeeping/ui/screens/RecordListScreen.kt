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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.ui.util.formatAmount
import com.example.bookkeeping.ui.util.formatDateTime

@Composable
fun RecordListScreen(
    records: List<Record>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "还没有记录，先去“添加”页创建第一条吧。")
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(records, key = { it.id }) { record ->
            RecordCard(record = record)
        }
    }
}

@Composable
private fun RecordCard(record: Record) {
    val amountColor = if (record.type == RecordType.INCOME) {
        Color(0xFF2E7D32)
    } else {
        Color(0xFFC62828)
    }
    val amountPrefix = if (record.type == RecordType.INCOME) "+" else "-"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${record.type.displayName} · ${record.tag}")
                Text(
                    text = "$amountPrefix${formatAmount(record.amount)}",
                    color = amountColor
                )
            }
            Text(text = "日期：${formatDateTime(record.timestamp)}")
            if (!record.note.isNullOrBlank()) {
                Text(text = "备注：${record.note}")
            }
        }
    }
}
