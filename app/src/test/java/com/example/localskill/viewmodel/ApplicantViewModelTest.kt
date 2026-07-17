package com.example.localskill.viewmodel

import com.example.localskill.model.ApplicationModel
import com.example.localskill.model.ApplicationStatus
import com.example.localskill.repo.ApplicantRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyJobRepo
import com.example.localskill.repo.UserRepo
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicantViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var applicantRepo: ApplicantRepo
    private lateinit var companyJobRepo: CompanyJobRepo
    private lateinit var userRepo: UserRepo
    private lateinit var viewModel: ApplicantViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        applicantRepo = mock()
        companyJobRepo = mock()
        userRepo = mock()
        viewModel = ApplicantViewModel(authRepo, applicantRepo, companyJobRepo, userRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun stubDetails(application: ApplicationModel) {
        whenever(applicantRepo.getApplicationDetails("uid-123", "app-1")).thenReturn(ResultState.Success(application))
        whenever(applicantRepo.getApplicantProfile(application.applicantId)).thenReturn(
            ResultState.Error("no profile")
        )
        whenever(userRepo.getUserById(application.applicantId)).thenReturn(ResultState.Error("no user"))
    }

    @Test
    fun `updateStatus allows the next valid pipeline stage`() = runTest {
        val application = ApplicationModel(id = "app-1", companyId = "uid-123", status = ApplicationStatus.APPLIED.name)
        stubDetails(application)
        whenever(applicantRepo.updateApplicationStatus("uid-123", "app-1", ApplicationStatus.UNDER_REVIEW.name, ""))
            .thenReturn(ResultState.Success(Unit))
        val updated = application.copy(status = ApplicationStatus.UNDER_REVIEW.name)
        whenever(applicantRepo.getApplicationDetails("uid-123", "app-1")).thenReturn(
            ResultState.Success(application),
            ResultState.Success(updated)
        )
        whenever(applicantRepo.getApplicantProfile(application.applicantId)).thenReturn(ResultState.Error("no profile"))
        whenever(userRepo.getUserById(application.applicantId)).thenReturn(ResultState.Error("no user"))

        viewModel.loadApplicantDetails("app-1")
        viewModel.updateStatus(ApplicationStatus.UNDER_REVIEW.name)

        assertEquals(ApplicationStatus.UNDER_REVIEW.name, viewModel.detailsUiState.value.application?.status)
    }

    @Test
    fun `updateStatus rejects a transition that skips pipeline stages`() = runTest {
        val application = ApplicationModel(id = "app-1", companyId = "uid-123", status = ApplicationStatus.APPLIED.name)
        stubDetails(application)
        whenever(applicantRepo.updateApplicationStatus("uid-123", "app-1", ApplicationStatus.HIRED.name, ""))
            .thenReturn(ResultState.Error("This status change is not allowed from ${ApplicationStatus.APPLIED.name}."))

        viewModel.loadApplicantDetails("app-1")
        viewModel.updateStatus(ApplicationStatus.HIRED.name)

        assertEquals(ApplicationStatus.APPLIED.name, viewModel.detailsUiState.value.application?.status)
    }

    @Test
    fun `a company cannot update an application belonging to another company`() = runTest {
        whenever(applicantRepo.getApplicationDetails("uid-123", "app-1"))
            .thenReturn(ResultState.Error("This application was not found."))

        viewModel.loadApplicantDetails("app-1")

        assertEquals(null, viewModel.detailsUiState.value.application)
        assertEquals("This application was not found.", viewModel.detailsUiState.value.errorMessage)
    }

    @Test
    fun `scheduleInterview requires a future date`() = runTest {
        val application = ApplicationModel(id = "app-1", companyId = "uid-123", status = ApplicationStatus.SHORTLISTED.name)
        stubDetails(application)
        val past = System.currentTimeMillis() - 100_000
        whenever(applicantRepo.scheduleInterview("uid-123", "app-1", past, "Office", ""))
            .thenReturn(ResultState.Error("Interview date must be in the future."))

        viewModel.loadApplicantDetails("app-1")
        viewModel.scheduleInterview(past, "Office")

        assertEquals(ApplicationStatus.SHORTLISTED.name, viewModel.detailsUiState.value.application?.status)
    }

    @Test
    fun `scheduleInterview with a future date moves the application to INTERVIEW`() = runTest {
        val application = ApplicationModel(id = "app-1", companyId = "uid-123", status = ApplicationStatus.SHORTLISTED.name)
        stubDetails(application)
        val future = System.currentTimeMillis() + 86_400_000L
        whenever(applicantRepo.scheduleInterview("uid-123", "app-1", future, "Office", ""))
            .thenReturn(ResultState.Success(Unit))
        val updated = application.copy(
            status = ApplicationStatus.INTERVIEW.name,
            interviewDate = future,
            interviewLocation = "Office"
        )
        whenever(applicantRepo.getApplicationDetails("uid-123", "app-1")).thenReturn(
            ResultState.Success(application),
            ResultState.Success(updated)
        )
        whenever(applicantRepo.getApplicantProfile(application.applicantId)).thenReturn(ResultState.Error("no profile"))
        whenever(userRepo.getUserById(application.applicantId)).thenReturn(ResultState.Error("no user"))

        viewModel.loadApplicantDetails("app-1")
        viewModel.scheduleInterview(future, "Office")

        val result = viewModel.detailsUiState.value.application
        assertEquals(ApplicationStatus.INTERVIEW.name, result?.status)
        assertEquals(future, result?.interviewDate)
    }

    @Test
    fun `rejectApplication moves the application to REJECTED with a message`() = runTest {
        val application = ApplicationModel(id = "app-1", companyId = "uid-123", status = ApplicationStatus.UNDER_REVIEW.name)
        stubDetails(application)
        whenever(
            applicantRepo.updateApplicationStatus("uid-123", "app-1", ApplicationStatus.REJECTED.name, "Not a fit for this role.")
        ).thenReturn(ResultState.Success(Unit))
        val updated = application.copy(status = ApplicationStatus.REJECTED.name)
        whenever(applicantRepo.getApplicationDetails("uid-123", "app-1")).thenReturn(
            ResultState.Success(application),
            ResultState.Success(updated)
        )
        whenever(applicantRepo.getApplicantProfile(application.applicantId)).thenReturn(ResultState.Error("no profile"))
        whenever(userRepo.getUserById(application.applicantId)).thenReturn(ResultState.Error("no user"))

        viewModel.loadApplicantDetails("app-1")
        viewModel.rejectApplication("Not a fit for this role.")

        assertEquals(ApplicationStatus.REJECTED.name, viewModel.detailsUiState.value.application?.status)
    }
}
