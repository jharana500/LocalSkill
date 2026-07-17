package com.example.localskill.viewmodel

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.ResumeModel
import com.example.localskill.repo.ApplicationRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.JobRepo
import com.example.localskill.repo.JobSeekerProfileRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var jobRepo: JobRepo
    private lateinit var applicationRepo: ApplicationRepo
    private lateinit var profileRepo: JobSeekerProfileRepo
    private lateinit var viewModel: ApplicationViewModel

    private val job = JobModel(
        id = "job-1",
        companyId = "company-1",
        title = "Android Developer",
        companyName = "Acme",
        status = "ACTIVE",
        applicationDeadline = 0L
    )

    private val profileWithResume = JobSeekerProfileModel(
        userId = "uid-123",
        resume = ResumeModel(fileName = "resume.pdf", downloadUrl = "https://example.com/resume.pdf")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        jobRepo = mock()
        applicationRepo = mock()
        profileRepo = mock()
        viewModel = ApplicationViewModel(authRepo, jobRepo, applicationRepo, profileRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun stubHappyApply() {
        whenever(jobRepo.getJobById("job-1")).thenReturn(ResultState.Success(job))
        whenever(applicationRepo.hasApplied("uid-123", "job-1")).thenReturn(ResultState.Success(false))
        whenever(profileRepo.getProfile("uid-123")).thenReturn(ResultState.Success(profileWithResume))
    }

    @Test
    fun `loadApplyScreen exposes the job and resume`() = runTest {
        stubHappyApply()

        viewModel.loadApplyScreen("job-1")
        val state = viewModel.applyUiState.value
        assertEquals("job-1", state.job?.id)
        assertTrue(state.resume?.isPresent == true)
        assertFalse(state.jobUnavailable)
    }

    @Test
    fun `loadApplyScreen rejects a job the user already applied to`() = runTest {
        whenever(jobRepo.getJobById("job-1")).thenReturn(ResultState.Success(job))
        whenever(applicationRepo.hasApplied("uid-123", "job-1")).thenReturn(ResultState.Success(true))

        viewModel.loadApplyScreen("job-1")

        assertTrue(viewModel.applyUiState.value.jobUnavailable)
    }

    @Test
    fun `submitApplication succeeds and exposes success state`() = runTest {
        stubHappyApply()
        whenever(applicationRepo.submitApplication(job, "uid-123", profileWithResume.resume.downloadUrl, ""))
            .thenReturn(ResultState.Success(ApplicationModel(id = "app-1")))

        viewModel.loadApplyScreen("job-1")
        viewModel.submitApplication()

        val state = viewModel.applyUiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isSubmitting)
        verify(applicationRepo).submitApplication(any(), any(), any(), any())
    }

    @Test
    fun `submitApplication surfaces repository failure`() = runTest {
        stubHappyApply()
        whenever(applicationRepo.submitApplication(any(), any(), any(), any()))
            .thenReturn(ResultState.Error("Network error"))

        viewModel.loadApplyScreen("job-1")
        viewModel.submitApplication()

        val state = viewModel.applyUiState.value
        assertFalse(state.isSuccess)
        assertEquals("Network error", state.errorMessage)
    }

    @Test
    fun `submitApplication without a resume is blocked client-side`() = runTest {
        whenever(jobRepo.getJobById("job-1")).thenReturn(ResultState.Success(job))
        whenever(applicationRepo.hasApplied("uid-123", "job-1")).thenReturn(ResultState.Success(false))
        whenever(profileRepo.getProfile("uid-123")).thenReturn(
            ResultState.Success(JobSeekerProfileModel(userId = "uid-123"))
        )

        viewModel.loadApplyScreen("job-1")
        viewModel.submitApplication()

        assertFalse(viewModel.applyUiState.value.isSuccess)
        verify(applicationRepo, times(0)).submitApplication(any(), any(), any(), any())
    }

    @Test
    fun `repeated submit taps do not create duplicate applications`() = runTest {
        stubHappyApply()
        whenever(applicationRepo.submitApplication(any(), any(), any(), any()))
            .thenReturn(ResultState.Success(ApplicationModel(id = "app-1")))

        viewModel.loadApplyScreen("job-1")
        viewModel.submitApplication()
        viewModel.submitApplication()

        verify(applicationRepo, times(1)).submitApplication(any(), any(), any(), any())
    }

    @Test
    fun `withdrawApplication moves an eligible application to WITHDRAWN`() = runTest {
        val application = ApplicationModel(id = "app-1", jobId = "job-1", applicantId = "uid-123", status = ApplicationStatus.APPLIED.name)
        val withdrawn = application.copy(status = ApplicationStatus.WITHDRAWN.name)
        whenever(applicationRepo.getApplicationById("app-1")).thenReturn(
            ResultState.Success(application),
            ResultState.Success(withdrawn)
        )
        whenever(applicationRepo.withdrawApplication("uid-123", "app-1")).thenReturn(ResultState.Success(Unit))

        viewModel.loadApplicationDetails("app-1")
        viewModel.withdrawApplication()

        assertEquals(ApplicationStatus.WITHDRAWN.name, viewModel.applicationDetailsUiState.value.application?.status)
    }

    @Test
    fun `withdrawApplication is rejected once the application is HIRED`() = runTest {
        val application = ApplicationModel(id = "app-1", jobId = "job-1", applicantId = "uid-123", status = ApplicationStatus.HIRED.name)
        whenever(applicationRepo.getApplicationById("app-1")).thenReturn(ResultState.Success(application))
        whenever(applicationRepo.withdrawApplication("uid-123", "app-1"))
            .thenReturn(ResultState.Error("This application can no longer be withdrawn."))

        viewModel.loadApplicationDetails("app-1")
        viewModel.withdrawApplication()

        assertEquals(ApplicationStatus.HIRED.name, viewModel.applicationDetailsUiState.value.application?.status)
    }

    @Test
    fun `selectApplicationsTab filters by the active tab grouping`() {
        val active = ApplicationModel(id = "a", status = ApplicationStatus.UNDER_REVIEW.name)
        val hired = ApplicationModel(id = "b", status = ApplicationStatus.HIRED.name)

        assertTrue(ApplicationFilterTab.ACTIVE.matches(active.status))
        assertFalse(ApplicationFilterTab.ACTIVE.matches(hired.status))
        assertTrue(ApplicationFilterTab.HIRED.matches(hired.status))
        assertTrue(ApplicationFilterTab.ALL.matches(hired.status))
    }
}
