package com.example.localskill.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationModelTest {

    @Test
    fun `employer transitions follow the applied to hired pipeline`() {
        assertTrue(ApplicationModel.isValidCompanyTransition(ApplicationStatus.APPLIED.name, ApplicationStatus.UNDER_REVIEW.name))
        assertTrue(ApplicationModel.isValidCompanyTransition(ApplicationStatus.UNDER_REVIEW.name, ApplicationStatus.SHORTLISTED.name))
        assertTrue(ApplicationModel.isValidCompanyTransition(ApplicationStatus.SHORTLISTED.name, ApplicationStatus.INTERVIEW.name))
        assertTrue(ApplicationModel.isValidCompanyTransition(ApplicationStatus.INTERVIEW.name, ApplicationStatus.HIRED.name))
    }

    @Test
    fun `rejection is allowed from every active status`() {
        assertTrue(ApplicationModel.isValidCompanyTransition(ApplicationStatus.APPLIED.name, ApplicationStatus.REJECTED.name))
        assertTrue(ApplicationModel.isValidCompanyTransition(ApplicationStatus.UNDER_REVIEW.name, ApplicationStatus.REJECTED.name))
        assertTrue(ApplicationModel.isValidCompanyTransition(ApplicationStatus.SHORTLISTED.name, ApplicationStatus.REJECTED.name))
        assertTrue(ApplicationModel.isValidCompanyTransition(ApplicationStatus.INTERVIEW.name, ApplicationStatus.REJECTED.name))
    }

    @Test
    fun `transitions cannot skip pipeline stages`() {
        assertFalse(ApplicationModel.isValidCompanyTransition(ApplicationStatus.APPLIED.name, ApplicationStatus.HIRED.name))
        assertFalse(ApplicationModel.isValidCompanyTransition(ApplicationStatus.APPLIED.name, ApplicationStatus.SHORTLISTED.name))
        assertFalse(ApplicationModel.isValidCompanyTransition(ApplicationStatus.APPLIED.name, ApplicationStatus.INTERVIEW.name))
    }

    @Test
    fun `terminal statuses accept no further employer transitions`() {
        assertFalse(ApplicationModel.isValidCompanyTransition(ApplicationStatus.HIRED.name, ApplicationStatus.REJECTED.name))
        assertFalse(ApplicationModel.isValidCompanyTransition(ApplicationStatus.REJECTED.name, ApplicationStatus.UNDER_REVIEW.name))
        assertFalse(ApplicationModel.isValidCompanyTransition(ApplicationStatus.WITHDRAWN.name, ApplicationStatus.UNDER_REVIEW.name))
    }

    @Test
    fun `withdrawable statuses exclude terminal states`() {
        assertTrue(ApplicationModel.WITHDRAWABLE_STATUSES.contains(ApplicationStatus.APPLIED.name))
        assertTrue(ApplicationModel.WITHDRAWABLE_STATUSES.contains(ApplicationStatus.INTERVIEW.name))
        assertFalse(ApplicationModel.WITHDRAWABLE_STATUSES.contains(ApplicationStatus.HIRED.name))
        assertFalse(ApplicationModel.WITHDRAWABLE_STATUSES.contains(ApplicationStatus.REJECTED.name))
        assertFalse(ApplicationModel.WITHDRAWABLE_STATUSES.contains(ApplicationStatus.WITHDRAWN.name))
    }
}
