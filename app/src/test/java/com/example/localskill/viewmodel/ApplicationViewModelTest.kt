package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeApplicationRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeJobRepo
import com.example.localskill.fakes.FakeJobSeekerProfileRepo
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobSeekerProfileModel
import com.example.localskill.model.ResumeModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeJobRepo: FakeJobRepo
    private lateinit var fakeApplicationRepo: FakeApplicationRepo
    private lateinit var fakeProfileRepo: FakeJobSeekerProfileRepo
    private lateinit var viewModel: ApplicationViewModel

    private val job = JobModel(
        id = "job-1",
        companyId = "company-1",
        title = "Android Developer",
        companyName = "Acme",
        status = "ACTIVE",
        applicationDeadline = 0L
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeJobRepo = FakeJobRepo().apply { activeJobs = listOf(job) }
        fakeApplicationRepo = FakeApplicationRepo()
        fakeProfileRepo = FakeJobSeekerProfileRepo().apply {
            profiles["uid-123"] = JobSeekerProfileModel(
                userId = "uid-123",
                resume = ResumeModel(fileName = "resume.pdf", downloadUrl = "https://example.com/resume.pdf")
            )
        }
        viewModel = ApplicationViewModel(fakeAuthRepo, fakeJobRepo, fakeApplicationRepo, fakeProfileRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadApplyScreen exposes the job and resume`() = runTest {
        viewModel.loadApplyScreen("job-1")
        val state = viewModel.applyUiState.value
        assertEquals("job-1", state.job?.id)
        assertTrue(state.resume?.isPresent == true)
        assertFalse(state.jobUnavailable)
    }

    @Test
    fun `loadApplyScreen rejects a job the user already applied to`() = runTest {
        fakeApplicationRepo.applications["existing"] = ApplicationModel(
            id = "existing",
            jobId = "job-1",
            applicantId = "uid-123",
            status = ApplicationStatus.APPLIED.name
        )

        viewModel.loadApplyScreen("job-1")

        assertTrue(viewModel.applyUiState.value.jobUnavailable)
    }

    @Test
    fun `submitApplication succeeds and exposes success state`() = runTest {
        viewModel.loadApplyScreen("job-1")
        viewModel.submitApplication()

        val state = viewModel.applyUiState.value
        assertTrue(state.isSuccess)
        assertFalse(state.isSubmitting)
        assertEquals(1, fakeApplicationRepo.applications.size)
    }

    @Test
    fun `submitApplication surfaces repository failure`() = runTest {
        fakeApplicationRepo.submitResult = ResultState.Error("Network error")
        viewModel.loadApplyScreen("job-1")
        viewModel.submitApplication()

        val state = viewModel.applyUiState.value
        assertFalse(state.isSuccess)
        assertEquals("Network error", state.errorMessage)
    }

    @Test
    fun `submitApplication without a resume is blocked client-side`() = runTest {
        fakeProfileRepo.profiles["uid-123"] = JobSeekerProfileModel(userId = "uid-123")
        viewModel.loadApplyScreen("job-1")
        viewModel.submitApplication()

        assertFalse(viewModel.applyUiState.value.isSuccess)
        assertTrue(fakeApplicationRepo.applications.isEmpty())
    }

    @Test
    fun `repeated submit taps do not create duplicate applications`() = runTest {
        viewModel.loadApplyScreen("job-1")
        viewModel.submitApplication()
        viewModel.submitApplication()

        assertEquals(1, fakeApplicationRepo.applications.size)
    }

    @Test
    fun `withdrawApplication moves an eligible application to WITHDRAWN`() = runTest {
        fakeApplicationRepo.applications["app-1"] = ApplicationModel(
            id = "app-1",
            jobId = "job-1",
            applicantId = "uid-123",
            status = ApplicationStatus.APPLIED.name
        )

        viewModel.loadApplicationDetails("app-1")
        viewModel.withdrawApplication()

        assertEquals(ApplicationStatus.WITHDRAWN.name, viewModel.applicationDetailsUiState.value.application?.status)
    }

    @Test
    fun `withdrawApplication is rejected once the application is HIRED`() = runTest {
        fakeApplicationRepo.applications["app-1"] = ApplicationModel(
            id = "app-1",
            jobId = "job-1",
            applicantId = "uid-123",
            status = ApplicationStatus.HIRED.name
        )

        viewModel.loadApplicationDetails("app-1")
        viewModel.withdrawApplication()

        assertEquals(ApplicationStatus.HIRED.name, fakeApplicationRepo.applications["app-1"]?.status)
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
