package com.example.bookkeeping.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val fullDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun formatDateTime(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(fullDateFormatter)
}

fun formatDay(millis: Long): String {
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(dayFormatter)
}

fun formatAmount(amount: Double): String {
    return String.format(Locale.getDefault(), "%.2f", amount)
}
