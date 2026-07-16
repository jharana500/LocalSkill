package com.example.localskill.repo

import com.example.localskill.model.AdminActivityModel
import com.example.localskill.model.AdminDashboardStatsModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.UserModel
import com.example.localskill.utils.ResultState

/**
 * Every write here is a trusted, administrator-only operation: approving
 * or rejecting company verification, suspending/reactivating accounts,
 * moderating jobs, and managing categories. This is the only repository
 * allowed to write a company's verification fields or a user's
 * accountStatus after registration.
 */
interface AdminRepo {

    suspend fun getDashboardStats(): ResultState<AdminDashboardStatsModel>

    suspend fun getAllCompanies(): ResultState<List<CompanyModel>>

    suspend fun getPendingCompanies(): ResultState<List<CompanyModel>>

    suspend fun getCompanyById(companyId: String): ResultState<CompanyModel>

    suspend fun approveCompany(adminId: String, companyId: String): ResultState<Unit>

    suspend fun rejectCompany(adminId: String, companyId: String, reason: String): ResultState<Unit>

    suspend fun getAllUsers(): ResultState<List<UserModel>>

    /** Refuses to suspend [adminId] itself. */
    suspend fun suspendUser(adminId: String, targetUserId: String): ResultState<Unit>

    suspend fun reactivateUser(adminId: String, targetUserId: String): ResultState<Unit>

    suspend fun getAllJobsForModeration(): ResultState<List<JobModel>>

    suspend fun removeJob(adminId: String, jobId: String, reason: String): ResultState<Unit>

    suspend fun restoreJob(adminId: String, jobId: String): ResultState<Unit>

    suspend fun getCategories(includeInactive: Boolean = true): ResultState<List<JobCategoryModel>>

    suspend fun addCategory(name: String): ResultState<Unit>

    suspend fun setCategoryActive(categoryId: String, isActive: Boolean, adminId: String): ResultState<Unit>

    suspend fun getActivityLog(limit: Int = 50): ResultState<List<AdminActivityModel>>
}
