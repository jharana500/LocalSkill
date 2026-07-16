package com.example.localskill.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {

    private fun mediumDateFormat() = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private fun monthYearFormat() = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    fun formatDate(timestampMillis: Long): String =
        if (timestampMillis <= 0L) "" else mediumDateFormat().format(Date(timestampMillis))

    fun formatMonthYear(timestampMillis: Long): String =
        if (timestampMillis <= 0L) "" else monthYearFormat().format(Date(timestampMillis))

    fun formatRelative(timestampMillis: Long): String {
        if (timestampMillis <= 0L) return ""
        val diff = System.currentTimeMillis() - timestampMillis
        if (diff < 0) return formatDate(timestampMillis)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days < 7 -> "$days day${if (days == 1L) "" else "s"} ago"
            days < 30 -> "${days / 7} week${if (days / 7 == 1L) "" else "s"} ago"
            else -> formatDate(timestampMillis)
        }
    }

    fun formatInterviewDateTime(timestampMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        if (timestampMillis <= 0L) return ""
        val dateTime = Instant.ofEpochMilli(timestampMillis).atZone(zoneId)
        val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .format(dateTime)
        val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .format(dateTime)
        return "$date • $time • ${zoneId.id}"
    }

    fun formatDeadline(deadlineMillis: Long): String {
        if (deadlineMillis <= 0L) return "No deadline"
        val remainingDays = TimeUnit.MILLISECONDS.toDays(deadlineMillis - System.currentTimeMillis())
        return when {
            remainingDays < 0 -> "Deadline passed"
            remainingDays == 0L -> "Closes today"
            remainingDays == 1L -> "Closes tomorrow"
            else -> "Closes in $remainingDays days"
        }
    }

    fun formatEducationDuration(startYear: Int, endYear: Int?, currentlyStudying: Boolean): String {
        val start = if (startYear > 0) startYear.toString() else "—"
        val end = when {
            currentlyStudying -> "Present"
            endYear != null && endYear > 0 -> endYear.toString()
            else -> "—"
        }
        return "$start – $end"
    }

    fun formatExperienceDuration(startDate: Long, endDate: Long?, currentlyWorking: Boolean): String {
        val start = formatMonthYear(startDate).ifBlank { "—" }
        val end = when {
            currentlyWorking -> "Present"
            endDate != null && endDate > 0 -> formatMonthYear(endDate)
            else -> "—"
        }
        return "$start – $end"
    }
}
