package com.example.localskill.viewmodel

import com.example.localskill.fakes.FakeAdminRepo
import com.example.localskill.fakes.FakeAuthRepo
import com.example.localskill.fakes.FakeCompanyRepo
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminCompanyViewModelTest {

    private lateinit var fakeAuthRepo: FakeAuthRepo
    private lateinit var fakeAdminRepo: FakeAdminRepo
    private lateinit var viewModel: AdminCompanyViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthRepo = FakeAuthRepo().apply { loggedIn = true }
        fakeAdminRepo = FakeAdminRepo().apply {
            companies["company-1"] = CompanyModel(
                id = "company-1",
                companyName = "Acme",
                verificationStatus = CompanyVerificationStatus.PENDING.name
            )
        }
        viewModel = AdminCompanyViewModel(fakeAuthRepo, fakeAdminRepo, FakeCompanyRepo())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `approveCompany moves a pending company to VERIFIED`() = runTest {
        viewModel.loadCompanyDetails("company-1")
        viewModel.approveCompany()

        assertEquals(CompanyVerificationStatus.VERIFIED.name, fakeAdminRepo.companies.getValue("company-1").verificationStatus)
        assertEquals(1, fakeAdminRepo.activityLog.size)
    }

    @Test
    fun `rejectCompany requires a reason and records it`() = runTest {
        viewModel.loadCompanyDetails("company-1")
        viewModel.rejectCompany("Missing registration documents.")

        val company = fakeAdminRepo.companies.getValue("company-1")
        assertEquals(CompanyVerificationStatus.REJECTED.name, company.verificationStatus)
        assertEquals("Missing registration documents.", company.rejectionReason)
    }

    @Test
    fun `an already-verified company cannot be approved again`() = runTest {
        fakeAdminRepo.companies["company-1"] = fakeAdminRepo.companies.getValue("company-1")
            .copy(verificationStatus = CompanyVerificationStatus.VERIFIED.name)
        viewModel.loadCompanyDetails("company-1")

        viewModel.approveCompany()

        assertEquals(0, fakeAdminRepo.activityLog.size)
    }
}
