package com.example.bookkeeping.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

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

fun formatCalendarAmount(value: Double): String {
    if (value == 0.0) return "0"

    val absoluteValue = abs(value)
    val sign = if (value > 0) "+" else "-"
    val (scaledValue, suffix) = when {
        absoluteValue < 1_000 -> absoluteValue to ""
        absoluteValue < 10_000 -> (absoluteValue / 1_000) to "k"
        else -> (absoluteValue / 10_000) to "w"
    }

    val formatter = DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.getDefault()))
    return sign + formatter.format(scaledValue) + suffix
}
