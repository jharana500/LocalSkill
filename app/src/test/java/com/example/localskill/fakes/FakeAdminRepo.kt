package com.example.localskill.fakes

import com.example.localskill.model.AccountStatus
import com.example.localskill.model.AdminActivityModel
import com.example.localskill.model.AdminActivityType
import com.example.localskill.model.AdminDashboardStatsModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobModerationStatus
import com.example.localskill.model.UserModel
import com.example.localskill.repo.AdminRepo
import com.example.localskill.utils.ResultState

/** Mirrors AdminRepoImpl's authorization and moderation rules (self-suspend guard, pending-only
 * approve/reject, duplicate-category rejection, append-only activity log) for ViewModel tests. */
class FakeAdminRepo : AdminRepo {

    val companies = mutableMapOf<String, CompanyModel>()
    val users = mutableMapOf<String, UserModel>()
    val jobs = mutableMapOf<String, JobModel>()
    val categories = mutableMapOf<String, JobCategoryModel>()
    val activityLog = mutableListOf<AdminActivityModel>()
    var dashboardStats: AdminDashboardStatsModel = AdminDashboardStatsModel()
    private var categoryIdCounter = 0

    override suspend fun getDashboardStats(): ResultState<AdminDashboardStatsModel> = ResultState.Success(dashboardStats)

    override suspend fun getAllCompanies(): ResultState<List<CompanyModel>> =
        ResultState.Success(companies.values.sortedByDescending { it.createdAt })

    override suspend fun getPendingCompanies(): ResultState<List<CompanyModel>> =
        ResultState.Success(companies.values.filter { it.isPending })

    override suspend fun getCompanyById(companyId: String): ResultState<CompanyModel> =
        companies[companyId]?.let { ResultState.Success(it) } ?: ResultState.Error("Company not found.")

    override suspend fun approveCompany(adminId: String, companyId: String): ResultState<Unit> {
        val company = companies[companyId] ?: return ResultState.Error("Company not found.")
        if (company.verificationStatus != CompanyVerificationStatus.PENDING.name) {
            return ResultState.Error("Only companies under review can be approved.")
        }
        companies[companyId] = company.copy(
            verificationStatus = CompanyVerificationStatus.VERIFIED.name,
            verifiedAt = System.currentTimeMillis(),
            verifiedBy = adminId,
            rejectionReason = ""
        )
        users[company.ownerUserId]?.let { users[company.ownerUserId] = it.copy(accountStatus = AccountStatus.ACTIVE.name) }
        recordActivity(adminId, AdminActivityType.COMPANY_APPROVED, "company", companyId, "Approved ${company.companyName}")
        return ResultState.Success(Unit)
    }

    override suspend fun rejectCompany(adminId: String, companyId: String, reason: String): ResultState<Unit> {
        if (reason.isBlank()) return ResultState.Error("A rejection reason is required.")
        val company = companies[companyId] ?: return ResultState.Error("Company not found.")
        if (company.verificationStatus != CompanyVerificationStatus.PENDING.name) {
            return ResultState.Error("Only companies under review can be rejected.")
        }
        companies[companyId] = company.copy(verificationStatus = CompanyVerificationStatus.REJECTED.name, rejectionReason = reason)
        recordActivity(adminId, AdminActivityType.COMPANY_REJECTED, "company", companyId, "Rejected ${company.companyName}: $reason")
        return ResultState.Success(Unit)
    }

    override suspend fun getAllUsers(): ResultState<List<UserModel>> = ResultState.Success(users.values.toList())

    override suspend fun suspendUser(adminId: String, targetUserId: String): ResultState<Unit> {
        if (adminId == targetUserId) return ResultState.Error("You cannot suspend your own account.")
        val user = users[targetUserId] ?: return ResultState.Error("User not found.")
        users[targetUserId] = user.copy(accountStatus = AccountStatus.SUSPENDED.name)
        recordActivity(adminId, AdminActivityType.USER_SUSPENDED, "user", targetUserId, "Suspended user $targetUserId")
        return ResultState.Success(Unit)
    }

    override suspend fun reactivateUser(adminId: String, targetUserId: String): ResultState<Unit> {
        val user = users[targetUserId] ?: return ResultState.Error("User not found.")
        users[targetUserId] = user.copy(accountStatus = AccountStatus.ACTIVE.name)
        recordActivity(adminId, AdminActivityType.USER_REACTIVATED, "user", targetUserId, "Reactivated user $targetUserId")
        return ResultState.Success(Unit)
    }

    override suspend fun getAllJobsForModeration(): ResultState<List<JobModel>> =
        ResultState.Success(jobs.values.sortedByDescending { it.createdAt })

    override suspend fun removeJob(adminId: String, jobId: String, reason: String): ResultState<Unit> {
        val job = jobs[jobId] ?: return ResultState.Error("Job not found.")
        jobs[jobId] = job.copy(moderationStatus = JobModerationStatus.REMOVED.name, moderationReason = reason)
        recordActivity(adminId, AdminActivityType.JOB_REMOVED, "job", jobId, "Removed job from discovery: $reason")
        return ResultState.Success(Unit)
    }

    override suspend fun restoreJob(adminId: String, jobId: String): ResultState<Unit> {
        val job = jobs[jobId] ?: return ResultState.Error("Job not found.")
        jobs[jobId] = job.copy(moderationStatus = JobModerationStatus.VISIBLE.name, moderationReason = "")
        recordActivity(adminId, AdminActivityType.JOB_RESTORED, "job", jobId, "Restored job to discovery")
        return ResultState.Success(Unit)
    }

    override suspend fun getCategories(includeInactive: Boolean): ResultState<List<JobCategoryModel>> =
        ResultState.Success(
            (if (includeInactive) categories.values.toList() else categories.values.filter { it.isActive })
                .sortedBy { it.name }
        )

    override suspend fun addCategory(name: String): ResultState<Unit> {
        if (name.isBlank()) return ResultState.Error("Category name is required.")
        val normalized = name.trim().lowercase()
        if (categories.values.any { it.name.trim().lowercase() == normalized }) {
            return ResultState.Error("A category with this name already exists.")
        }
        val id = "category-${categoryIdCounter++}"
        categories[id] = JobCategoryModel(id = id, name = name.trim(), jobCount = 0, isActive = true)
        return ResultState.Success(Unit)
    }

    override suspend fun setCategoryActive(categoryId: String, isActive: Boolean, adminId: String): ResultState<Unit> {
        val category = categories[categoryId] ?: return ResultState.Error("Category not found.")
        categories[categoryId] = category.copy(isActive = isActive)
        val activityType = if (isActive) AdminActivityType.CATEGORY_EDITED else AdminActivityType.CATEGORY_DEACTIVATED
        recordActivity(adminId, activityType, "category", categoryId, if (isActive) "Reactivated category" else "Deactivated category")
        return ResultState.Success(Unit)
    }

    override suspend fun getActivityLog(limit: Int): ResultState<List<AdminActivityModel>> =
        ResultState.Success(activityLog.sortedByDescending { it.createdAt }.take(limit))

    private fun recordActivity(adminId: String, actionType: AdminActivityType, targetType: String, targetId: String, summary: String) {
        activityLog.add(
            AdminActivityModel(
                id = "activity-${activityLog.size}",
                adminId = adminId,
                actionType = actionType.name,
                targetType = targetType,
                targetId = targetId,
                summary = summary,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
