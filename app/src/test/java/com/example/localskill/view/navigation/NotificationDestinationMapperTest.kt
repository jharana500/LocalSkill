package com.example.localskill.view.navigation

import com.example.localskill.model.NotificationEntityType
import com.example.localskill.model.NotificationModel
import com.example.localskill.model.NotificationType
import com.example.localskill.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationDestinationMapperTest {

    @Test
    fun jobSeekerApplicationNotificationRoutesToApplicationDetails() {
        val notification =
            notification(NotificationType.INTERVIEW_SCHEDULED, NotificationEntityType.APPLICATION, "app-1")

        val route = NotificationDestinationMapper.routeFor(notification, UserRole.JOB_SEEKER)

        assertEquals(JobSeekerRoute.ApplicationDetails.createRoute("app-1"), route)
    }

    @Test
    fun companyApplicationNotificationRoutesToApplicantDetails() {
        val notification = notification(NotificationType.NEW_APPLICATION, NotificationEntityType.APPLICATION, "app-2")

        val route = NotificationDestinationMapper.routeFor(notification, UserRole.COMPANY)

        assertEquals(CompanyRoute.ApplicantDetails.createRoute("app-2"), route)
    }

    @Test
    fun adminVerificationNotificationRoutesToCompanyDetails() {
        val notification =
            notification(NotificationType.NEW_VERIFICATION_REQUEST, NotificationEntityType.COMPANY, "company-1")

        val route = NotificationDestinationMapper.routeFor(notification, UserRole.ADMIN)

        assertEquals(AdminRoute.CompanyDetails.createRoute("company-1"), route)
    }

    @Test
    fun roleMismatchReturnsNull() {
        val notification = notification(NotificationType.NEW_APPLICATION, NotificationEntityType.APPLICATION, "app-2")

        assertNull(NotificationDestinationMapper.routeFor(notification, UserRole.ADMIN))
    }

    @Test
    fun invalidTypeReturnsNull() {
        val notification = NotificationModel(type = "job_seeker/application/app-1", relatedEntityId = "app-1")

        assertNull(NotificationDestinationMapper.routeFor(notification, UserRole.JOB_SEEKER))
    }

    private fun notification(
        type: NotificationType,
        entityType: NotificationEntityType,
        entityId: String
    ) = NotificationModel(
        id = "n-1",
        recipientId = "user-1",
        senderId = "sender-1",
        type = type.name,
        relatedEntityType = entityType.name,
        relatedEntityId = entityId,
        createdAt = 1_000L
    )
}
