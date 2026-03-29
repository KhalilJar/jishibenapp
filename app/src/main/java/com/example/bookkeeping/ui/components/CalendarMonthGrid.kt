package com.example.bookkeeping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.data.model.DailySummary
import com.example.bookkeeping.ui.util.formatAmount
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.abs

@Composable
fun CalendarMonthGrid(
    yearMonth: YearMonth,
    weekdays: List<String>,
    summaryByDate: Map<LocalDate, DailySummary>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstDay = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value - 1 // Monday = 0
    val daysInMonth = yearMonth.lengthOfMonth()

    val calendarCells = buildList<LocalDate?>(42) {
        repeat(startOffset) { add(null) }
        for (day in 1..daysInMonth) {
            add(yearMonth.atDay(day))
        }
        while (size < 42) {
            add(null)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        WeekdayHeader(weekdays = weekdays)

        for (weekIndex in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayIndex in 0 until 7) {
                    val day = calendarCells[weekIndex * 7 + dayIndex]
                    CalendarDayCell(
                        date = day,
                        summary = day?.let { summaryByDate[it] },
                        onClick = onDayClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader(weekdays: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        weekdays.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    summary: DailySummary?,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    if (date == null) {
        Box(
            modifier = modifier
                .height(84.dp)
                .padding(2.dp)
                .border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.small)
        )
        return
    }

    val netAmount = (summary?.totalIncome ?: 0.0) - (summary?.totalExpense ?: 0.0)

    Column(
        modifier = modifier
            .height(84.dp)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .border(width = 1.dp, color = borderColor, shape = MaterialTheme.shapes.small)
            .clickable { onClick(date) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall
        )

        if (summary != null) {
            val netText = if (netAmount >= 0) {
                "+${formatAmount(netAmount)}"
            } else {
                "-${formatAmount(abs(netAmount))}"
            }
            val netColor = if (netAmount >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)

            Text(
                text = netText,
                style = MaterialTheme.typography.labelSmall,
                color = netColor
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                if (summary.totalIncome > 0) {
                    IndicatorDot(color = Color(0xFF2E7D32))
                }
                if (summary.totalExpense > 0) {
                    if (summary.totalIncome > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IndicatorDot(color = Color(0xFFC62828))
                }
            }
        } else {
            Spacer(modifier = Modifier.size(1.dp))
        }
    }
}

@Composable
private fun IndicatorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(MaterialTheme.shapes.small)
            .background(color)
    )
}
