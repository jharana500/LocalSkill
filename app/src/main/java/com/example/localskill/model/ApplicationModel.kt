package com.example.localskill.model

data class ApplicationModel(
    val id: String = "",
    val jobId: String = "",
    val applicantId: String = "",
    val companyId: String = "",
    val jobTitle: String = "",
    val companyName: String = "",
    val resumeUrl: String = "",
    val coverLetter: String = "",
    val status: String = ApplicationStatus.APPLIED.name,
    val companyMessage: String = "",
    val interviewDate: Long? = null,
    val interviewLocation: String = "",
    val appliedAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "jobId" to jobId,
        "applicantId" to applicantId,
        "companyId" to companyId,
        "jobTitle" to jobTitle,
        "companyName" to companyName,
        "resumeUrl" to resumeUrl,
        "coverLetter" to coverLetter,
        "status" to status,
        "companyMessage" to companyMessage,
        "interviewDate" to interviewDate,
        "interviewLocation" to interviewLocation,
        "appliedAt" to appliedAt,
        "updatedAt" to updatedAt
    )

    companion object {
        val WITHDRAWABLE_STATUSES = setOf(
            ApplicationStatus.APPLIED.name,
            ApplicationStatus.UNDER_REVIEW.name,
            ApplicationStatus.SHORTLISTED.name,
            ApplicationStatus.INTERVIEW.name
        )

        /** Statuses a company may transition an application into, keyed by its current status. */
        private val COMPANY_TRANSITIONS: Map<String, Set<String>> = mapOf(
            ApplicationStatus.APPLIED.name to setOf(ApplicationStatus.UNDER_REVIEW.name, ApplicationStatus.REJECTED.name),
            ApplicationStatus.UNDER_REVIEW.name to setOf(ApplicationStatus.SHORTLISTED.name, ApplicationStatus.REJECTED.name),
            ApplicationStatus.SHORTLISTED.name to setOf(ApplicationStatus.INTERVIEW.name, ApplicationStatus.REJECTED.name),
            ApplicationStatus.INTERVIEW.name to setOf(ApplicationStatus.HIRED.name, ApplicationStatus.REJECTED.name)
        )

        fun isValidCompanyTransition(from: String, to: String): Boolean =
            COMPANY_TRANSITIONS[from]?.contains(to) == true
    }
}
