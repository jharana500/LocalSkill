package com.example.localskill.viewmodel

import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.repo.FileRepo
import com.example.localskill.utils.ResultState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CompanyVerificationViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var companyRepo: CompanyRepo
    private lateinit var fileRepo: FileRepo
    private lateinit var viewModel: CompanyVerificationViewModel

    private val completeCompany = CompanyModel(
        id = "uid-123",
        companyName = "Acme",
        description = "We build things.",
        industry = "Technology",
        address = "123 Main St",
        city = "Kathmandu",
        registrationNumber = "REG-1",
        logoUrl = "https://example.com/logo.png",
        verificationStatus = CompanyVerificationStatus.DRAFT.name
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("uid-123")
        companyRepo = mock()
        fileRepo = mock()
        viewModel = CompanyVerificationViewModel(authRepo, companyRepo, fileRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitVerification is blocked without a registration certificate`() = runTest {
        whenever(companyRepo.submitVerification("uid-123"))
            .thenReturn(ResultState.Error("Upload your registration certificate before submitting."))

        viewModel.submitVerification()

        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submitVerification is blocked with an incomplete profile`() = runTest {
        whenever(companyRepo.submitVerification("uid-123"))
            .thenReturn(ResultState.Error("Complete your company profile before submitting."))

        viewModel.submitVerification()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertEquals(false, viewModel.uiState.value.submitSuccess)
    }

    @Test
    fun `submitVerification succeeds once profile and documents are complete`() = runTest {
        whenever(companyRepo.submitVerification("uid-123")).thenReturn(ResultState.Success(Unit))
        whenever(companyRepo.getCompany("uid-123")).thenReturn(
            ResultState.Success(completeCompany.copy(verificationStatus = CompanyVerificationStatus.PENDING.name))
        )
        whenever(companyRepo.getDocuments("uid-123")).thenReturn(ResultState.Success(emptyList()))

        viewModel.submitVerification()

        assertEquals(true, viewModel.uiState.value.submitSuccess)
        assertEquals(CompanyVerificationStatus.PENDING.name, viewModel.uiState.value.company.verificationStatus)
    }

    @Test
    fun `a rejected company can resubmit for verification`() = runTest {
        whenever(companyRepo.submitVerification("uid-123")).thenReturn(ResultState.Success(Unit))
        whenever(companyRepo.getCompany("uid-123")).thenReturn(
            ResultState.Success(
                completeCompany.copy(verificationStatus = CompanyVerificationStatus.PENDING.name, rejectionReason = "")
            )
        )
        whenever(companyRepo.getDocuments("uid-123")).thenReturn(ResultState.Success(emptyList()))

        viewModel.submitVerification()

        val company = viewModel.uiState.value.company
        assertEquals(CompanyVerificationStatus.PENDING.name, company.verificationStatus)
        assertEquals("", company.rejectionReason)
    }
}
