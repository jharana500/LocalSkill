package com.example.localskill.repo

import com.example.localskill.model.AccountStatus
import com.example.localskill.model.AdminActivityModel
import com.example.localskill.model.AdminActivityType
import com.example.localskill.model.AdminDashboardStatsModel
import com.example.localskill.model.CompanyModel
import com.example.localskill.model.CompanyVerificationStatus
import com.example.localskill.model.JobCategoryModel
import com.example.localskill.model.JobModel
import com.example.localskill.model.JobModerationStatus
import com.example.localskill.model.JobReportModel
import com.example.localskill.model.JobStatus
import com.example.localskill.model.NotificationEntityType
import com.example.localskill.model.NotificationType
import com.example.localskill.model.ReportStatus
import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AdminRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val userRepo: UserRepo,
    private val notificationRepo: NotificationRepo? = null
) : AdminRepo {

    private val usersRef: DatabaseReference = database.getReference(Constants.USERS_NODE)
    private val companiesRef: DatabaseReference = database.getReference(Constants.COMPANIES_NODE)
    private val jobsRef: DatabaseReference = database.getReference(Constants.JOBS_NODE)
    private val applicationsRef: DatabaseReference = database.getReference(Constants.APPLICATIONS_NODE)
    private val reportsRef: DatabaseReference = database.getReference(Constants.REPORTS_NODE)
    private val categoriesRef: DatabaseReference = database.getReference(Constants.JOB_CATEGORIES_NODE)
    private val activitiesRef: DatabaseReference = database.getReference(Constants.ADMIN_ACTIVITIES_NODE)

    override suspend fun getDashboardStats(): ResultState<AdminDashboardStatsModel> = try {
        val users = usersRef.get().await().children.mapNotNull { it.getValue(UserModel::class.java) }
        val companies = companiesRef.get().await().children.mapNotNull { it.getValue(CompanyModel::class.java) }
        val jobs = jobsRef.get().await().children.mapNotNull { it.getValue(JobModel::class.java) }
        val applicationsCount = applicationsRef.get().await().childrenCount.toInt()
        val openReports = reportsRef.get().await().children.mapNotNull { it.getValue(JobReportModel::class.java) }
            .count { it.status == ReportStatus.PENDING.name || it.status == ReportStatus.UNDER_REVIEW.name }

        ResultState.Success(
            AdminDashboardStatsModel(
                totalUsers = users.size,
                activeJobSeekers = users.count {
                    it.role == UserRole.JOB_SEEKER.name && it.accountStatus == AccountStatus.ACTIVE.name
                },
                totalCompanies = companies.size,
                verifiedCompanies = companies.count { it.isVerified },
                pendingCompanies = companies.count { it.isPending },
                rejectedCompanies = companies.count { it.isRejected },
                activeJobs = jobs.count { it.status == JobStatus.ACTIVE.name },
                draftJobs = jobs.count { it.status == JobStatus.DRAFT.name },
                closedJobs = jobs.count { it.status == JobStatus.CLOSED.name },
                totalApplications = applicationsCount,
                openReports = openReports,
                suspendedAccounts = users.count { it.accountStatus == AccountStatus.SUSPENDED.name }
            )
        )
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getAllCompanies(): ResultState<List<CompanyModel>> = try {
        val companies = companiesRef.get().await().children.mapNotNull { it.getValue(CompanyModel::class.java) }
        ResultState.Success(companies.sortedByDescending { it.createdAt })
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getPendingCompanies(): ResultState<List<CompanyModel>> =
        when (val result = getAllCompanies()) {
            is ResultState.Success -> ResultState.Success(
                result.data.filter { it.isPending }.sortedBy { it.verificationSubmittedAt }
            )

            else -> result
        }

    override suspend fun getCompanyById(companyId: String): ResultState<CompanyModel> = try {
        val company = companiesRef.child(companyId).get().await().getValue(CompanyModel::class.java)
        if (company != null) ResultState.Success(company) else ResultState.Error("Company not found.")
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun approveCompany(adminId: String, companyId: String): ResultState<Unit> {
        val companyResult = getCompanyById(companyId)
        if (companyResult !is ResultState.Success) {
            return ResultState.Error((companyResult as? ResultState.Error)?.message ?: "Company not found.")
        }
        val company = companyResult.data
        if (company.verificationStatus != CompanyVerificationStatus.PENDING.name) {
            return ResultState.Error("Only companies under review can be approved.")
        }

        return try {
            val now = System.currentTimeMillis()
            val updates = mapOf(
                "${Constants.COMPANIES_NODE}/$companyId/verificationStatus" to CompanyVerificationStatus.VERIFIED.name,
                "${Constants.COMPANIES_NODE}/$companyId/verifiedAt" to now,
                "${Constants.COMPANIES_NODE}/$companyId/verifiedBy" to adminId,
                "${Constants.COMPANIES_NODE}/$companyId/rejectionReason" to "",
                "${Constants.COMPANIES_NODE}/$companyId/updatedAt" to now,
                "${Constants.USERS_NODE}/${company.ownerUserId}/accountStatus" to AccountStatus.ACTIVE.name,
                "${Constants.USERS_NODE}/${company.ownerUserId}/updatedAt" to now
            )
            database.reference.updateChildren(updates).await()
            notificationRepo?.createNotificationIfAbsent(
                recipientId = company.ownerUserId,
                senderId = adminId,
                type = NotificationType.COMPANY_VERIFIED,
                relatedEntityType = NotificationEntityType.COMPANY,
                relatedEntityId = companyId,
                eventKey = "company:$companyId:verified:$now"
            )
            recordActivity(
                adminId,
                AdminActivityType.COMPANY_APPROVED,
                "company",
                companyId,
                "Approved ${company.companyName}"
            )
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun rejectCompany(adminId: String, companyId: String, reason: String): ResultState<Unit> {
        if (reason.isBlank()) return ResultState.Error("A rejection reason is required.")
        val companyResult = getCompanyById(companyId)
        if (companyResult !is ResultState.Success) {
            return ResultState.Error((companyResult as? ResultState.Error)?.message ?: "Company not found.")
        }
        val company = companyResult.data
        if (company.verificationStatus != CompanyVerificationStatus.PENDING.name) {
            return ResultState.Error("Only companies under review can be rejected.")
        }

        return try {
            val now = System.currentTimeMillis()
            companiesRef.child(companyId).updateChildren(
                mapOf(
                    "verificationStatus" to CompanyVerificationStatus.REJECTED.name,
                    "rejectionReason" to reason,
                    "updatedAt" to now
                )
            ).await()
            notificationRepo?.createNotificationIfAbsent(
                recipientId = company.ownerUserId,
                senderId = adminId,
                type = NotificationType.COMPANY_VERIFICATION_REJECTED,
                relatedEntityType = NotificationEntityType.COMPANY,
                relatedEntityId = companyId,
                eventKey = "company:$companyId:rejected:$now"
            )
            recordActivity(
                adminId,
                AdminActivityType.COMPANY_REJECTED,
                "company",
                companyId,
                "Rejected ${company.companyName}: $reason"
            )
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun getAllUsers(): ResultState<List<UserModel>> = userRepo.getAllUsers()

    override suspend fun suspendUser(adminId: String, targetUserId: String): ResultState<Unit> {
        if (adminId == targetUserId) return ResultState.Error("You cannot suspend your own account.")
        return try {
            usersRef.child(targetUserId).updateChildren(
                mapOf("accountStatus" to AccountStatus.SUSPENDED.name, "updatedAt" to System.currentTimeMillis())
            ).await()
            recordActivity(
                adminId,
                AdminActivityType.USER_SUSPENDED,
                "user",
                targetUserId,
                "Suspended user $targetUserId"
            )
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun reactivateUser(adminId: String, targetUserId: String): ResultState<Unit> = try {
        usersRef.child(targetUserId).updateChildren(
            mapOf("accountStatus" to AccountStatus.ACTIVE.name, "updatedAt" to System.currentTimeMillis())
        ).await()
        recordActivity(
            adminId,
            AdminActivityType.USER_REACTIVATED,
            "user",
            targetUserId,
            "Reactivated user $targetUserId"
        )
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getAllJobsForModeration(): ResultState<List<JobModel>> = try {
        val jobs = jobsRef.get().await().children.mapNotNull { it.getValue(JobModel::class.java) }
        ResultState.Success(jobs.sortedByDescending { it.createdAt })
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun removeJob(adminId: String, jobId: String, reason: String): ResultState<Unit> = try {
        val job = jobsRef.child(jobId).get().await().getValue(JobModel::class.java)
        val now = System.currentTimeMillis()
        jobsRef.child(jobId).updateChildren(
            mapOf(
                "moderationStatus" to JobModerationStatus.REMOVED.name,
                "moderationReason" to reason,
                "updatedAt" to now
            )
        ).await()
        notifyCompanyOwnerForJob(job, adminId, NotificationType.JOB_MODERATED, "job:$jobId:moderated:$now")
        recordActivity(adminId, AdminActivityType.JOB_REMOVED, "job", jobId, "Removed job from discovery: $reason")
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun restoreJob(adminId: String, jobId: String): ResultState<Unit> = try {
        val job = jobsRef.child(jobId).get().await().getValue(JobModel::class.java)
        val now = System.currentTimeMillis()
        jobsRef.child(jobId).updateChildren(
            mapOf(
                "moderationStatus" to JobModerationStatus.VISIBLE.name,
                "moderationReason" to "",
                "updatedAt" to now
            )
        ).await()
        notifyCompanyOwnerForJob(job, adminId, NotificationType.JOB_RESTORED, "job:$jobId:restored:$now")
        recordActivity(adminId, AdminActivityType.JOB_RESTORED, "job", jobId, "Restored job to discovery")
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getCategories(includeInactive: Boolean): ResultState<List<JobCategoryModel>> = try {
        val categories = categoriesRef.get().await().children.mapNotNull { it.getValue(JobCategoryModel::class.java) }
        ResultState.Success(
            (if (includeInactive) categories else categories.filter { it.isActive }).sortedBy { it.name }
        )
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun addCategory(name: String): ResultState<Unit> {
        if (name.isBlank()) return ResultState.Error("Category name is required.")
        val normalized = name.trim().lowercase()
        val existing = getCategories(includeInactive = true)
        if (existing is ResultState.Success && existing.data.any { it.name.trim().lowercase() == normalized }) {
            return ResultState.Error("A category with this name already exists.")
        }

        return try {
            val id = categoriesRef.push().key ?: UUID.randomUUID().toString()
            val category = JobCategoryModel(id = id, name = name.trim(), jobCount = 0, isActive = true)
            categoriesRef.child(id).setValue(category.toMap()).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun setCategoryActive(categoryId: String, isActive: Boolean, adminId: String): ResultState<Unit> =
        try {
            categoriesRef.child(categoryId).child("isActive").setValue(isActive).await()
            val activityType =
                if (isActive) AdminActivityType.CATEGORY_EDITED else AdminActivityType.CATEGORY_DEACTIVATED
            recordActivity(
                adminId,
                activityType,
                "category",
                categoryId,
                if (isActive) "Reactivated category" else "Deactivated category"
            )
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }

    override suspend fun getActivityLog(limit: Int): ResultState<List<AdminActivityModel>> = try {
        val snapshot = activitiesRef.get().await()
        val activities = snapshot.children.mapNotNull { it.getValue(AdminActivityModel::class.java) }
            .sortedByDescending { it.createdAt }
            .take(limit)
        ResultState.Success(activities)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    private suspend fun notifyCompanyOwnerForJob(
        job: JobModel?,
        adminId: String,
        type: NotificationType,
        eventKey: String
    ) {
        if (job == null || job.companyId.isBlank()) return
        val ownerUserId =
            companiesRef.child(job.companyId).child("ownerUserId").get().await().getValue(String::class.java).orEmpty()
        if (ownerUserId.isBlank()) return
        notificationRepo?.createNotificationIfAbsent(
            recipientId = ownerUserId,
            senderId = adminId,
            type = type,
            relatedEntityType = NotificationEntityType.JOB,
            relatedEntityId = job.id,
            secondaryEntityId = job.companyId,
            eventKey = eventKey
        )
    }

    private suspend fun recordActivity(
        adminId: String,
        actionType: AdminActivityType,
        targetType: String,
        targetId: String,
        summary: String
    ) {
        val id = activitiesRef.push().key ?: UUID.randomUUID().toString()
        val activity = AdminActivityModel(
            id = id,
            adminId = adminId,
            actionType = actionType.name,
            targetType = targetType,
            targetId = targetId,
            summary = summary,
            createdAt = System.currentTimeMillis()
        )
        runCatching { activitiesRef.child(id).setValue(activity.toMap()).await() }
    }
}
