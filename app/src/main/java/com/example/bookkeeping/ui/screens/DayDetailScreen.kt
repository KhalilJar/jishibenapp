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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.R
import com.example.bookkeeping.data.model.Record
import com.example.bookkeeping.data.model.RecordType
import com.example.bookkeeping.ui.util.formatAmount
import com.example.bookkeeping.ui.util.formatDateTime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val detailDayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@Composable
fun DayDetailScreen(
    dayStartMillis: Long,
    records: List<Record>,
    accountNameForRecord: (Long) -> String,
    contentPadding: PaddingValues,
    onBackClick: () -> Unit,
    onRecordClick: (Record) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayLabel = Instant.ofEpochMilli(dayStartMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(detailDayFormatter)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.day_detail_back)
                )
            }
            Text(
                text = stringResource(R.string.day_detail_title, dayLabel),
                style = MaterialTheme.typography.titleMedium
            )
        }

        if (records.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(R.string.day_detail_empty))
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records, key = { it.id }) { record ->
                val amountColor = if (record.type == RecordType.INCOME) Color(0xFF2E7D32) else Color(0xFFC62828)
                val amountPrefix = if (record.type == RecordType.INCOME) "+" else "-"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRecordClick(record) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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
                            Text(text = "$amountPrefix${formatAmount(record.amount)}", color = amountColor)
                        }
                        Text(text = "账户：${accountNameForRecord(record.accountId)}")
                        Text(text = stringResource(R.string.label_time, formatDateTime(record.timestamp)))
                        if (!record.note.isNullOrBlank()) {
                            Text(text = stringResource(R.string.label_note, record.note.orEmpty()))
                        }
                    }
                }
            }
        }
    }
}
