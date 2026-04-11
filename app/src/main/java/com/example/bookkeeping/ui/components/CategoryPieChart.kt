package com.example.bookkeeping.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.model.CategoryBreakdown
import com.example.bookkeeping.ui.util.formatAmount

private val chartColors = listOf(
    Color(0xFF5E60CE),
    Color(0xFF00A896),
    Color(0xFFFF9F1C),
    Color(0xFFE71D36),
    Color(0xFF6A994E),
    Color(0xFF4361EE),
    Color(0xFFFF70A6),
    Color(0xFF4D908E)
)

@Composable
fun CategoryPieChart(
    items: List<CategoryBreakdown>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "当前条件下还没有可展示的分类占比",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            val diameter = size.minDimension * 0.78f
            val topLeftX = (size.width - diameter) / 2
            val topLeftY = (size.height - diameter) / 2
            var startAngle = -90f

            items.forEachIndexed { index, item ->
                val color = chartColors[index % chartColors.size]
                val sweepAngle = item.percentage * 360f
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(topLeftX, topLeftY),
                    size = Size(diameter, diameter),
                    style = Stroke(width = 52f)
                )
                startAngle += sweepAngle
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEachIndexed { index, item ->
                val color = chartColors[index % chartColors.size]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color = color, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item.tag)
                    }
                    Text(
                        text = "${formatAmount(item.amount)}  (${(item.percentage * 100).toInt()}%)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
