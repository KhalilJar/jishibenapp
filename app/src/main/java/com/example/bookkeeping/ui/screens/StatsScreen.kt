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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.model.DailySummary
import com.example.bookkeeping.ui.util.formatAmount

@Composable
fun StatsScreen(
    dailySummary: List<DailySummary>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    if (dailySummary.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "暂无统计数据，新增记录后会自动汇总。")
            Text(
                text = "当前仅实现按天统计，按月/按年将在下一阶段扩展。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
        item {
            Text(
                text = "按天汇总（收入与支出分别统计）",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "按月/按年：已预留接口，后续补充。",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(dailySummary, key = { it.day }) { item ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = item.day, style = MaterialTheme.typography.titleSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("收入")
                        Text(text = "+${formatAmount(item.totalIncome)}", color = Color(0xFF2E7D32))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("支出")
                        Text(text = "-${formatAmount(item.totalExpense)}", color = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}
