package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeCompanyJobRepo
import com.example.localskill.fakes.FakeCompanyRepo
import com.example.localskill.fakes.FakeJobRepo
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompanyJobViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeCompanyRepo: FakeCompanyRepo
    private lateinit var fakeCompanyJobRepo: FakeCompanyJobRepo
    private lateinit var viewModel: CompanyJobViewModel

    private fun fillPublishableForm() {
        viewModel.updateTitle("Android Developer")
        viewModel.updateDescription("a".repeat(100))
        viewModel.updateCategory(com.example.localskill.model.JobCategoryModel(id = "cat-1", name = "Engineering"))
        viewModel.updateLocation("Kathmandu")
        viewModel.updateJobType("FULL_TIME")
        viewModel.updateWorkplaceType("ON_SITE")
        viewModel.updateDeadline(System.currentTimeMillis() + 86_400_000L)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeCompanyRepo = FakeCompanyRepo().apply {
            companies["uid-123"] = CompanyModel(id = "uid-123", companyName = "Acme", verificationStatus = CompanyVerificationStatus.DRAFT.name)
        }
        fakeCompanyJobRepo = FakeCompanyJobRepo(fakeCompanyRepo)
        viewModel = CompanyJobViewModel(fakeAuthRepo, fakeCompanyRepo, fakeCompanyJobRepo, FakeJobRepo())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveDraft persists a new draft job owned by the company`() = runTest {
        viewModel.updateTitle("Android Developer")
        viewModel.saveDraft()

        val saved = fakeCompanyJobRepo.jobs.values.single()
        assertEquals("uid-123", saved.companyId)
        assertEquals(JobStatus.DRAFT.name, saved.status)
    }

    @Test
    fun `publish is blocked while the company is unverified`() = runTest {
        fillPublishableForm()
        viewModel.publish()

        val job = fakeCompanyJobRepo.jobs.values.single()
        assertEquals(JobStatus.DRAFT.name, job.status)
        assertEquals("Your company must be verified before publishing jobs.", viewModel.formUiState.value.errorMessage)
    }

    @Test
    fun `publish succeeds once the company is verified and the form is complete`() = runTest {
        fakeCompanyRepo.companies["uid-123"] = fakeCompanyRepo.companies.getValue("uid-123")
            .copy(verificationStatus = CompanyVerificationStatus.VERIFIED.name)
        fillPublishableForm()
        viewModel.publish()

        val job = fakeCompanyJobRepo.jobs.values.single()
        assertEquals(JobStatus.ACTIVE.name, job.status)
        assertTrue(viewModel.formUiState.value.publishSuccess)
    }

    @Test
    fun `publish reports violations for an incomplete job without persisting it as active`() = runTest {
        fakeCompanyRepo.companies["uid-123"] = fakeCompanyRepo.companies.getValue("uid-123")
            .copy(verificationStatus = CompanyVerificationStatus.VERIFIED.name)
        viewModel.updateTitle("") // deliberately incomplete
        viewModel.publish()

        assertTrue(viewModel.formUiState.value.violations.isNotEmpty())
        assertTrue(fakeCompanyJobRepo.jobs.isEmpty())
    }

    @Test
    fun `closeJob and reopenJob round-trip an active job`() = runTest {
        fakeCompanyJobRepo.jobs["job-1"] = JobModel(
            id = "job-1",
            companyId = "uid-123",
            status = JobStatus.ACTIVE.name,
            applicationDeadline = System.currentTimeMillis() + 86_400_000L
        )

        viewModel.closeJob("job-1")
        assertEquals(JobStatus.CLOSED.name, fakeCompanyJobRepo.jobs.getValue("job-1").status)

        viewModel.reopenJob("job-1")
        assertEquals(JobStatus.ACTIVE.name, fakeCompanyJobRepo.jobs.getValue("job-1").status)
    }

    @Test
    fun `reopenJob is rejected once the original deadline has passed`() = runTest {
        fakeCompanyJobRepo.jobs["job-1"] = JobModel(
            id = "job-1",
            companyId = "uid-123",
            status = JobStatus.CLOSED.name,
            applicationDeadline = System.currentTimeMillis() - 86_400_000L
        )

        viewModel.reopenJob("job-1")

        assertEquals(JobStatus.CLOSED.name, fakeCompanyJobRepo.jobs.getValue("job-1").status)
    }

    @Test
    fun `a company cannot manage a job it does not own`() = runTest {
        fakeCompanyJobRepo.jobs["job-1"] = JobModel(
            id = "job-1",
            companyId = "other-company",
            status = JobStatus.ACTIVE.name
        )

        viewModel.closeJob("job-1")

        assertEquals(JobStatus.ACTIVE.name, fakeCompanyJobRepo.jobs.getValue("job-1").status)
    }
}
