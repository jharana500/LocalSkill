package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAdminRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobModerationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminJobViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeAdminRepo: FakeAdminRepo
    private lateinit var viewModel: AdminJobViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeAdminRepo = FakeAdminRepo().apply {
            jobs["job-1"] = JobModel(id = "job-1", title = "Android Developer", applicationCount = 3)
        }
        viewModel = AdminJobViewModel(fakeAuthRepo, fakeAdminRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `removeJob hides the job from discovery but preserves its record`() = runTest {
        viewModel.removeJob("job-1", "Reported as a scam listing.")

        val job = fakeAdminRepo.jobs.getValue("job-1")
        assertEquals(JobModerationStatus.REMOVED.name, job.moderationStatus)
        assertEquals("Android Developer", job.title)
        assertEquals(3, job.applicationCount)
    }

    @Test
    fun `removeJob without a reason is a no-op`() = runTest {
        viewModel.removeJob("job-1", "")

        assertEquals("", fakeAdminRepo.jobs.getValue("job-1").moderationStatus)
    }

    @Test
    fun `restoreJob returns a removed job to VISIBLE`() = runTest {
        fakeAdminRepo.jobs["job-1"] = fakeAdminRepo.jobs.getValue("job-1")
            .copy(moderationStatus = JobModerationStatus.REMOVED.name, moderationReason = "Scam")

        viewModel.restoreJob("job-1")

        val job = fakeAdminRepo.jobs.getValue("job-1")
        assertEquals(JobModerationStatus.VISIBLE.name, job.moderationStatus)
        assertEquals("", job.moderationReason)
    }
}
