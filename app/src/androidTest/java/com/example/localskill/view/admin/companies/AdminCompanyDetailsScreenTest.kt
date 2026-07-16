package com.example.localskill.view.admin.companies

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.localskill.model.AuthSessionModel
import com.example.localskill.model.CompanyDocumentModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.UserModel
import com.example.localskill.model.AdminActivityModel
import com.example.localskill.model.AdminDashboardStatsModel
import com.example.localskill.repo.AdminRepo
import com.example.localskill.repo.AuthRepo
import com.example.localskill.repo.CompanyRepo
import com.example.localskill.utils.ResultState
import com.example.localskill.view.theme.LocalSkillTheme
import com.example.localskill.viewmodel.AdminCompanyViewModel
import org.junit.Rule
import org.junit.Test

/** Minimal in-file fakes: this screen takes a real ViewModel, and only the
 * methods the approve/reject flow actually calls need real behavior. */
private class StubAuthRepo : AuthRepo {
    override fun currentUserId(): String? = "admin-1"
    override fun currentUserEmail(): String? = "admin@example.com"
    override fun isUserLoggedIn(): Boolean = true
    override suspend fun login(email: String, password: String): ResultState<String> = ResultState.Error("unused")
    override suspend fun registerJobSeeker(fullName: String, email: String, phone: String, address: String, password: String): ResultState<String> = ResultState.Error("unused")
    override suspend fun registerCompany(companyName: String, contactPersonName: String, email: String, phone: String, address: String, password: String): ResultState<String> = ResultState.Error("unused")
    override suspend fun sendPasswordResetEmail(email: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun sendEmailVerification(): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun reloadAndCheckEmailVerified(): ResultState<Boolean> = ResultState.Error("unused")
    override suspend fun restoreSession(): ResultState<AuthSessionModel?> = ResultState.Success(null)
    override fun logout() = Unit
}

private class StubCompanyRepo : CompanyRepo {
    override suspend fun getCompany(companyId: String): ResultState<CompanyModel> = ResultState.Error("unused")
    override suspend fun updateCompanyProfile(company: CompanyModel): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun updateLogoUrl(companyId: String, logoUrl: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun getDocuments(companyId: String): ResultState<List<CompanyDocumentModel>> = ResultState.Success(emptyList())
    override suspend fun saveDocumentMetadata(document: CompanyDocumentModel): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun deleteDocumentMetadata(companyId: String, documentId: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun submitVerification(companyId: String): ResultState<Unit> = ResultState.Error("unused")
}

private class RecordingAdminRepo(initialCompany: CompanyModel) : AdminRepo {
    var company = initialCompany
        private set
    var approveCalled = false
    var rejectReason: String? = null

    override suspend fun getDashboardStats(): ResultState<AdminDashboardStatsModel> = ResultState.Success(AdminDashboardStatsModel())
    override suspend fun getAllCompanies(): ResultState<List<CompanyModel>> = ResultState.Success(listOf(company))
    override suspend fun getPendingCompanies(): ResultState<List<CompanyModel>> = ResultState.Success(listOf(company))
    override suspend fun getCompanyById(companyId: String): ResultState<CompanyModel> = ResultState.Success(company)
    override suspend fun approveCompany(adminId: String, companyId: String): ResultState<Unit> {
        approveCalled = true
        company = company.copy(verificationStatus = CompanyVerificationStatus.VERIFIED.name)
        return ResultState.Success(Unit)
    }
    override suspend fun rejectCompany(adminId: String, companyId: String, reason: String): ResultState<Unit> {
        rejectReason = reason
        company = company.copy(verificationStatus = CompanyVerificationStatus.REJECTED.name, rejectionReason = reason)
        return ResultState.Success(Unit)
    }
    override suspend fun getAllUsers(): ResultState<List<UserModel>> = ResultState.Success(emptyList())
    override suspend fun suspendUser(adminId: String, targetUserId: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun reactivateUser(adminId: String, targetUserId: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun getAllJobsForModeration(): ResultState<List<JobModel>> = ResultState.Success(emptyList())
    override suspend fun removeJob(adminId: String, jobId: String, reason: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun restoreJob(adminId: String, jobId: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun getCategories(includeInactive: Boolean): ResultState<List<JobCategoryModel>> = ResultState.Success(emptyList())
    override suspend fun addCategory(name: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun setCategoryActive(categoryId: String, isActive: Boolean, adminId: String): ResultState<Unit> = ResultState.Error("unused")
    override suspend fun getActivityLog(limit: Int): ResultState<List<AdminActivityModel>> = ResultState.Success(emptyList())
}

class AdminCompanyDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val pendingCompany = CompanyModel(
        id = "company-1",
        companyName = "Acme Robotics",
        verificationStatus = CompanyVerificationStatus.PENDING.name
    )

    @Test
    fun approveCompanyButtonApprovesAPendingCompany() {
        val adminRepo = RecordingAdminRepo(pendingCompany)
        val viewModel = AdminCompanyViewModel(StubAuthRepo(), adminRepo, StubCompanyRepo())

        composeTestRule.setContent {
            LocalSkillTheme {
                AdminCompanyDetailsScreen(viewModel = viewModel, companyId = "company-1", onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Approve company").performClick()

        composeTestRule.waitForIdle()
        assert(adminRepo.approveCalled)
    }

    @Test
    fun rejectRequiresANonBlankReasonBeforeItCanBeSubmitted() {
        val adminRepo = RecordingAdminRepo(pendingCompany)
        val viewModel = AdminCompanyViewModel(StubAuthRepo(), adminRepo, StubCompanyRepo())

        composeTestRule.setContent {
            LocalSkillTheme {
                AdminCompanyDetailsScreen(viewModel = viewModel, companyId = "company-1", onBack = {})
            }
        }

        composeTestRule.onNodeWithText("Reject with reason").performClick()
        composeTestRule.onNodeWithText("Reject").assertIsNotEnabled()

        composeTestRule.onNodeWithText("Rejection reason").performTextInput("Missing registration documents")
        composeTestRule.onNodeWithText("Reject").performClick()

        composeTestRule.waitForIdle()
        assert(adminRepo.rejectReason == "Missing registration documents")
    }
}
