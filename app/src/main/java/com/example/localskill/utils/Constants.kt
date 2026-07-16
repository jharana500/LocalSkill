package com.example.localskill.utils

object Constants {
    const val USERS_NODE = "users"
    const val COMPANIES_NODE = "companies"
    const val JOB_SEEKER_PROFILES_NODE = "jobSeekerProfiles"
    const val JOBS_NODE = "jobs"
    const val JOB_CATEGORIES_NODE = "jobCategories"
    const val APPLICATIONS_NODE = "applications"
    const val USER_APPLICATIONS_NODE = "userApplications"
    const val JOB_APPLICATIONS_NODE = "jobApplications"
    const val SAVED_JOBS_NODE = "savedJobs"
    const val COMPANY_DOCUMENTS_NODE = "companyDocuments"
    const val COMPANY_JOBS_NODE = "companyJobs"
    const val COMPANY_APPLICATIONS_NODE = "companyApplications"
    const val REPORTS_NODE = "reports"
    const val ADMIN_ACTIVITIES_NODE = "adminActivities"
    const val NOTIFICATIONS_NODE = "notifications"
    const val USER_DEVICES_NODE = "userDevices"

    const val RESUMES_STORAGE_PATH = "resumes"
    const val PROFILE_IMAGES_STORAGE_PATH = "profileImages"
    const val COMPANY_LOGOS_STORAGE_PATH = "companyLogos"
    const val COMPANY_DOCUMENTS_STORAGE_PATH = "companyDocuments"
    const val REPORT_EVIDENCE_STORAGE_PATH = "reportEvidence"

    const val EMAIL_VERIFICATION_RESEND_COOLDOWN_SECONDS = 60
    const val MIN_PASSWORD_LENGTH = 8

    const val MAX_RESUME_SIZE_BYTES = 5L * 1024 * 1024
    const val MAX_PROFILE_IMAGE_SIZE_BYTES = 3L * 1024 * 1024
    const val MAX_COMPANY_LOGO_SIZE_BYTES = 3L * 1024 * 1024
    const val MAX_COMPANY_DOCUMENT_SIZE_BYTES = 8L * 1024 * 1024
    const val MAX_REPORT_EVIDENCE_SIZE_BYTES = 5L * 1024 * 1024
    const val MAX_COVER_LETTER_LENGTH = 2000
    const val MAX_SKILLS_COUNT = 30
    const val MAX_JOB_DESCRIPTION_MIN_LENGTH = 80
}
