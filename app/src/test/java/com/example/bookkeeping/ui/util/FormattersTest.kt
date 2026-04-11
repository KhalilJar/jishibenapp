package com.example.bookkeeping.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersTest {

    @Test
    fun formatCalendarAmount_formatsSmallValues() {
        assertEquals("+125", formatCalendarAmount(125.0))
        assertEquals("-98.5", formatCalendarAmount(-98.5))
        assertEquals("0", formatCalendarAmount(0.0))
    }

    @Test
    fun formatCalendarAmount_formatsCompactKAndWValues() {
        assertEquals("+7.6k", formatCalendarAmount(7631.0))
        assertEquals("-8.3k", formatCalendarAmount(-8266.0))
        assertEquals("+1.3w", formatCalendarAmount(12600.0))
    }
}
