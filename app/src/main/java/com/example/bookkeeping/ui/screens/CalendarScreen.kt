package com.example.bookkeeping.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.bookkeeping.R
import com.example.bookkeeping.data.model.DailySummary
import com.example.bookkeeping.ui.components.CalendarMonthGrid
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy\u5e74MM\u6708")

@Composable
fun CalendarScreen(
    yearMonth: YearMonth,
    dailySummary: List<DailySummary>,
    contentPadding: PaddingValues,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoneId = ZoneId.systemDefault()
    val weekdays = listOf(
        stringResource(R.string.weekday_mon),
        stringResource(R.string.weekday_tue),
        stringResource(R.string.weekday_wed),
        stringResource(R.string.weekday_thu),
        stringResource(R.string.weekday_fri),
        stringResource(R.string.weekday_sat),
        stringResource(R.string.weekday_sun)
    )
    val summaryByDate = remember(dailySummary) {
        dailySummary.mapNotNull { item ->
            runCatching {
                LocalDate.parse(item.day) to item
            }.getOrNull()
        }.toMap()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.Outlined.ChevronLeft,
                    contentDescription = stringResource(R.string.calendar_prev_month)
                )
            }
            Text(
                text = yearMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.calendar_next_month)
                )
            }
        }

        Text(
            text = stringResource(R.string.calendar_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CalendarMonthGrid(
            yearMonth = yearMonth,
            weekdays = weekdays,
            summaryByDate = summaryByDate,
            onDayClick = { date ->
                val dayStartMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
                onDayClick(dayStartMillis)
            }
        )
    }
}
