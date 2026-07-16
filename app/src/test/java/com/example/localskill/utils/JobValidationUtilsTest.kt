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
}
