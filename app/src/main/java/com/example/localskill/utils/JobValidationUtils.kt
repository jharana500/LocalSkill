package com.example.localskill.utils

import java.util.Calendar

object JobValidationUtils {

    fun validateSalaryRange(minimumSalary: Double?, maximumSalary: Double?): String? =
        if (minimumSalary != null && maximumSalary != null && minimumSalary > maximumSalary) {
            "Minimum salary cannot exceed maximum salary."
        } else {
            null
        }

    fun isDeadlineValid(applicationDeadline: Long, referenceTimeMillis: Long = System.currentTimeMillis()): Boolean =
        applicationDeadline <= 0L || applicationDeadline >= referenceTimeMillis

    fun validateCoverLetter(coverLetter: String): String? =
        if (coverLetter.length > Constants.MAX_COVER_LETTER_LENGTH) {
            "Cover letter must be under ${Constants.MAX_COVER_LETTER_LENGTH} characters."
        } else {
            null
        }

    fun validateEducationYears(startYear: Int, endYear: Int?, currentlyStudying: Boolean): String? {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return when {
            startYear < 1950 || startYear > currentYear + 1 -> "Enter a valid start year."
            !currentlyStudying && endYear != null && endYear < startYear -> "End year cannot be before start year."
            else -> null
        }
    }

    fun validateExperienceDates(startDate: Long, endDate: Long?, currentlyWorking: Boolean): String? = when {
        startDate <= 0L -> "Enter a valid start date."
        !currentlyWorking && endDate != null && endDate < startDate -> "End date cannot be before start date."
        else -> null
    }
}
