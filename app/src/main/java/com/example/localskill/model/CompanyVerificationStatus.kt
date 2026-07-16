package com.example.localskill.model

/**
 * DRAFT: registered but has not submitted verification yet.
 * PENDING: submitted, awaiting administrator review (also the legacy value
 * every company created before this phase already has).
 */
enum class CompanyVerificationStatus {
    DRAFT,
    PENDING,
    VERIFIED,
    REJECTED
}
