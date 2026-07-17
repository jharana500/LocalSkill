package com.example.localskill.viewmodel

import com.example.localskill.model.JobModel
import com.example.localskill.model.JobModerationStatus
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.utils.ResultState
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AdminJobViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var adminRepo: AdminRepo
    private lateinit var viewModel: AdminJobViewModel

    private val job = JobModel(id = "job-1", title = "Android Developer", applicationCount = 3)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("admin-1")
        adminRepo = mock()
        viewModel = AdminJobViewModel(authRepo, adminRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `removeJob hides the job from discovery but preserves its record`() = runTest {
        val removedJob = job.copy(moderationStatus = JobModerationStatus.REMOVED.name, moderationReason = "Reported as a scam listing.")
        whenever(adminRepo.removeJob("admin-1", "job-1", "Reported as a scam listing."))
            .thenReturn(ResultState.Success(Unit))
        whenever(adminRepo.getAllJobsForModeration()).thenReturn(ResultState.Success(listOf(removedJob)))

        viewModel.removeJob("job-1", "Reported as a scam listing.")

        val uiJob = viewModel.uiState.value.jobs.single()
        assertEquals(JobModerationStatus.REMOVED.name, uiJob.moderationStatus)
        assertEquals("Android Developer", uiJob.title)
        assertEquals(3, uiJob.applicationCount)
    }

    @Test
    fun `removeJob without a reason is a no-op`() = runTest {
        viewModel.removeJob("job-1", "")

        verify(adminRepo, never()).removeJob(any(), any(), any())
    }

    @Test
    fun `restoreJob returns a removed job to VISIBLE`() = runTest {
        val restoredJob = job.copy(moderationStatus = JobModerationStatus.VISIBLE.name, moderationReason = "")
        whenever(adminRepo.restoreJob("admin-1", "job-1")).thenReturn(ResultState.Success(Unit))
        whenever(adminRepo.getAllJobsForModeration()).thenReturn(ResultState.Success(listOf(restoredJob)))

        viewModel.restoreJob("job-1")

        val uiJob = viewModel.uiState.value.jobs.single()
        assertEquals(JobModerationStatus.VISIBLE.name, uiJob.moderationStatus)
        assertEquals("", uiJob.moderationReason)
    }
}
