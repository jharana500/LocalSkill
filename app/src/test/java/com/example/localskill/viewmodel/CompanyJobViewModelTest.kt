package com.example.localskill.viewmodel

import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyJobRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.repo.JobRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CompanyJobViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var companyRepo: CompanyRepo
    private lateinit var companyJobRepo: CompanyJobRepo
    private lateinit var jobRepo: JobRepo
    private lateinit var viewModel: CompanyJobViewModel

    private fun fillPublishableForm() {
        viewModel.updateTitle("Android Developer")
        viewModel.updateDescription("a".repeat(100))
        viewModel.updateCategory(JobCategoryModel(id = "cat-1", name = "Engineering"))
        viewModel.updateLocation("Kathmandu")
        viewModel.updateJobType("FULL_TIME")
        viewModel.updateWorkplaceType("ON_SITE")
        viewModel.updateDeadline(System.currentTimeMillis() + 86_400_000L)
    }

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        companyRepo = mock()
        whenever(companyRepo.getCompany("uid-123")).thenReturn(
            ResultState.Success(CompanyModel(id = "uid-123", companyName = "Acme", verificationStatus = CompanyVerificationStatus.DRAFT.name))
        )
        companyJobRepo = mock()
        jobRepo = mock()
        viewModel = CompanyJobViewModel(authRepo, companyRepo, companyJobRepo, jobRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveDraft persists a new draft job owned by the company`() = runTest {
        whenever(companyJobRepo.createDraft(any())).thenAnswer { invocation ->
            val job = invocation.getArgument<JobModel>(0)
            ResultState.Success(job.copy(id = "job-0", status = JobStatus.DRAFT.name))
        }

        viewModel.updateTitle("Android Developer")
        viewModel.saveDraft()

        assertEquals("job-0", viewModel.formUiState.value.jobId)
        assertTrue(viewModel.formUiState.value.saveSuccess)
        verify(companyJobRepo).createDraft(
            argThat { companyId == "uid-123" && status == JobStatus.DRAFT.name }
        )
    }

    @Test
    fun `publish is blocked while the company is unverified`() = runTest {
        whenever(companyJobRepo.createDraft(any())).thenAnswer { invocation ->
            val job = invocation.getArgument<JobModel>(0)
            ResultState.Success(job.copy(id = "job-0", status = JobStatus.DRAFT.name))
        }
        whenever(companyJobRepo.publishJob("uid-123", "job-0"))
            .thenReturn(ResultState.Error("Your company must be verified before publishing jobs."))

        fillPublishableForm()
        viewModel.publish()

        assertEquals("Your company must be verified before publishing jobs.", viewModel.formUiState.value.errorMessage)
        assertTrue(!viewModel.formUiState.value.publishSuccess)
    }

    @Test
    fun `publish succeeds once the company is verified and the form is complete`() = runTest {
        whenever(companyJobRepo.createDraft(any())).thenAnswer { invocation ->
            val job = invocation.getArgument<JobModel>(0)
            ResultState.Success(job.copy(id = "job-0", status = JobStatus.DRAFT.name))
        }
        whenever(companyJobRepo.publishJob("uid-123", "job-0")).thenReturn(ResultState.Success(Unit))

        fillPublishableForm()
        viewModel.publish()

        assertTrue(viewModel.formUiState.value.publishSuccess)
    }

    @Test
    fun `publish reports violations for an incomplete job without persisting it as active`() = runTest {
        viewModel.updateTitle("") // deliberately incomplete

        viewModel.publish()

        assertTrue(viewModel.formUiState.value.violations.isNotEmpty())
        verify(companyJobRepo, never()).createDraft(any())
    }

    @Test
    fun `closeJob and reopenJob round-trip an active job`() = runTest {
        val activeJob = JobModel(
            id = "job-1",
            companyId = "uid-123",
            status = JobStatus.ACTIVE.name,
            applicationDeadline = System.currentTimeMillis() + 86_400_000L
        )
        val closedJob = activeJob.copy(status = JobStatus.CLOSED.name)

        whenever(companyJobRepo.closeJob("uid-123", "job-1")).thenReturn(ResultState.Success(Unit))
        whenever(companyJobRepo.reopenJob("uid-123", "job-1")).thenReturn(ResultState.Success(Unit))
        whenever(companyJobRepo.getCompanyJobs("uid-123")).thenReturn(
            ResultState.Success(listOf(closedJob)),
            ResultState.Success(listOf(activeJob))
        )

        viewModel.closeJob("job-1")
        assertEquals(JobStatus.CLOSED.name, viewModel.jobsUiState.value.jobs.single().status)

        viewModel.reopenJob("job-1")
        assertEquals(JobStatus.ACTIVE.name, viewModel.jobsUiState.value.jobs.single().status)
    }

    @Test
    fun `reopenJob is rejected once the original deadline has passed`() = runTest {
        whenever(companyJobRepo.reopenJob("uid-123", "job-1"))
            .thenReturn(ResultState.Error("This job's deadline has passed. Update the deadline before reopening."))

        val events = mutableListOf<CompanyJobEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.reopenJob("job-1")
        advanceUntilIdle()

        assertEquals(
            listOf(CompanyJobEvent.ShowMessage("This job's deadline has passed. Update the deadline before reopening.")),
            events
        )
        job.cancel()
    }

    @Test
    fun `a company cannot manage a job it does not own`() = runTest {
        whenever(companyJobRepo.closeJob("uid-123", "job-1"))
            .thenReturn(ResultState.Error("You do not have permission to manage this job."))

        val events = mutableListOf<CompanyJobEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.closeJob("job-1")
        advanceUntilIdle()

        assertEquals(
            listOf(CompanyJobEvent.ShowMessage("You do not have permission to manage this job.")),
            events
        )
        job.cancel()
    }
}
