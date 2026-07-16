package com.example.localskill.utils

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class DateUtilsInterviewTest {

    @Test
    fun interviewFormatterIncludesDateTimeAndTimezone() {
        val zone = ZoneId.of("Asia/Kathmandu")
        val timestamp = LocalDateTime.of(2026, 8, 14, 11, 30).atZone(zone).toInstant().toEpochMilli()

        val formatted = DateUtils.formatInterviewDateTime(timestamp, zone)

        assertTrue(formatted.contains("2026"))
        assertTrue(formatted.contains("11:30"))
        assertTrue(formatted.contains("Asia/Kathmandu"))
    }
}
