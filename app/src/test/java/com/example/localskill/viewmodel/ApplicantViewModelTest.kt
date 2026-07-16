package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeApplicantRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeCompanyJobRepo
import com.example.localskill.fakes.FakeCompanyRepo
import com.example.localskill.fakes.FakeUserRepo
import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
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
class ApplicantViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeApplicantRepo: FakeApplicantRepo
    private lateinit var viewModel: ApplicantViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeApplicantRepo = FakeApplicantRepo()
        viewModel = ApplicantViewModel(fakeAuthRepo, fakeApplicantRepo, FakeCompanyJobRepo(FakeCompanyRepo()), FakeUserRepo())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateStatus allows the next valid pipeline stage`() = runTest {
        fakeApplicantRepo.applications["app-1"] = ApplicationModel(
            id = "app-1",
            companyId = "uid-123",
            status = ApplicationStatus.APPLIED.name
        )
        viewModel.loadApplicantDetails("app-1")

        viewModel.updateStatus(ApplicationStatus.UNDER_REVIEW.name)

        assertEquals(ApplicationStatus.UNDER_REVIEW.name, fakeApplicantRepo.applications.getValue("app-1").status)
    }

    @Test
    fun `updateStatus rejects a transition that skips pipeline stages`() = runTest {
        fakeApplicantRepo.applications["app-1"] = ApplicationModel(
            id = "app-1",
            companyId = "uid-123",
            status = ApplicationStatus.APPLIED.name
        )
        viewModel.loadApplicantDetails("app-1")

        viewModel.updateStatus(ApplicationStatus.HIRED.name)

        assertEquals(ApplicationStatus.APPLIED.name, fakeApplicantRepo.applications.getValue("app-1").status)
    }

    @Test
    fun `a company cannot update an application belonging to another company`() = runTest {
        fakeApplicantRepo.applications["app-1"] = ApplicationModel(
            id = "app-1",
            companyId = "other-company",
            status = ApplicationStatus.APPLIED.name
        )
        viewModel.loadApplicantDetails("app-1")

        viewModel.updateStatus(ApplicationStatus.UNDER_REVIEW.name)

        assertEquals(ApplicationStatus.APPLIED.name, fakeApplicantRepo.applications.getValue("app-1").status)
    }

    @Test
    fun `scheduleInterview requires a future date`() = runTest {
        fakeApplicantRepo.applications["app-1"] = ApplicationModel(
            id = "app-1",
            companyId = "uid-123",
            status = ApplicationStatus.SHORTLISTED.name
        )
        viewModel.loadApplicantDetails("app-1")

        viewModel.scheduleInterview(System.currentTimeMillis() - 100_000, "Office")

        assertEquals(ApplicationStatus.SHORTLISTED.name, fakeApplicantRepo.applications.getValue("app-1").status)
    }

    @Test
    fun `scheduleInterview with a future date moves the application to INTERVIEW`() = runTest {
        fakeApplicantRepo.applications["app-1"] = ApplicationModel(
            id = "app-1",
            companyId = "uid-123",
            status = ApplicationStatus.SHORTLISTED.name
        )
        viewModel.loadApplicantDetails("app-1")

        val future = System.currentTimeMillis() + 86_400_000L
        viewModel.scheduleInterview(future, "Office")

        val updated = fakeApplicantRepo.applications.getValue("app-1")
        assertEquals(ApplicationStatus.INTERVIEW.name, updated.status)
        assertEquals(future, updated.interviewDate)
    }

    @Test
    fun `rejectApplication moves the application to REJECTED with a message`() = runTest {
        fakeApplicantRepo.applications["app-1"] = ApplicationModel(
            id = "app-1",
            companyId = "uid-123",
            status = ApplicationStatus.UNDER_REVIEW.name
        )
        viewModel.loadApplicantDetails("app-1")

        viewModel.rejectApplication("Not a fit for this role.")

        assertEquals(ApplicationStatus.REJECTED.name, fakeApplicantRepo.applications.getValue("app-1").status)
    }
}
