package com.example.localskill.model

/** Company-side application review tabs — distinct from the Job Seeker's own [ApplicationStatus] grouping. */
enum class ApplicantStatusFilter {
    ALL,
    NEW,
    UNDER_REVIEW,
    SHORTLISTED,
    INTERVIEW,
    HIRED,
    REJECTED,
    WITHDRAWN;

    fun matches(status: String): Boolean = when (this) {
        ALL -> true
        NEW -> status == ApplicationStatus.APPLIED.name
        UNDER_REVIEW -> status == ApplicationStatus.UNDER_REVIEW.name
        SHORTLISTED -> status == ApplicationStatus.SHORTLISTED.name
        INTERVIEW -> status == ApplicationStatus.INTERVIEW.name
        HIRED -> status == ApplicationStatus.HIRED.name
        REJECTED -> status == ApplicationStatus.REJECTED.name
        WITHDRAWN -> status == ApplicationStatus.WITHDRAWN.name
    }
}

data class ApplicantFilterModel(
    val jobId: String? = null,
    val statusFilter: ApplicantStatusFilter = ApplicantStatusFilter.ALL
)
