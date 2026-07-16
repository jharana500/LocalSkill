package com.example.localskill.repo

import com.example.localskill.model.AdminActivityModel
import com.example.localskill.model.AdminActivityType
import com.example.localskill.model.JobReportModel
import com.example.localskill.model.ReportStatus
import com.example.localskill.model.NotificationEntityType
import com.example.localskill.model.NotificationType
import com.example.localskill.model.UserModel
import com.example.localskill.model.UserRole
import com.example.localskill.utils.Constants
import com.example.localskill.utils.FirebaseErrorMapper
import com.example.localskill.utils.ResultState
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ReportRepoImpl(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val notificationRepo: NotificationRepo? = null
) : ReportRepo {

    private val reportsRef: DatabaseReference = database.getReference(Constants.REPORTS_NODE)
    private val activitiesRef: DatabaseReference = database.getReference(Constants.ADMIN_ACTIVITIES_NODE)
    private val usersRef: DatabaseReference = database.getReference(Constants.USERS_NODE)

    override suspend fun submitReport(report: JobReportModel): ResultState<JobReportModel> {
        if (report.reason.isBlank()) return ResultState.Error("A reason is required.")
        if (report.targetId.isBlank()) return ResultState.Error("This item is no longer available to report.")

        return try {
            val existing = reportsRef.get().await().children.mapNotNull { it.getValue(JobReportModel::class.java) }
            val duplicate = existing.any {
                it.reporterId == report.reporterId &&
                        it.targetId == report.targetId &&
                        (it.status == ReportStatus.PENDING.name || it.status == ReportStatus.UNDER_REVIEW.name)
            }
            if (duplicate) return ResultState.Error("You already have an open report for this item.")

            val id = reportsRef.push().key ?: UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val toSave = report.copy(id = id, status = ReportStatus.PENDING.name, createdAt = now, updatedAt = now)
            reportsRef.child(id).setValue(toSave.toMap()).await()
            notifyAdmins(
                senderId = report.reporterId,
                reportId = id,
                eventKeySuffix = "report_submitted:$now"
            )
            ResultState.Success(toSave)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun getReports(status: String?): ResultState<List<JobReportModel>> = try {
        val reports = reportsRef.get().await().children.mapNotNull { it.getValue(JobReportModel::class.java) }
        val filtered = if (status != null) reports.filter { it.status == status } else reports
        ResultState.Success(filtered.sortedByDescending { it.createdAt })
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun getReportById(reportId: String): ResultState<JobReportModel> = try {
        val report = reportsRef.child(reportId).get().await().getValue(JobReportModel::class.java)
        if (report != null) ResultState.Success(report) else ResultState.Error("Report not found.")
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun markUnderReview(adminId: String, reportId: String): ResultState<Unit> = try {
        reportsRef.child(reportId).updateChildren(
            mapOf("status" to ReportStatus.UNDER_REVIEW.name, "updatedAt" to System.currentTimeMillis())
        ).await()
        ResultState.Success(Unit)
    } catch (e: Exception) {
        ResultState.Error(FirebaseErrorMapper.map(e), e)
    }

    override suspend fun resolveReport(adminId: String, reportId: String, resolutionNote: String): ResultState<Unit> {
        if (resolutionNote.isBlank()) return ResultState.Error("A resolution note is required.")
        return try {
            val now = System.currentTimeMillis()
            val current = reportsRef.child(reportId).get().await().getValue(JobReportModel::class.java)
            reportsRef.child(reportId).updateChildren(
                mapOf(
                    "status" to ReportStatus.RESOLVED.name,
                    "resolutionNote" to resolutionNote,
                    "resolvedBy" to adminId,
                    "resolvedAt" to now,
                    "updatedAt" to now
                )
            ).await()
            notifyReporter(current, adminId, NotificationType.REPORT_RESOLVED, "report:$reportId:resolved:$now")
            recordActivity(adminId, AdminActivityType.REPORT_RESOLVED, reportId, "Resolved report: $resolutionNote")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    override suspend fun rejectReport(adminId: String, reportId: String, resolutionNote: String): ResultState<Unit> {
        if (resolutionNote.isBlank()) return ResultState.Error("A resolution note is required.")
        return try {
            val now = System.currentTimeMillis()
            val current = reportsRef.child(reportId).get().await().getValue(JobReportModel::class.java)
            reportsRef.child(reportId).updateChildren(
                mapOf(
                    "status" to ReportStatus.REJECTED.name,
                    "resolutionNote" to resolutionNote,
                    "resolvedBy" to adminId,
                    "resolvedAt" to now,
                    "updatedAt" to now
                )
            ).await()
            notifyReporter(current, adminId, NotificationType.REPORT_REJECTED, "report:$reportId:rejected:$now")
            recordActivity(adminId, AdminActivityType.REPORT_REJECTED, reportId, "Rejected report: $resolutionNote")
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(FirebaseErrorMapper.map(e), e)
        }
    }

    private suspend fun notifyAdmins(senderId: String, reportId: String, eventKeySuffix: String) {
        val admins = usersRef.get().await().children.mapNotNull { it.getValue(UserModel::class.java) }
            .filter { it.role == UserRole.ADMIN.name }
        admins.forEach { admin ->
            notificationRepo?.createNotificationIfAbsent(
                recipientId = admin.id,
                senderId = senderId,
                type = NotificationType.NEW_JOB_REPORT,
                relatedEntityType = NotificationEntityType.REPORT,
                relatedEntityId = reportId,
                eventKey = "report:$reportId:$eventKeySuffix:admin:${admin.id}"
            )
        }
    }

    private suspend fun notifyReporter(
        report: JobReportModel?,
        adminId: String,
        type: NotificationType,
        eventKey: String
    ) {
        if (report == null || report.reporterId.isBlank()) return
        val relatedJobId = report.relatedJobId.ifBlank { report.targetId }
        notificationRepo?.createNotificationIfAbsent(
            recipientId = report.reporterId,
            senderId = adminId,
            type = type,
            relatedEntityType = NotificationEntityType.JOB,
            relatedEntityId = relatedJobId,
            secondaryEntityId = report.id,
            eventKey = eventKey
        )
    }

    private suspend fun recordActivity(
        adminId: String,
        actionType: AdminActivityType,
        targetId: String,
        summary: String
    ) {
        val id = activitiesRef.push().key ?: UUID.randomUUID().toString()
        val activity = AdminActivityModel(
            id = id,
            adminId = adminId,
            actionType = actionType.name,
            targetType = "report",
            targetId = targetId,
            summary = summary,
            createdAt = System.currentTimeMillis()
        )
        runCatching { activitiesRef.child(id).setValue(activity.toMap()).await() }
    }
}
