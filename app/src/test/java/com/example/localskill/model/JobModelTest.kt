package com.example.localskill.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JobModelTest {

    @Test
    fun `a job created before Phase 4 with no moderationStatus is treated as VISIBLE`() {
        val legacyJob = JobModel(id = "job-1", status = JobStatus.ACTIVE.name, moderationStatus = "")
        assertEquals(JobModerationStatus.VISIBLE.name, legacyJob.normalizedModerationStatus)
        assertTrue(legacyJob.isDiscoverable)
    }

    @Test
    fun `a job removed by an administrator is not discoverable`() {
        val removedJob = JobModel(id = "job-1", status = JobStatus.ACTIVE.name, moderationStatus = JobModerationStatus.REMOVED.name)
        assertFalse(removedJob.isDiscoverable)
        assertFalse(removedJob.isOpenForApplications)
    }

    @Test
    fun `a flagged job stays visible but is flagged`() {
        val flaggedJob = JobModel(id = "job-1", status = JobStatus.ACTIVE.name, moderationStatus = JobModerationStatus.FLAGGED.name)
        assertFalse(flaggedJob.isDiscoverable)
    }

    @Test
    fun `only active discoverable non-expired jobs are open for applications`() {
        val future = System.currentTimeMillis() + 100_000
        val openJob = JobModel(id = "job-1", status = JobStatus.ACTIVE.name, applicationDeadline = future)
        assertTrue(openJob.isOpenForApplications)

        val draftJob = openJob.copy(status = JobStatus.DRAFT.name)
        assertFalse(draftJob.isOpenForApplications)

        val expiredJob = openJob.copy(applicationDeadline = System.currentTimeMillis() - 100_000)
        assertTrue(expiredJob.isExpired)
        assertFalse(expiredJob.isOpenForApplications)
    }
}
