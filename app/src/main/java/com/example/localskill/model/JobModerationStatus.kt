package com.example.localskill.model

/** Administrator moderation state, kept separate from the company-owned [JobStatus] lifecycle. */
enum class JobModerationStatus {
    VISIBLE,
    FLAGGED,
    REMOVED
}
