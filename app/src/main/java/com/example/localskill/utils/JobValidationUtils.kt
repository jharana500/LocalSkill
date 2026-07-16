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

    fun validateVacancyCount(vacancyCount: Int): String? =
        if (vacancyCount <= 0) "Vacancy count must be at least 1." else null

    fun validateDescriptionLength(description: String): String? =
        if (description.trim().length < Constants.MAX_JOB_DESCRIPTION_MIN_LENGTH) {
            "Description should be at least ${Constants.MAX_JOB_DESCRIPTION_MIN_LENGTH} characters."
        } else {
            null
        }

    fun hasDuplicateSkills(skills: List<String>): Boolean {
        val normalized = skills.map { it.trim().lowercase() }.filter { it.isNotBlank() }
        return normalized.size != normalized.distinct().size
    }

    /**
     * Full validation required before a job can move from draft to published.
     * Returns every violation found, empty when the job is publish-ready.
     */
    fun validatePublishReadiness(
        title: String,
        description: String,
        categoryId: String,
        location: String,
        jobType: String,
        workplaceType: String,
        vacancyCount: Int,
        applicationDeadline: Long,
        minimumSalary: Double?,
        maximumSalary: Double?,
        skills: List<String>
    ): List<String> = buildList {
        if (title.isBlank()) add("Job title is required.")
        validateDescriptionLength(description)?.let { add(it) }
        if (categoryId.isBlank()) add("Category is required.")
        if (location.isBlank()) add("Location is required.")
        if (jobType.isBlank()) add("Job type is required.")
        if (workplaceType.isBlank()) add("Workplace type is required.")
        validateVacancyCount(vacancyCount)?.let { add(it) }
        if (applicationDeadline <= 0L) {
            add("Application deadline is required.")
        } else if (!isDeadlineValid(applicationDeadline)) {
            add("Application deadline must be in the future.")
        }
        validateSalaryRange(minimumSalary, maximumSalary)?.let { add(it) }
        if (hasDuplicateSkills(skills)) add("Remove duplicate skills.")
    }
}
