package com.example.localskill.viewmodel

import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyRepo
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AdminCompanyViewModelTest {

    private lateinit var authRepo: AuthRepo
    private lateinit var adminRepo: AdminRepo
    private lateinit var companyRepo: CompanyRepo
    private lateinit var viewModel: AdminCompanyViewModel

    private val pendingCompany = CompanyModel(
        id = "company-1",
        companyName = "Acme",
        verificationStatus = CompanyVerificationStatus.PENDING.name
    )

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepo = mock()
        whenever(authRepo.currentUserId()).thenReturn("admin-1")
        adminRepo = mock()
        companyRepo = mock()
        whenever(companyRepo.getDocuments("company-1")).thenReturn(ResultState.Success(emptyList()))
        viewModel = AdminCompanyViewModel(authRepo, adminRepo, companyRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `approveCompany moves a pending company to VERIFIED`() = runTest {
        val verifiedCompany = pendingCompany.copy(verificationStatus = CompanyVerificationStatus.VERIFIED.name)
        whenever(adminRepo.getCompanyById("company-1")).thenReturn(
            ResultState.Success(pendingCompany),
            ResultState.Success(verifiedCompany)
        )
        whenever(adminRepo.approveCompany("admin-1", "company-1")).thenReturn(ResultState.Success(Unit))

        viewModel.loadCompanyDetails("company-1")
        viewModel.approveCompany()

        assertEquals(CompanyVerificationStatus.VERIFIED.name, viewModel.detailsUiState.value.company?.verificationStatus)
        verify(adminRepo).approveCompany("admin-1", "company-1")
    }

    @Test
    fun `rejectCompany requires a reason and records it`() = runTest {
        val rejectedCompany = pendingCompany.copy(
            verificationStatus = CompanyVerificationStatus.REJECTED.name,
            rejectionReason = "Missing registration documents."
        )
        whenever(adminRepo.getCompanyById("company-1")).thenReturn(
            ResultState.Success(pendingCompany),
            ResultState.Success(rejectedCompany)
        )
        whenever(adminRepo.rejectCompany("admin-1", "company-1", "Missing registration documents."))
            .thenReturn(ResultState.Success(Unit))

        viewModel.loadCompanyDetails("company-1")
        viewModel.rejectCompany("Missing registration documents.")

        val company = viewModel.detailsUiState.value.company
        assertEquals(CompanyVerificationStatus.REJECTED.name, company?.verificationStatus)
        assertEquals("Missing registration documents.", company?.rejectionReason)
    }

    @Test
    fun `an already-verified company cannot be approved again`() = runTest {
        val verifiedCompany = pendingCompany.copy(verificationStatus = CompanyVerificationStatus.VERIFIED.name)
        whenever(adminRepo.getCompanyById("company-1")).thenReturn(ResultState.Success(verifiedCompany))
        whenever(adminRepo.approveCompany("admin-1", "company-1"))
            .thenReturn(ResultState.Error("Only companies under review can be approved."))

        viewModel.loadCompanyDetails("company-1")
        viewModel.approveCompany()

        verify(adminRepo).approveCompany("admin-1", "company-1")
        assertEquals(CompanyVerificationStatus.VERIFIED.name, viewModel.detailsUiState.value.company?.verificationStatus)
    }
}
