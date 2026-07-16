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

    const val RESUMES_STORAGE_PATH = "resumes"
    const val PROFILE_IMAGES_STORAGE_PATH = "profileImages"

    const val EMAIL_VERIFICATION_RESEND_COOLDOWN_SECONDS = 60
    const val MIN_PASSWORD_LENGTH = 8

    const val MAX_RESUME_SIZE_BYTES = 5L * 1024 * 1024
    const val MAX_PROFILE_IMAGE_SIZE_BYTES = 3L * 1024 * 1024
    const val MAX_COVER_LETTER_LENGTH = 2000
    const val MAX_SKILLS_COUNT = 30
}
