package com.example.localskill.utils

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class JobValidationUtilsTest {

    @Test
    fun `salary range valid when min less than max`() {
        assertNull(JobValidationUtils.validateSalaryRange(20000.0, 30000.0))
    }

    @Test
    fun `salary range invalid when min exceeds max`() {
        assertNotNull(JobValidationUtils.validateSalaryRange(50000.0, 30000.0))
    }

    @Test
    fun `salary range valid when either bound is missing`() {
        assertNull(JobValidationUtils.validateSalaryRange(null, 30000.0))
        assertNull(JobValidationUtils.validateSalaryRange(20000.0, null))
    }

    @Test
    fun `deadline in the future is valid`() {
        val future = System.currentTimeMillis() + 100_000
        assert(JobValidationUtils.isDeadlineValid(future))
    }

    @Test
    fun `deadline in the past is invalid`() {
        val past = System.currentTimeMillis() - 100_000
        assert(!JobValidationUtils.isDeadlineValid(past))
    }

    @Test
    fun `zero deadline means no deadline and is valid`() {
        assert(JobValidationUtils.isDeadlineValid(0L))
    }

    @Test
    fun `cover letter within limit passes`() {
        assertNull(JobValidationUtils.validateCoverLetter("A reasonable cover letter."))
    }

    @Test
    fun `cover letter exceeding limit fails`() {
        val longText = "a".repeat(Constants.MAX_COVER_LETTER_LENGTH + 1)
        assertNotNull(JobValidationUtils.validateCoverLetter(longText))
    }

    @Test
    fun `education end year before start year fails`() {
        assertNotNull(JobValidationUtils.validateEducationYears(2020, 2018, currentlyStudying = false))
    }

    @Test
    fun `education currently studying ignores missing end year`() {
        assertNull(JobValidationUtils.validateEducationYears(2020, null, currentlyStudying = true))
    }

    @Test
    fun `education invalid start year fails`() {
        assertNotNull(JobValidationUtils.validateEducationYears(1800, null, currentlyStudying = true))
    }

    @Test
    fun `experience end date before start date fails`() {
        assertNotNull(JobValidationUtils.validateExperienceDates(2_000L, 1_000L, currentlyWorking = false))
    }

    @Test
    fun `experience currently working ignores missing end date`() {
        assertNull(JobValidationUtils.validateExperienceDates(1_000L, null, currentlyWorking = true))
    }

    @Test
    fun `experience missing start date fails`() {
        assertNotNull(JobValidationUtils.validateExperienceDates(0L, null, currentlyWorking = true))
    }

    @Test
    fun `vacancy count must be at least one`() {
        assertNotNull(JobValidationUtils.validateVacancyCount(0))
        assertNull(JobValidationUtils.validateVacancyCount(1))
    }

    @Test
    fun `description shorter than the minimum length fails`() {
        assertNotNull(JobValidationUtils.validateDescriptionLength("too short"))
    }

    @Test
    fun `description meeting the minimum length passes`() {
        assertNull(JobValidationUtils.validateDescriptionLength("a".repeat(Constants.MAX_JOB_DESCRIPTION_MIN_LENGTH)))
    }

    @Test
    fun `duplicate skills are detected case-insensitively`() {
        assert(JobValidationUtils.hasDuplicateSkills(listOf("Kotlin", "kotlin")))
        assert(!JobValidationUtils.hasDuplicateSkills(listOf("Kotlin", "Java")))
    }

    private fun readyJobArgs(overrides: Map<String, Any?> = emptyMap()) = mapOf(
        "title" to "Android Developer",
        "description" to "a".repeat(Constants.MAX_JOB_DESCRIPTION_MIN_LENGTH),
        "categoryId" to "cat-1",
        "location" to "Kathmandu",
        "jobType" to "FULL_TIME",
        "workplaceType" to "ON_SITE",
        "vacancyCount" to 1,
        "applicationDeadline" to (System.currentTimeMillis() + 86_400_000L),
        "minimumSalary" to 20000.0,
        "maximumSalary" to 30000.0,
        "skills" to listOf("Kotlin")
    ) + overrides

    @Test
    fun `validatePublishReadiness passes for a fully complete job`() {
        val args = readyJobArgs()
        @Suppress("UNCHECKED_CAST")
        val violations = JobValidationUtils.validatePublishReadiness(
            title = args["title"] as String,
            description = args["description"] as String,
            categoryId = args["categoryId"] as String,
            location = args["location"] as String,
            jobType = args["jobType"] as String,
            workplaceType = args["workplaceType"] as String,
            vacancyCount = args["vacancyCount"] as Int,
            applicationDeadline = args["applicationDeadline"] as Long,
            minimumSalary = args["minimumSalary"] as Double?,
            maximumSalary = args["maximumSalary"] as Double?,
            skills = args["skills"] as List<String>
        )
        assert(violations.isEmpty())
    }

    @Test
    fun `validatePublishReadiness rejects a job missing required fields`() {
        val violations = JobValidationUtils.validatePublishReadiness(
            title = "",
            description = "too short",
            categoryId = "",
            location = "",
            jobType = "",
            workplaceType = "",
            vacancyCount = 0,
            applicationDeadline = System.currentTimeMillis() - 1000L,
            minimumSalary = 50000.0,
            maximumSalary = 20000.0,
            skills = listOf("Kotlin", "kotlin")
        )
        assert(violations.isNotEmpty())
        assert(violations.contains("Job title is required."))
    }
}
