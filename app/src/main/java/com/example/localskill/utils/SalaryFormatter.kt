package com.example.localskill.utils

import java.text.NumberFormat
import java.util.Locale

object SalaryFormatter {

    private fun formatAmount(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount)
    }

    fun format(minimumSalary: Double?, maximumSalary: Double?, currency: String = "NPR"): String = when {
        minimumSalary == null && maximumSalary == null -> "Not disclosed"
        minimumSalary != null && maximumSalary != null && minimumSalary == maximumSalary ->
            "$currency ${formatAmount(minimumSalary)}/month"

        minimumSalary != null && maximumSalary != null ->
            "$currency ${formatAmount(minimumSalary)}–${formatAmount(maximumSalary)}/month"

        minimumSalary != null -> "$currency ${formatAmount(minimumSalary)}+/month"
        maximumSalary != null -> "Up to $currency ${formatAmount(maximumSalary)}/month"
        else -> "Negotiable"
    }
}
