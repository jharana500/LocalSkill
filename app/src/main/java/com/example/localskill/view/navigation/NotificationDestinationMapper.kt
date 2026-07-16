package com.example.localskill.view.navigation

import com.example.localskill.model.NotificationEntityType
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
import com.example.localskill.model.UserRole

object NotificationDestinationMapper {
    fun routeFor(notification: NotificationModel, role: UserRole): String? {
        val type = notification.typedType ?: return null
        if (notification.relatedEntityId.isBlank()) return null
        return when (role) {
            UserRole.JOB_SEEKER -> jobSeekerRoute(type, notification)
            UserRole.COMPANY -> companyRoute(type, notification)
            UserRole.ADMIN -> adminRoute(type, notification)
        }
    }

    private fun jobSeekerRoute(type: NotificationType, notification: NotificationModel): String? = when (type) {
        NotificationType.APPLICATION_SUBMITTED,
        NotificationType.APPLICATION_UNDER_REVIEW,
        NotificationType.APPLICATION_SHORTLISTED,
        NotificationType.INTERVIEW_SCHEDULED,
        NotificationType.APPLICATION_HIRED,
        NotificationType.APPLICATION_REJECTED,
        NotificationType.APPLICATION_WITHDRAWN_CONFIRMATION ->
            requireEntity(
                notification,
                NotificationEntityType.APPLICATION
            )?.let(JobSeekerRoute.ApplicationDetails::createRoute)

        NotificationType.REPORT_RESOLVED,
        NotificationType.REPORT_REJECTED ->
            if (notification.relatedEntityType == NotificationEntityType.JOB.name) {
                JobSeekerRoute.JobDetails.createRoute(notification.relatedEntityId)
            } else null

        else -> null
    }

    private fun companyRoute(type: NotificationType, notification: NotificationModel): String? = when (type) {
        NotificationType.NEW_APPLICATION,
        NotificationType.APPLICATION_WITHDRAWN ->
            requireEntity(
                notification,
                NotificationEntityType.APPLICATION
            )?.let(CompanyRoute.ApplicantDetails::createRoute)

        NotificationType.COMPANY_VERIFIED,
        NotificationType.COMPANY_VERIFICATION_REJECTED,
        NotificationType.VERIFICATION_SUBMITTED_CONFIRMATION -> CompanyRoute.Verification.route

        NotificationType.JOB_MODERATED,
        NotificationType.JOB_RESTORED ->
            requireEntity(notification, NotificationEntityType.JOB)?.let(CompanyRoute.JobDetails::createRoute)

        else -> null
    }

    private fun adminRoute(type: NotificationType, notification: NotificationModel): String? = when (type) {
        NotificationType.NEW_VERIFICATION_REQUEST,
        NotificationType.COMPANY_VERIFICATION_RESUBMITTED ->
            requireEntity(notification, NotificationEntityType.COMPANY)?.let(AdminRoute.CompanyDetails::createRoute)

        NotificationType.NEW_JOB_REPORT ->
            requireEntity(notification, NotificationEntityType.REPORT)?.let(AdminRoute.ReportDetails::createRoute)

        else -> null
    }

    private fun requireEntity(notification: NotificationModel, entityType: NotificationEntityType): String? =
        notification.relatedEntityId.takeIf { notification.relatedEntityType == entityType.name && it.isNotBlank() }
}
