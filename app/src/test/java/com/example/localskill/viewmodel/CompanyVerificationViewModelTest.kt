package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeCompanyRepo
import com.example.localskill.fakes.FakeFileRepo
import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.CompanyDocumentType
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
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

@OptIn(ExperimentalCoroutinesApi::class)
class CompanyVerificationViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeCompanyRepo: FakeCompanyRepo
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
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeCompanyRepo = FakeCompanyRepo()
        viewModel = CompanyVerificationViewModel(fakeAuthRepo, fakeCompanyRepo, FakeFileRepo())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submitVerification is blocked without a registration certificate`() = runTest {
        fakeCompanyRepo.companies["uid-123"] = completeCompany

        viewModel.submitVerification()

        assertEquals(CompanyVerificationStatus.DRAFT.name, fakeCompanyRepo.companies.getValue("uid-123").verificationStatus)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `submitVerification is blocked with an incomplete profile`() = runTest {
        fakeCompanyRepo.companies["uid-123"] = CompanyModel(id = "uid-123", verificationStatus = CompanyVerificationStatus.DRAFT.name)
        fakeCompanyRepo.documents["uid-123"] = mutableListOf(
            CompanyDocumentModel(id = "doc-1", companyId = "uid-123", documentType = CompanyDocumentType.REGISTRATION_CERTIFICATE.name)
        )

        viewModel.submitVerification()

        assertEquals(CompanyVerificationStatus.DRAFT.name, fakeCompanyRepo.companies.getValue("uid-123").verificationStatus)
    }

    @Test
    fun `submitVerification succeeds once profile and documents are complete`() = runTest {
        fakeCompanyRepo.companies["uid-123"] = completeCompany
        fakeCompanyRepo.documents["uid-123"] = mutableListOf(
            CompanyDocumentModel(id = "doc-1", companyId = "uid-123", documentType = CompanyDocumentType.REGISTRATION_CERTIFICATE.name)
        )

        viewModel.submitVerification()

        assertEquals(CompanyVerificationStatus.PENDING.name, fakeCompanyRepo.companies.getValue("uid-123").verificationStatus)
    }

    @Test
    fun `a rejected company can resubmit for verification`() = runTest {
        fakeCompanyRepo.companies["uid-123"] = completeCompany.copy(
            verificationStatus = CompanyVerificationStatus.REJECTED.name,
            rejectionReason = "Missing documents"
        )
        fakeCompanyRepo.documents["uid-123"] = mutableListOf(
            CompanyDocumentModel(id = "doc-1", companyId = "uid-123", documentType = CompanyDocumentType.REGISTRATION_CERTIFICATE.name)
        )

        viewModel.submitVerification()

        val company = fakeCompanyRepo.companies.getValue("uid-123")
        assertEquals(CompanyVerificationStatus.PENDING.name, company.verificationStatus)
        assertEquals("", company.rejectionReason)
    }
}
